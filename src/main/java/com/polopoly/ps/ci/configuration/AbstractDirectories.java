package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.exception.NoSuchFileException;

class AbstractDirectories extends AbstractConfiguration {

    protected File verifyDirectoryExistence(String directoryName) throws NoSuchFileException {
        File result = new File(directoryName);

        verifyDirectoryExistence(result);

        return result;
    }

    protected File verifyDirectoryExistence(File directory) throws NoSuchFileException {
        if (!directory.exists()) {
            throw new NoSuchFileException("Expected the directory " + directory.getAbsolutePath() + " to exist.");
        }

        if (!directory.isDirectory()) {
            throw new NoSuchFileException("Expected " + directory.getAbsolutePath() + " to be a directory.");
        }

        return directory;
    }

    protected File verifyFileExistence(File file) throws NoSuchFileException {
        if (!file.exists()) {
            throw new NoSuchFileException("Expected the file " + file.getAbsolutePath() + " to exist.");
        }

        if (!file.isFile()) {
            throw new NoSuchFileException("Expected " + file.getAbsolutePath() + " to be a file.");
        }

        return file;
    }
}
