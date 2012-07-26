package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;

public class IndexserverTool extends SimpleProcessTool {

    @Override
    protected void stopProcess() throws CIException {
        stopIndexServer();
    }

    @Override
    protected void startProcess() throws CIException {
        startIndexServer();
    }

    @Override
    protected void killProcess() throws CIException {
        killIndexServer();
    }

    @Override
    protected void reindex() {
        new ProcessUtil().reindexSolr();
    }

    @Override
    protected boolean isProcessRunning() throws CIException {
        return isIndexServerRunning();
    }

    @Override
    protected String getProcessName() {
        return "Indexserver";
    }

    @Override
    protected File getLogFile() {
        return new PolopolyDirectories().getIndexServerLogFile();
    }

    @Override
    protected boolean isThisToolsProcess(ProcessInfo process) {
        return process.isIndexServerProcess();
    }
}
