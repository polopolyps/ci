package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class DumpRestorer extends AbstractDumpHandler {
    private File dumpFile;
    private DirectoryUtil directoryUtil = new DirectoryUtil();

    public DumpRestorer(File dumpFile) throws CIException {
        this.dumpFile = dumpFile;

        if (!dumpFile.exists()) {
            throw new CIException("File " + dumpFile.getAbsolutePath() + " does not exist.");
        }
    }

    public void restore() throws CIException, RunningInMavenException {
        if (!getWorkDirectory().exists()) {
            new DirectoryUtil().createDirectory(getWorkDirectory());
        }

        try {
            unpackDump();

            boolean polopolyWasRunning = new ProcessUtil().killPolopoly() > 0;

            restoreDatabase();

            restoreIndexes();

            restoreSolr();

            // TODO: Dump and restore of poll and statistics

            removeCacheDirectories();

            if (polopolyWasRunning) {
                new ProcessUtil().startJbossIfNotRunning();

                new ProcessUtil().startPolopoly();
            }
        } catch (CIException e) {
            throw new CIException("While restoring dump: " + e.getMessage(), e);
        } finally {
            new Executor("rm -rf " + getWorkDirectory().getAbsolutePath()).execute();
        }

        System.out.println("Done restoring.");
    }

    public void unpackDump() throws CIException {
        String dumpPath = dumpFile.getAbsolutePath();
        System.out.println("Unpacking dump " + dumpPath + "...");
        new Executor("tar -xzvf " + dumpPath).setDirectory(getWorkDirectory()).execute();

        System.out.println("Done extracting.");
    }

    private void restoreDatabase() throws CIException {
        new DatabaseRestorer(getSqlDumpFile()).restore();
    }

    private void restoreIndexes() throws CIException, RunningInMavenException {
        System.out.println("Restoring indexes...");

        restoreIndex("DefaultIndex");
        restoreIndex("PublicIndex");

        System.out.println("Done.");
    }

    private void restoreIndex(String indexName) throws CIException, RunningInMavenException {
        File indexDirectory = new File(new PolopolyDirectories().getIndexDirectory(), indexName);

        File indexWorkDirectory = new File(getIndexWorkDirectory(), indexName);

        if (indexWorkDirectory.exists() && !isEmpty(indexWorkDirectory)) {
            if (!indexDirectory.exists()) {
                new DirectoryUtil().createDirectory(indexDirectory);
            } else {
                directoryUtil.clearDirectory(indexDirectory);
            }

            new Executor("mv " + indexWorkDirectory.getAbsolutePath() + "/* " + indexDirectory).execute();
        }
    }

    /**
     * Should be moved to a utility class.
     */
    private boolean isEmpty(File directory) {
        for (File file : directory.listFiles()) {
            if (!file.getName().startsWith(".")) {
                return false;
            }
        }

        return true;
    }

    private void restoreSolr() throws CIException, RunningInMavenException {
        if (!getSolrWorkDirectory().exists()) {
            return;
        }

        boolean jbossRunning = new ProcessUtil().isJbossRunning();

        if (jbossRunning) {
            new JbossDeployment().undeploySolr();
        }

        System.out.println("Restoring solr...");

        directoryUtil.clearDirectory(new PolopolyDirectories().getSolrDirectory());

        new Executor("cp -r " + getSolrWorkDirectory().getAbsolutePath() + "/* "
                + new PolopolyDirectories().getSolrDirectory().getAbsolutePath()).execute();

        if (jbossRunning) {
            new JbossDeployment().deploySolr();
        }

        System.out.println("Done.");
    }

    private void removeCacheDirectories() throws CIException {
        System.out.println("Removing cache directories...");

        File projectHomePath = new Configuration().getProjectHomeDirectory().getValue();

        for (File module : projectHomePath.listFiles()) {
            if (module.isDirectory()) {
                File cachedir = new File(module, "src/main/webapp/WEB-INF/contentcache");

                if (cachedir.exists()) {
                    new DirectoryUtil().deleteDirectory(cachedir);
                }

                cachedir = new File(module, "src/main/webapp/WEB-INF/filescache");

                if (cachedir.exists()) {
                    new DirectoryUtil().deleteDirectory(cachedir);
                }
            }
        }

        System.out.println("Done.");
    }

    protected File getWorkDirectory() {
        return new File("tmp");
    }

}
