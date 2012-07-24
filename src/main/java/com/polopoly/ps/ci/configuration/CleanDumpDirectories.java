package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.exception.CIException;

public class CleanDumpDirectories extends AbstractDirectories {

    public File getCleanDumpFile(boolean mustExist) throws CIException {
        return new Configuration().getPolopolyCleanDumpFile().getValue(mustExist);
    }

}
