package com.healthedge.ftp;

import com.healthedge.config.Properties;
import com.healthedge.config.props.PropertyReader;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FtpUtilities {

    public static final Logger LOG = LogManager.getLogger(FtpUtilities.class);
    public static final Integer TIMEOUT = PropertyReader.getProperty(Properties.FTP_TIMEOUT_MINUTES, 3);
    private static final HashMap<String, Session> connectionMap = new HashMap();
    private final Session session;
    private ChannelSftp channelSftp;
    private ChannelExec channelExec;

    private FtpUtilities(Session session) {
        this.session = session;
    }

    /**
     * Creates instance with connection params from properties.
     * <p>Example: 'envPrefix=etl' - requires properties below to be set:</p>
     * <ol>
     * <li>etl.ftp.host={host}</li>
     * <li>etl.ftp.username={username}</li>
     * <li>etl.ftp.password={password}</li>
     * <li>and optional (default = 22): etl.ftp.port={port}</li>
     * </ol>
     *
     * @param envPrefix - use predefined properties with env name prefix.
     * @return {@link FtpUtilities} instance
     */

    public static synchronized FtpUtilities get(String envPrefix) {
        FtpConnection ftpConnection = new FtpConnection();
        ftpConnection.setFtpHost(PropertyReader.getProperty(envPrefix + "." + Properties.FTP_HOST))
                .setFtpUser(PropertyReader.getProperty((envPrefix + "." + Properties.FTP_USERNAME)))
                .setFtpPassword(PropertyReader.getProperty((envPrefix + "." + Properties.FTP_PASSWORD)))
                .setFtpPort(PropertyReader.getProperty((envPrefix + "." + Properties.FTP_PORT), 22));
        String key = String.valueOf(ftpConnection.hashCode());
        if (!connectionMap.containsKey(key) || !connectionMap.get(key).isConnected()) {
            connectionMap.put(key, ftpConnection.getConnection());
        }
        return new FtpUtilities(connectionMap.get(key));
    }

    /**
     * Creates instance with preconfigured {@link FtpConnection}
     *
     * @param ftpConnection - preconfigured {@link FtpConnection} connection
     * @return {@link FtpUtilities} instance
     */
    public static synchronized FtpUtilities get(FtpConnection ftpConnection) {
        String key = String.valueOf(ftpConnection.hashCode());
        if (!connectionMap.containsKey(key) || !connectionMap.get(key).isConnected()) {
            connectionMap.put(key, ftpConnection.getConnection());
        }
        return new FtpUtilities(connectionMap.get(key));
    }

    /**
     * Download source file from FTP server to local
     *
     * @param srcPath  complete location of source file along with its name
     * @param destPath local dest path
     */
    public void downloadFile(String srcPath, String destPath) {
        try {
            FileTransferProgressMonitor monitor = new FileTransferProgressMonitor();
            getSftpChannel().get(srcPath, destPath, monitor);
            while (!monitor.isCompleted()) {
                TimeUnit.SECONDS.sleep(Duration.ofSeconds(3).toSeconds());
            }
        } catch (SftpException e) {
            throw new RuntimeException("Error downloading file from server: ", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error during wait for download finish: ", e);
        }
    }

    /**
     * Upload source file from local to server
     *
     * @param srcPath  source path along with filename
     * @param destPath local dest path
     */
    public void uploadFile(String srcPath, String destPath) {
        LOG.info("uploadFile -> start");
        try {
            FileTransferProgressMonitor monitor = new FileTransferProgressMonitor();
            getSftpChannel().put(srcPath, destPath, monitor);
            while (!monitor.isCompleted()) {
                TimeUnit.SECONDS.sleep(Duration.ofSeconds(3).toSeconds());
            }
        } catch (SftpException e) {
            throw new RuntimeException("Error uploading file to server: ", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error during wait for upload finish: ", e);
        }
    }


    public FtpCommandResult executeCommand(String command) {
        FtpCommandResult result = new FtpCommandResult();
        Instant currentTime = Instant.now();
        Instant endTime = currentTime.plus(Duration.ofMinutes(TIMEOUT));
        StringBuilder outputBuffer = new StringBuilder();
        StringBuilder errorBuffer = new StringBuilder();
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            InputStream inputStream = channelExec.getInputStream();
            InputStream errorStream = channelExec.getErrStream();
            while (currentTime.isBefore(endTime)) {
                outputBuffer.append(getOutput(inputStream));
                errorBuffer.append(getOutput(errorStream));
                if (channelExec.isClosed() && inputStream.available() == 0 && errorStream.available() == 0) {
                    break;
                }
                TimeUnit.SECONDS.sleep(Duration.ofSeconds(3).toSeconds());
                currentTime = Instant.now();
            }
            result.setExitCode(channelExec.getExitStatus()).setOutput(outputBuffer).setErrorOutput(errorBuffer);

        } catch (JSchException e) {
            throw new RuntimeException("Error executing command: " + command, e);
        } catch (IOException e) {
            LOG.error("Error getting command execution output", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error during wait execution result", e);
        } finally {
            if (!channelExec.isClosed()) {
                channelExec.disconnect();
            }
        }
        LOG.info(result.toString());
        return result;
    }

    /**
     * Establish ChannelSftp
     *
     * @return current SftpChannel
     */

    public ChannelSftp getSftpChannel() {
        if (channelSftp == null) {
            try {
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
            } catch (JSchException e) {
                throw new RuntimeException("Error opening SFTP Channel: ", e);
            }
        }
        return channelSftp;
    }

    /**
     * Gets SFTP Session instance
     *
     * @return {@link Session} instance
     */
    public Session getSession() {
        return session;
    }

    /**
     * Closes ChannelSftp, ChannelExec and Session
     */
    public void closeSession() {
        if (!channelSftp.isClosed()) {
            channelSftp.disconnect();
        }
        if (!channelExec.isClosed()) {
            channelExec.disconnect();
        }
        session.disconnect();
    }


    private StringBuilder getOutput(InputStream commandOutput) {
        StringBuilder outputBuffer = new StringBuilder();
        byte[] tmp = new byte[1024];
        try {
            while (commandOutput.available() > 0) {
                int i = commandOutput.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                outputBuffer.append(new String(tmp, 0, i));
            }
        } catch (IOException e) {
            LOG.error("Error getting command execution output", e);
        }
        return outputBuffer;
    }
}