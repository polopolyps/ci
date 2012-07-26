package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.AbstractConfiguration;

public class Main {
    private static PolopolyJarLoader jarLoader = new PolopolyJarLoader();

    public static void main(String[] args) {
        // we have to do some manual parsing of the most important parameters
        // here since
        // parameter parsing is in pcmd and we haven't loaded pcmd yet.
        for (String arg : args) {
            if (arg.equals("--verbose") || arg.equals("--verbose=true")) {
                VerboseLogging.setVerbose(true);
            }

            if (arg.startsWith("--home=")) {
                AbstractConfiguration.setHomeDirectory(new File(arg.substring(7)));
            }
        }

        jarLoader.loadJarsNeededForCI();

        new MainAfterLoadingJars(args, jarLoader).main();
    }
}
