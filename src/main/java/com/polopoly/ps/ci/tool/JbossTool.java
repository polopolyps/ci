package com.polopoly.ps.ci.tool;

import static com.polopoly.ps.ci.tool.ProcessOperation.JSTACK;
import static com.polopoly.ps.ci.tool.ProcessOperation.KILL;
import static com.polopoly.ps.ci.tool.ProcessOperation.REDEPLOY;
import static com.polopoly.ps.ci.tool.ProcessOperation.RESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.START;
import static com.polopoly.ps.ci.tool.ProcessOperation.STOP;
import static com.polopoly.ps.ci.tool.ProcessOperation.TAIL;

import java.io.File;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.JBossReinstaller;
import com.polopoly.ps.ci.JbossDeployment;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.PolopolyBelongsToOtherProjectException;
import com.polopoly.ps.ci.exception.PolopolyPartiallyRunningException;

public class JbossTool extends ProcessTool<ProcessParameters> implements Tool<ProcessParameters> {

    @Override
    protected void stopProcess() throws CIException {
        if (canPolopolyBeStopped()) {
            new ProcessUtil().stopPolopoly();
        }

        stopJboss();
    }

    @Override
    protected void startProcess() throws CIException {
        startJbossIfNotRunning();
    }

    @Override
    protected void killProcess() throws CIException {
        if (canPolopolyBeStopped()) {
            new ProcessUtil().killPolopoly();
        }

        stopJboss();
    }

    protected boolean canPolopolyBeStopped() {
        boolean isPolopolyRunning;

        try {
            isPolopolyRunning = new ProcessUtil().isPolopolyRunning();
        } catch (PolopolyBelongsToOtherProjectException e) {
            System.out.println(e.getMessage());

            isPolopolyRunning = true;
        } catch (PolopolyPartiallyRunningException e) {
            System.out.println(e.getMessage());

            isPolopolyRunning = true;
        }

        return isPolopolyRunning;
    }

    @Override
    protected void redeploy() {
        new JbossDeployment().redeployCmServer();
        new JbossDeployment().undeployAllWars();
        new JbossDeployment().deployAllWars();
    }

    @Override
    protected void reinstall() {
        stopJboss();
        new JBossReinstaller().reinstall();
    }

    @Override
    protected boolean isProcessRunning() throws CIException {
        return isJbossRunning();
    }

    @Override
    protected String getProcessName() {
        return "JBoss";
    }

    @Override
    public ProcessParameters createParameters() {
        return new ProcessParameters(START, STOP, RESTART, TAIL, JSTACK, KILL, REDEPLOY);
    }

    @Override
    protected File getLogFile() {
        return new JbossDirectories().getJbossLog();
    }

    @Override
    protected boolean isThisToolsProcess(ProcessInfo process) {
        return process.isJbossProcess();
    }
}
