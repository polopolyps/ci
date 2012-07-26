package com.polopoly.ps.ci;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class SolrReindexer {
    private Configuration configuration = new Configuration();

    public void reindexSolr() throws CIException, RunningInMavenException {
        System.out.println("Starting SOLR reindex...");

        LogFileMonitor monitor = createLogFileMonitor();

        new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath() + "/bin/polopoly tools reindex-solr")
                .execute();

        monitor.monitor();
    }

    private LogFileMonitor createLogFileMonitor() throws CIException, RunningInMavenException {
        LogFileMonitor monitor = new LogFileMonitor(new PolopolyDirectories().getIndexServerLogFile());

        monitor.addSuccessString("Reindexing of solr index 'public' is now complete");
        monitor.addSuccessString("Reindexing of solr index 'internal' is now complete");
        monitor.addFailureString("Indexing batch failed");
        monitor.addFailureString(ClassNotFoundException.class.getName());
        monitor.addFailureString(NoClassDefFoundError.class.getName());
        monitor.setTimeout(configuration.getSolrReindexingTimeoutSeconds().getValue() * 1000);

        return monitor;
    }

}
