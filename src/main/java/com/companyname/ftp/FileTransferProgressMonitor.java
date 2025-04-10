package com.companyname.ftp;

import com.jcraft.jsch.SftpProgressMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTransferProgressMonitor implements SftpProgressMonitor {

    public static final Logger LOG = LogManager.getLogger(FileTransferProgressMonitor.class);
    private long max = 0;
    private long count = 0;
    private long percent = 0;

    private Boolean endFlag = false;

    @Override
    public void init(int i, String srcPath, String destPath, long max) {
        this.max = max;
        LOG.info("Starting the Download from: " + srcPath + " to " + destPath + ". Total File Size" + max);
    }

    @Override
    public boolean count(long bytes) {
        this.count += bytes;
        long percentNow = this.count * 100 / max;
        if (percentNow > this.percent) {
            this.percent = percentNow;
            LOG.info("In Progress: " + this.percent);
            LOG.info("Total file size: " + this.max);
        }
        return true;
    }

    @Override
    public void end() {
        endFlag = true;
        LOG.info("File downloaded successfully " + this.percent + ". Size: " + max);
    }

    public Boolean isCompleted() {
        return endFlag;
    }
}