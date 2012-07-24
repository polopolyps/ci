package com.polopoly.ps.ci;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class MavenVersionSanityChecker {

    public void sanityCheckMavenVersion() {
        String desiredVersion = new Configuration().getMavenVersion().getValue().trim();

        if (desiredVersion.equals("")) {
            return;
        }

        String output = new Executor("mvn -version").setOutputOnConsole(false).execute();

        String versionPrefix = "Apache Maven ";

        if (output.startsWith(versionPrefix)) {
            int i = output.indexOf(' ', versionPrefix.length());

            String actualVersion = output.substring(versionPrefix.length(), i);

            if (!actualVersion.startsWith(desiredVersion)) {
                throw new CIException("The project requires Maven " + desiredVersion + " but the current Maven version is "
                        + actualVersion + ".");
            }

            System.out.println("Maven version: " + actualVersion);
        } else {
            System.out.println("Could not determine Maven version. Expected the following to start with \"" + versionPrefix
                    + "\": " + output);
        }
    }

}
