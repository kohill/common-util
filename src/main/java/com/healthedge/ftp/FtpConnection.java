package com.healthedge.ftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import java.util.StringJoiner;

public class FtpConnection {

    private String ftpHost;
    private String ftpUser;
    private String ftpPassword;
    private Integer ftpPort;

    public FtpConnection() {
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public FtpConnection setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
        return this;
    }

    public String getFtpUser() {
        return ftpUser;
    }

    public FtpConnection setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
        return this;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public FtpConnection setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
        return this;
    }

    public Integer getFtpPort() {
        return ftpPort;
    }

    public FtpConnection setFtpPort(Integer ftpPort) {
        this.ftpPort = ftpPort;
        return this;
    }

    public Session getConnection() {
        if (StringUtils.isBlank(ftpHost) || StringUtils.isBlank(ftpUser) || StringUtils.isBlank(ftpPassword)) {
            throw new RuntimeException(String.format("Not all connection parameters are set, please check: ftpHost=%1s, ftpUser=%2s, ftpPassword=%3s,ftpPort=%4s", ftpHost, ftpUser, ftpPassword, ftpPort));
        }
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(ftpUser, ftpHost, ftpPort);
            session.setPassword(ftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect();
            return session;
        } catch (JSchException e) {
            throw new RuntimeException("Not possible to create connection: ", e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FtpConnection.class.getSimpleName() + "[", "]")
                .add("ftpHost='" + ftpHost + "'")
                .add("ftpUser='" + ftpUser + "'")
                .add("ftpPassword='" + ftpPassword + "'")
                .add("ftpPort=" + ftpPort)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FtpConnection that = (FtpConnection) o;

        if (!ftpHost.equals(that.ftpHost)) return false;
        if (!ftpUser.equals(that.ftpUser)) return false;
        if (!ftpPassword.equals(that.ftpPassword)) return false;
        return ftpPort != null ? ftpPort.equals(that.ftpPort) : that.ftpPort == null;
    }

    @Override
    public int hashCode() {
        int result = ftpHost.hashCode();
        result = 31 * result + ftpUser.hashCode();
        result = 31 * result + ftpPassword.hashCode();
        result = 31 * result + (ftpPort != null ? ftpPort.hashCode() : 0);
        return result;
    }
}