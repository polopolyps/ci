package com.polopoly.ps.ci.tool;

import static com.polopoly.ps.ci.tool.ProcessOperation.JSTACK;
import static com.polopoly.ps.ci.tool.ProcessOperation.KILL;
import static com.polopoly.ps.ci.tool.ProcessOperation.RESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.START;
import static com.polopoly.ps.ci.tool.ProcessOperation.STOP;
import static com.polopoly.ps.ci.tool.ProcessOperation.TAIL;

import java.io.File;

import com.polopoly.ps.ci.JettyController;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.PolopolyBelongsToOtherProjectException;

public class PolopolyTool extends ProcessTool<ProcessParameters> {
    @Override
    public ProcessParameters createParameters() {
        return new ProcessParameters(START, STOP, RESTART, TAIL, JSTACK, KILL);
    }

    @Override
    protected void stopProcess() throws CIException {
        if (new JettyController().isRunning()) {
            System.out.println("Jetty was running. Killing it.");

            new JettyController().kill();
        }

        boolean isPolopolyRunning;
        try {
            isPolopolyRunning = isPolopolyRunning();
        } catch (PolopolyBelongsToOtherProjectException e) {
            System.out.println(e.getMessage());

            isPolopolyRunning = true;
        }

        if (isPolopolyRunning) {
            stopPolopoly();
        } else {
            System.out.println("Polopoly was not running.");
        }
    }

    @Override
    protected void startProcess() throws CIException {
        startJbossIfNotRunning();

        startPolopolyIfNotRunning();

        if (!new Configuration().isRunInNitro()) {
        	if (!isIndexServerRunning()) {
        		throw new CIException("The index server process doesn't seem to be running after a start.");
        	}

        	if (!isJ2EEContainerRunning()) {
        		throw new CIException("The J2EE container doesn't seem to be running after a start.");
        	}
        }
    }

    @Override
    protected void killProcess() throws CIException {
        killPolopoly();
    }

    @Override
    protected boolean isProcessRunning() throws CIException {
        return isPolopolyRunning();
    }

    @Override
    protected String getProcessName() {
        return "Polopoly";
    }

    @Override
    protected File getLogFile() {
        return new PolopolyDirectories().getLogFile();
    }

    @Override
    protected boolean isThisToolsProcess(ProcessInfo process) {
        return process.isPolopolyProcess();
    }
}
