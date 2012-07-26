package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.exception.NoDirectoryFoundException;

public class LocateDirectoryContaining {

    public File locateDirectoryContaining(File directory, String containedFile) throws NoDirectoryFoundException {
        File result = null;

        for (File subdirectory : directory.listFiles()) {
            if (subdirectory.isDirectory()) {
                if (new File(subdirectory, containedFile).exists()) {
                    if (result != null) {
                        throw new NoDirectoryFoundException("There are at least two directories under "
                                + directory.getAbsolutePath() + " that contains a directory called \"" + containedFile + "\": "
                                + result + " and " + subdirectory + ".");
                    }

                    result = subdirectory;
                }
            }
        }

        if (result != null) {
            return result;
        }

        throw new NoDirectoryFoundException("There is no directory under " + directory.getAbsolutePath()
                + " that contains a directory called \"" + containedFile + "\".");
    }

}
