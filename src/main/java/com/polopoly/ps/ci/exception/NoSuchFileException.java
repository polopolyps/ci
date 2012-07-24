package com.polopoly.ps.ci.exception;

public class NoSuchFileException extends CIException {

    /**
     * 
     */
    private static final long serialVersionUID = 7831343961450672688L;

    public NoSuchFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchFileException(String message) {
        super(message);
    }

    public NoSuchFileException(Throwable cause) {
        super(cause);
    }

}
