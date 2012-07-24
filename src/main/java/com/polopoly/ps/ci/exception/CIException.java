package com.polopoly.ps.ci.exception;

/**
 * This may not extend FatalToolException because then it cannot be used before
 * the PCMD JAR has been loaded.
 */
public class CIException extends RuntimeException {
    public CIException(String message, Throwable cause) {
        super(message, cause);
    }

    public CIException(String message) {
        super(message);
    }

    public CIException(Throwable cause) {
        super(cause);
    }

}
