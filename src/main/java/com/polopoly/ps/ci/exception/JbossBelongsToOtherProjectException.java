package com.polopoly.ps.ci.exception;

public class JbossBelongsToOtherProjectException extends CIException {

    /**
     * 
     */
    private static final long serialVersionUID = 6040602142503920741L;

    public JbossBelongsToOtherProjectException(String message) {
        super(message);
    }

    public JbossBelongsToOtherProjectException() {
        super("The running JBoss does not belong to the current project.");
    }

}
