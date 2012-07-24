package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class DatabaseRestorer {
    private File dumpFile;
    private Configuration configuration = new Configuration();

    public DatabaseRestorer(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public void restore() throws CIException {
        System.out.println("Restoring database state from after reinstall from " + dumpFile + "...");

        String callMySql = "mysql --default-character-set=utf8 --user=" + configuration.getDatabaseUser().getValue()
				+ " --password=" + configuration.getDatabasePassword().getValue() + " --host="
				+ configuration.getDatabaseHost().getValue() + " " + configuration.getDatabaseSchema().getValue()
				+ " < " + dumpFile.getAbsolutePath();
        new Executor(callMySql).execute();

        System.out.println("Done.");
    }
}