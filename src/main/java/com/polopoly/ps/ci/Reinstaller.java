package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.CleanDumpDirectories;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.FileMonitorFailedException;
import com.polopoly.ps.ci.exception.NoSuchFileException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class Reinstaller extends JbossDeployment {
    private ProcessUtil processUtil = new ProcessUtil();

    private boolean restartIfOutOfMemoryInJboss = true;

    private boolean build;
    private boolean createDump;

    public void install() throws CIException {
        try {
			if (filesRequiredForQuickInstallExist()) {
			    System.out.println("Found clean dump. Doing a quick reinstall.");
			    quickInstall();
			} else {
			    System.out.println("Either the clean dump, the Polopoly EAR, SOLR WAR, statistics WAR, or the "
			            + "logger WAR did not exist, so going for a full reinstall.");
			    fullInstall();
			}
		} catch (RunningInMavenException e) {
			mavenInstall();
		}
    }

    private void mavenInstall() {
    	new Executor("mvn p:clean").setDirectory(new Configuration().getProjectHomeDirectory().getValue()).execute();
    	
    	new ProcessUtil().startPolopoly();
	}

	private void createDump() {
        System.out.println("Start database and pear dump...");

        try {
            new DumpExporter(new CleanDumpDirectories().getCleanDumpFile(false)).includePear().dump();
        } catch (Exception e) {
            System.err.println();
            System.err.println();
            System.err.println("Creating dump failed: " + e);
            e.printStackTrace(System.err);
        }
    }

    private boolean filesRequiredForQuickInstallExist() throws CIException, RunningInMavenException {
        try {
            new CleanDumpDirectories().getCleanDumpFile(true);
            new JbossDirectories().getPolopolyEarFile(true);
            new JbossDirectories().getSolrWarFile(true);
            new JbossDirectories().getStatisticsWarFile(true);
            new PolopolyDirectories().getConfigDirectory();

            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private void prepare() throws CIException {
        new MavenVersionSanityChecker().sanityCheckMavenVersion();

        processUtil.killPolopoly();

        processUtil.startJbossIfNotRunning();
    }

    /**
     * Don't call directly; use install() which selects either quick or full
     * install.
     */
    private void quickInstall() throws CIException, RunningInMavenException {
        prepare();

        if (isBuild()) {
            new Builder().installAll(true);
        }

        new ClientLibUpdater().updateClientLib(true);

        redeployCmServer();

        new DumpRestorer(new CleanDumpDirectories().getCleanDumpFile(true)).restore();

        new ProcessUtil().startPolopolyIfNotRunning();

        importProjectContent();
    }

    public void fullInstall() throws CIException, RunningInMavenException {
    	
    	String repoType = new Configuration().getRepositoryType().getValue();
    	
    	if ("git".equals(repoType)) {
    		new GitProjectCheckout().checkout();
    	}
    	else {
    		new SvnProjectCheckout().checkout();
    	}
    	
        new SchemaCreator().createSchemaIfRequired();

        if (!isJbossInstalled()) {
            System.out.println("JBoss had not been installed. Installing it.");

            new JBossReinstaller().reinstall();
        }

        if (!isPolopolyUnpacked()) {
            System.out.println("The Polopoly distribution had not been unpacked. Retrieving and unpacking it.");

            unpackPolopolyDistribution();
        }

        prepare();

        deleteDataFromPreviousInstallation();

        if (isBuild()) {
            new Builder().installAll(true);
        }

        new ClientLibUpdater().updateClientLib(true);

        // TODO: copy out indexes, poll, statistics, solr and copy back
        // after reinstall

        new ContainerClientlibs().copyContainerClientLibs();

        new Executor(new PolopolyDirectories().getPolopolyScript().getAbsolutePath() + " setup-installation").execute();

        deploy(new PolopolyDirectories().getCmServerEar());

        new Executor(new PolopolyDirectories().getPolopolyScript().getAbsolutePath() + " do-installation").execute();

        if (!new ProcessUtil().isJ2EEContainerRunning()) {
            throw new CIException("The j2eecontainer doesn't seem to run.");
        }

        deploy(new PolopolyDirectories().getSolrWar());
        // deploy statistics.war to jboss directory
        deploy(new PolopolyDirectories().getStatisticsWar());

        if (isCreateDump()) {
            createDump();
        }

        importProjectContent();

        System.out.println("Done.");
    }

    private void importProjectContent() throws RunningInMavenException {
        // TODO: stopping and starting is probably only necessary for lucene
        // indexing
        // (since the import can create the index definitions)
        new ProcessUtil().stopIndexServer();

        try {
            new ContentImporter().importContent();
        } finally {
            new ProcessUtil().startIndexServer();
        }

        sanityCheckSolr();

        // is this even needed?
        new SolrReindexer().reindexSolr();

        // TODO: index server reindexing if using lucene.
    }

    @Override
    public void deploy(File earOrWar) throws RunningInMavenException {
        try {
            super.deploy(earOrWar);
        } catch (FileMonitorFailedException e) {
            System.out.println("Log file deployment failed in JBoss: " + e);

            if (restartIfOutOfMemoryInJboss) {
                System.out.println("Stopping JBoss and restarting the install process again.");

                new ProcessUtil().stopJboss();
                restartIfOutOfMemoryInJboss = false;
                fullInstall();
            }
        }
    }

    private void unpackPolopolyDistribution() throws RunningInMavenException {
        try {
            File polopolyDirectory = new PolopolyDirectories().getPolopolyDirectory(false);

            if (polopolyDirectory.exists()) {
                if (!new DirectoryUtil().isEmpty(polopolyDirectory)) {
                    System.out.println("The Polopoly directory " + polopolyDirectory.getAbsolutePath()
                            + " existed but did not contain an unpacked Polopoly distribution. Clearing it.");

                    new DirectoryUtil().clearDirectory(polopolyDirectory);
                }
            } else {
                new DirectoryUtil().createDirectory(polopolyDirectory);
            }

            File distribution = new PolopolyDistribution().getPolopolyDistribution();

            new Executor("unzip " + distribution.getAbsolutePath()).setDirectory(polopolyDirectory).execute();

            try {
                new Executor("chmod a+x " + new PolopolyDirectories().getPolopolyScript().getAbsolutePath()).execute();
                new Executor("chmod a+x " + new PolopolyDirectories().getAntExecutable().getAbsolutePath()).execute();
            } catch (NoSuchFileException e) {
                throw new CIException("There seems to be something wrong with the Polopoly distribution "
                        + distribution.getAbsoluteFile() + ": " + e.getMessage());
            }
        } catch (CIException e) {
            throw new CIException("While unpacking Polopoly distribution: " + e.getMessage(), e);
        }
    }

    private boolean isJbossInstalled() {
        try {
            new JbossDirectories().getJbossDirectory();
            new JbossDirectories().getJbossDeployDirectory();

            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private boolean isPolopolyUnpacked() throws RunningInMavenException {
        try {
            new PolopolyDirectories().getPolopolyDirectory();
            new PolopolyDirectories().getInstallScript();

            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private void deleteDataFromPreviousInstallation() throws RunningInMavenException {
        try {
            File polopolyEar = new JbossDirectories().getPolopolyEarFile(true);

            LogFileMonitor monitor = createUndeployMonitor(polopolyEar);

            new Executor("rm " + polopolyEar.getAbsolutePath()).execute();

            monitor.monitor();
        } catch (NoSuchFileException e) {
            // ear not deployed. fine.
        }

        try {
            File solrWar = new JbossDirectories().getSolrWarFile(true);
            LogFileMonitor monitor = createUndeployMonitor(solrWar);

            new Executor("rm " + solrWar.getAbsolutePath()).execute();

            monitor.monitor();
        } catch (NoSuchFileException e) {
            // war not deployed. fine.
        }

        try {
            File statisticsWar = new JbossDirectories().getStatisticsWarFile(true);
            LogFileMonitor monitor = createUndeployMonitor(statisticsWar);

            new Executor("rm " + statisticsWar.getAbsolutePath()).execute();

            monitor.monitor();
        } catch (NoSuchFileException e) {
            // war not deployed. fine.
        }

        try {
            new DirectoryUtil().deleteDirectory(new PolopolyDirectories().getPearDirectory());
        } catch (NoSuchFileException e) {
            // no pear directory. fine.
        }
    }

    public void setBuild(boolean build) {
        this.build = build;
    }

    public boolean isBuild() {
        return build;
    }

    public void setCreateDump(boolean createDump) {
        this.createDump = createDump;
    }

    public boolean isCreateDump() {
        return createDump;
    }

}
