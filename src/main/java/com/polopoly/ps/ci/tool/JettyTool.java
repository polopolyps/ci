package com.polopoly.ps.ci.tool;

import java.io.File;
import java.util.Set;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.JettyController;
import com.polopoly.ps.ci.NonDuplicatingClasspathBuilder;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.JettyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoDirectoryFoundException;

public class JettyTool extends SimpleProcessTool implements Tool<ProcessParameters> {

    @Override
    protected void stopProcess() throws CIException {
        killProcess();
    }

    @Override
    protected void startProcess() throws CIException {
        startJbossIfNotRunning();
        startPolopolyIfNotRunning();

        JettyController jettyController = new JettyController();

        if (isProcessRunning()) {
            System.out.println("Jetty was already running. Killing the existing process...");
            jettyController.kill();
        }

        jettyController.start();

        checkForDuplicateJars();
    }

    protected void checkForDuplicateJars() {
        try {
            NonDuplicatingClasspathBuilder classpath = new NonDuplicatingClasspathBuilder();

            File webappDir = new JettyDirectories().getBuiltWebappDirectory();

            File lib = new File(webappDir, "WEB-INF/lib");

            if (!lib.exists()) {
                System.err.println("Could not find WEB-INF/lib directory under " + webappDir + ".");
                return;
            }

            classpath.addJarDirectory(lib);

            Set<File> duplicates = classpath.getDuplicates();

            if (!duplicates.isEmpty()) {
                System.err.println("The following JARs are duplicated in WEB-INF/lib: " + toShortNames(duplicates));
            }
        } catch (NoDirectoryFoundException e) {
            System.err.println("Could not find target directory: " + e.getMessage());
        }
    }

    private String toShortNames(Set<File> files) {
        StringBuffer result = new StringBuffer(100);

        for (File file : files) {
            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(file.getName());
        }

        return result.toString();
    }

    @Override
    protected void killProcess() throws CIException {
        new JettyController().kill();
    }

    /**
     * Checks if the process is running and if the port configured in the
     * property guiserver.port is available.
     * 
     * @return true if the process is running or if the port is already in use.
     */
    @Override
    protected boolean isProcessRunning() throws CIException {
        JettyController jettyController = new JettyController();

        boolean isRunning = jettyController.isRunning();

        if (!isRunning && jettyController.isPortAlreadyInUse()) {
            throw new CIException("The Jetty process was not found but the Jetty port ("
                    + new Configuration().getGuiServerPort().getValue() + ") was already in use.");
        }

        return isRunning;
    }

    @Override
    protected String getProcessName() {
        return "Jetty";
    }

    @Override
    protected File getLogFile() {
        return new JettyController().getLog();
    }

    @Override
    protected boolean isThisToolsProcess(ProcessInfo process) {
        return process.isJettyProcess();
    }
}
