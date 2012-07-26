package com.polopoly.ps.ci.exception;

public class NoSuchFileInRepo extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 9048866306994942877L;

    public NoSuchFileInRepo() {
        super();
    }

    public NoSuchFileInRepo(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public NoSuchFileInRepo(String arg0) {
        super(arg0);
    }

    public NoSuchFileInRepo(Throwable arg0) {
        super(arg0);
    }

}
