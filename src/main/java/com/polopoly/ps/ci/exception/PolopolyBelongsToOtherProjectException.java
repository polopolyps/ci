package com.polopoly.ps.ci.exception;

public class PolopolyBelongsToOtherProjectException extends CIException {
    /**
     * 
     */
    private static final long serialVersionUID = 6412527054519846855L;

    public PolopolyBelongsToOtherProjectException() {
        super("The running Polopoly does not belong to the current project.");
    }
}
