package com.polopoly.ps.ci.tool;

import static com.polopoly.ps.ci.tool.ProcessOperation.JSTACK;
import static com.polopoly.ps.ci.tool.ProcessOperation.REINDEX;
import static com.polopoly.ps.ci.tool.ProcessOperation.RESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.START;
import static com.polopoly.ps.ci.tool.ProcessOperation.STOP;

import java.io.File;

import com.polopoly.ps.ci.JbossDeployment;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.SolrReindexer;
import com.polopoly.ps.ci.WarChecker;
import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.exception.CIException;

public class SolrTool extends ProcessTool<ProcessParameters> {

    @Override
    protected void stopProcess() throws CIException {
        new JbossDeployment().undeploySolr();
    }

    @Override
    protected void startProcess() throws CIException {
        new JbossDeployment().deploySolr();
    }

    @Override
    protected void killProcess() throws CIException {
        stopProcess();
    }

    @Override
    protected boolean isProcessRunning() throws CIException {
        return WarChecker.isJbossSolrRunning();
    }

    @Override
    protected void reindex() {
        new SolrReindexer().reindexSolr();
    }

    @Override
    protected String getProcessName() {
        return "SOLR";
    }

    @Override
    public ProcessParameters createParameters() {
        return new ProcessParameters(START, STOP, RESTART, REINDEX, JSTACK);
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
