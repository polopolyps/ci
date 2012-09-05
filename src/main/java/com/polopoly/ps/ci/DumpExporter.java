package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class DumpExporter extends AbstractDumpHandler {
    private ProcessUtil processUtil = new ProcessUtil();

    private File dumpFile;
    private boolean includePear = false;
    private boolean includeSolr = true;

    public DumpExporter(File dumpFile) throws CIException {
        this.dumpFile = Require.require(dumpFile);

        new DirectoryUtil().createDirectory(dumpFile.getParentFile());
    }

    public DumpExporter includePear() {
        includePear = true;

        // the solr directory is in pear.
        includeSolr = false;

        return this;
    }

    public void dump() throws CIException, RunningInMavenException {
        createWorkDirectories();

        try {
            if (processUtil.isIndexServerRunning()) {
                processUtil.pauseIndexing();
            }

            dumpDatabase();
            dumpIndexes();

            // TODO: Dump of poll and statistics

            if (includePear) {
                dumpPear();
            }

            packData();

            if (processUtil.isIndexServerRunning()) {
                processUtil.resumeIndexing();
            }
        } finally {
            try {
                new DirectoryUtil().deleteDirectory(getWorkDirectory());
            } catch (CIException e) {
                System.out.println("Could not clean up work directory after creating dump: " + e);
                e.printStackTrace(System.err);
            }
        }

        System.out.println("Dump created in " + getPackedFile().getAbsolutePath() + ".");

    }

    private void createWorkDirectories() throws CIException {
        File workDirectory = getWorkDirectory();

        if (workDirectory.exists()) {
            new Executor("rm -rf " + workDirectory.getAbsolutePath() + "/*").execute();
        } else {
            new DirectoryUtil().createDirectory(workDirectory);
        }

        new DirectoryUtil().createDirectory(getSqlDumpFile().getParentFile());
        new DirectoryUtil().createDirectory(getIndexWorkDirectory());

        if (includeSolr) {
            new DirectoryUtil().createDirectory(getSolrWorkDirectory());
        }
    }

    @Override
	protected File getWorkDirectory() {
        return new File(dumpFile.getParentFile(), "tmp");
    }

    public void dumpDatabase() throws CIException {
        System.out.println("Dumping mysql...");

        Configuration configuration = new Configuration();

        String command = "mysqldump --add-drop-table " + "--default-character-set=utf8 --skip-extended-insert"
				+ " --user=" + configuration.getDatabaseUser().getValue() + " --password="
				+ configuration.getDatabasePassword().getValue() + " --host="
				+ configuration.getDatabaseHost().getValue() + " --databases "
				+ configuration.getDatabaseSchema().getValue() + " -r " + getSqlDumpFile().getAbsolutePath();

        new Executor(command).execute();

        if (!getSqlDumpFile().exists()) {
            throw new CIException("Even though the dump command succeeded, the dump file " + getSqlDumpFile().getAbsolutePath()
                    + " did not exist.");
        }
    }

    public void dumpPear() throws CIException, RunningInMavenException {
        System.out.println("Copying pear...");

        new Executor("cp -r " + new PolopolyDirectories().getPearDirectory().getAbsolutePath() + " "
                + getWorkDirectory().getAbsolutePath()).execute();

        File logsWorkDirectory = new File(getWorkDirectory(), "pear/logs");

        if (logsWorkDirectory.exists()) {
            new Executor("rm -rf " + logsWorkDirectory.getAbsolutePath() + "/*").execute();
        }

        File tmpDirectory = new File(getWorkDirectory(), "pear/tmp");

        if (tmpDirectory.exists()) {
            new Executor("rm -rf " + tmpDirectory.getAbsolutePath() + "/*").execute();
        }

        System.out.println("Done.");
    }

    public void dumpIndexes() throws CIException, RunningInMavenException {
        System.out.println("Copying indexes...");

        dumpIndex("DefaultIndex");
        dumpIndex("PublicIndex");

        File solrDirectory = new PolopolyDirectories().getSolrDirectory();

        if (includeSolr && !new DirectoryUtil().isEmpty(solrDirectory)) {
            try {
                new Executor("cp -r " + solrDirectory.getAbsolutePath() + "/*" + " " + getSolrWorkDirectory().getAbsolutePath())
                        .execute();
            } catch (NoSuchFileException e) {
                // no solr work directory
            }
        }

        System.out.println("Done.");
    }

    private void dumpIndex(String indexName) throws CIException, RunningInMavenException {
        File indexDirectory = new File(new PolopolyDirectories().getIndexDirectory(), indexName);

        if (indexDirectory.exists()) {
            new Executor("cp -r " + indexDirectory.getAbsolutePath() + " " + getIndexWorkDirectory().getAbsolutePath()).execute();
        }
    }

    private void packData() throws CIException {
        System.out.println("Compressing the files...");

        File packedFile = getPackedFile();

        String filesToPack = "";

        for (File fileToPack : getWorkDirectory().listFiles()) {
            if (!fileToPack.getName().startsWith(".")) {
                filesToPack += " " + fileToPack.getName();
            }
        }

        new Executor("tar -czvf " + packedFile.getAbsolutePath() + filesToPack).setDirectory(getWorkDirectory()).execute();

        System.out.println("Done.");
    }

    private File getPackedFile() {
        return dumpFile;
    }
}
