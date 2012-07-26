package com.polopoly.ps.ci;

public class VerboseLogging {
    private static boolean verbose;

    public static void log(String string) {
        if (verbose) {
            System.out.println(string);
        }
    }

    public static void setVerbose(boolean verbose) {
        VerboseLogging.verbose = verbose;
    }

}
