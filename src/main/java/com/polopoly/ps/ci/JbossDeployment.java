package com.polopoly.ps.ci;

import java.io.File;
import java.io.IOException;

import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class JbossDeployment {
    public void redeployCmServer() throws CIException {
        System.out.println("Redeploying Polopoly EAR in Jboss...");

        File tempDirectory = createTemporaryDeploymentDir();
        File deployDirectory = new JbossDirectories().getJbossPolopolyDeployDirectory();

        File polopolyEar = new JbossDirectories().getPolopolyEarFile(true);

        LogFileMonitor monitor = createUndeployMonitor(polopolyEar);

        new Executor("mv " + polopolyEar.getAbsolutePath() + " " + tempDirectory.getAbsolutePath()).execute();

        monitor.monitor();

        monitor = createDeployMonitor(polopolyEar);

        new Executor("rm -rf " + new JbossDirectories().getJbossWorkDirectory() + "/jboss.web/localhost/solr").execute();
        new Executor("mv " + tempDirectory.getAbsolutePath() + "/" + polopolyEar.getName() + " " + deployDirectory).execute();

        monitor.monitor();

        sanityCheckSolr();

        new Executor("rmdir " + tempDirectory.getAbsolutePath()).execute();

        System.out.println("Done.");
    }

    public void undeploySolr() throws CIException {
        undeployWars(new JbossDirectories().getSolrWarFile(true));
    }

    public void undeployStatistics() throws CIException {
        undeployWars(new JbossDirectories().getStatisticsWarFile(true));
    }

    /**
     * undeploy all war from jboss
     * 
     * @throws CIException
     */
    public void undeployAllWars() throws CIException {
        undeployWars(new JbossDirectories().getSolrWarFile(true), new JbossDirectories().getStatisticsWarFile(true));
    }

    /**
     * low level method to undeploy wars from jboss
     * 
     * @param wars
     *            files to undeploy
     * @throws CIException
     */
    protected void undeployWars(File... wars) throws CIException {

        LogFileMonitor monitor = createUndeployMonitor(wars);
        File tempDirectory = createTemporaryDeploymentDir();
        for (File war : wars) {
            System.out.println("Undeploying " + war.getName() + " in JBoss...");

            new Executor("mv " + war.getAbsolutePath() + " " + tempDirectory.getAbsolutePath()).execute();

        }
        monitor.monitor();
    }

    public void deploySolr() throws CIException {
        System.out.println("Deploying SOLR in JBoss...");
        File solrWar = new JbossDirectories().getSolrWarFile(false);
        LogFileMonitor monitor = createDeployMonitor(solrWar);

        File tempDirectory = getExistingTemporaryDeploymentDir();
        File deployDirectory = new JbossDirectories().getJbossPolopolyDeployDirectory();

        new Executor("rm -rf " + new JbossDirectories().getJbossWorkDirectory() + "/jboss.web/localhost/solr").execute();
        new Executor("mv " + tempDirectory.getAbsolutePath() + "/" + solrWar.getName() + " " + deployDirectory).execute();

        monitor.monitor();

        sanityCheckSolr();

        new Executor("rmdir " + tempDirectory.getAbsolutePath()).execute();

        System.out.println("Done.");
    }

    // TODO: deploySolr and deployStatistics methods violate DRY
    // need to refactor
    public void deployStatistics() throws CIException {
        System.out.println("Deploying statistics in JBoss...");
        File statistics = new JbossDirectories().getStatisticsWarFile(false);
        LogFileMonitor monitor = createDeployMonitor(statistics);

        File tempDirectory = getExistingTemporaryDeploymentDir();
        File deployDirectory = new JbossDirectories().getJbossPolopolyDeployDirectory();

        new Executor("rm -rf " + new JbossDirectories().getJbossWorkDirectory() + "/jboss.web/localhost/statistics").execute();
        new Executor("mv " + tempDirectory.getAbsolutePath() + "/" + statistics.getName() + " " + deployDirectory).execute();

        monitor.monitor();

        if (!WarChecker.isJbossStatisticsRunning()) {
            throw new CIException("statistics not responding");
        }
        new Executor("rmdir " + tempDirectory.getAbsolutePath()).execute();

        System.out.println("Done.");
    }

    public void deployAllWars() {
        deploySolr();
        deployStatistics();
    }

    private File createTemporaryDeploymentDir() throws CIException {
        File tempDirectory = getTemporaryDeploymentDir();

        if (tempDirectory.exists()) {
            new DirectoryUtil().clearDirectory(tempDirectory);
        } else {
            new Executor("mkdir " + tempDirectory.getAbsolutePath()).execute();
        }

        return tempDirectory;
    }

    private File getExistingTemporaryDeploymentDir() throws CIException {
        File result = getTemporaryDeploymentDir();

        if (!result.exists()) {
            throw new CIException("The temporary directory " + result.getAbsolutePath()
                    + "used for undeployment did not exist. It should have been created by the undeploy methods.");
        }

        return result;
    }

    private File getTemporaryDeploymentDir() throws CIException {
        File deployDirectory = new JbossDirectories().getJbossDeployDirectory();

        File tempDirectory = new File(deployDirectory.getParentFile(), "temp_deploy");

        return tempDirectory;
    }

    protected LogFileMonitor createDeployMonitor(File... deployedFiles) throws CIException {
        LogFileMonitor monitor = new LogFileMonitor(new JbossDirectories().getJbossLog());

        configureDeployMonitor(monitor, deployedFiles);

        return monitor;
    }

    protected void configureDeployMonitor(LogFileMonitor monitor, File... deployedFiles) throws CIException {
        monitor.setOutputFilter(OutputFilter.NO_DEBUG);

        for (File deployedFile : deployedFiles) {
            monitor.addSuccessString("Deployed package: file:" + getCanonicalPath(deployedFile));
        }

        monitor.addFailureString("OutOfMemory");
    }

    private String getCanonicalPath(File deployedFile) {
        try {
            return deployedFile.getCanonicalPath();
        } catch (IOException e) {
            return deployedFile.getAbsolutePath();
        }
    }

    protected LogFileMonitor createUndeployMonitor(File... deployedFiles) throws CIException {
        LogFileMonitor monitor = new LogFileMonitor(new JbossDirectories().getJbossLog());

        monitor.setOutputFilter(OutputFilter.NO_DEBUG);

        for (File deployedFile : deployedFiles) {
            monitor.addSuccessString("Undeployed file:" + getCanonicalPath(deployedFile));
        }

        monitor.addFailureString("OutOfMemory");

        return monitor;
    }

    protected void sanityCheckSolr() throws CIException {
        if (!WarChecker.isJbossSolrRunning()) {
            throw new CIException("SOLR was not responding");
        }
    }

    public void deploy(File earOrWar) throws RunningInMavenException {
        File deployDir = new JbossDirectories().getJbossPolopolyDeployDirectory();

        LogFileMonitor monitor = createDeployMonitor(new File(deployDir, earOrWar.getName()));

        new Executor("cp " + earOrWar.getAbsolutePath() + " " + deployDir).execute();

        monitor.monitor();
    }
}
