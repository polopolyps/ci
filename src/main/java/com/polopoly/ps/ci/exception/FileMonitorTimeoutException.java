package com.polopoly.ps.ci.exception;

public class FileMonitorTimeoutException extends CIException {
    /**
     * 
     */
    private static final long serialVersionUID = 3911791243966279814L;

    public FileMonitorTimeoutException(String message) {
        super(message);
    }
}
