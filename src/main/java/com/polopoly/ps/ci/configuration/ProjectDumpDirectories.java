package com.polopoly.ps.ci.configuration;

import java.io.File;
import java.io.IOException;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NotRunningInHudsonException;

public class ProjectDumpDirectories extends AbstractDirectories {
    public static final String DUMP_FILE_NAME = "polopolydump.tar.gz";

    public File getDefaultDumpDirectory(boolean mustExist) {
        File result = getDefaultDumpFile(false).getParentFile();

        if (result == null) {
            result = new File(".");
        }

        if (mustExist) {
            return verifyDirectoryExistence(result);
        } else {
            return result;
        }
    }

    public File getDefaultDumpFile(boolean mustExist) {
        File directory;

        try {
            directory = new HudsonVariables().getThisBuildUserContentDirectory();
        } catch (NotRunningInHudsonException e) {
            try {
                directory = new File(new File(".").getCanonicalPath());
            } catch (IOException e2) {
                throw new CIException("Could not determine current directory: " + e2, e2);
            }
        }

        File result = new File(directory, DUMP_FILE_NAME);

        if (mustExist) {
            return verifyFileExistence(result);
        } else {
            return result;
        }
    }

}
