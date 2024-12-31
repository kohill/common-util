package com.healthedge.ftp;

import java.util.Objects;

public class FtpCommandResult {
    private CharSequence output;
    private CharSequence errorOutput;
    private int exitCode;

    // Constructor has package access since this class should be instantiated/used only within 'ssh' package
    FtpCommandResult() {
    }

    public String getOutput() {
        return output.toString().trim();
    }

    FtpCommandResult setOutput(CharSequence output) {
        this.output = Objects.requireNonNull(output);
        return this;
    }

    public String getErrorOutput() {
        return errorOutput.toString().trim();
    }

    FtpCommandResult setErrorOutput(CharSequence errorOutput) {
        this.errorOutput = Objects.requireNonNull(errorOutput);
        return this;
    }

    public int getExitCode() {
        return exitCode;
    }

    FtpCommandResult setExitCode(int exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("{\n");
        sb.append("    Exit code:      ").append(getExitCode()).append("\n");
        sb.append("    Command output: '").append(output.length() > 0 ? getOutput() : "<EMPTY>").append("'\n");
        if (errorOutput.length() > 0) {
            sb.append("    Error output:   '").append(getErrorOutput()).append("'\n");
        }
        return sb.toString();
    }
}
