package com.polopoly.ps.ci.tool;

import static com.polopoly.ps.ci.tool.ProcessOperation.HARDRESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.JSTACK;
import static com.polopoly.ps.ci.tool.ProcessOperation.KILL;
import static com.polopoly.ps.ci.tool.ProcessOperation.LOG;
import static com.polopoly.ps.ci.tool.ProcessOperation.REDEPLOY;
import static com.polopoly.ps.ci.tool.ProcessOperation.REINDEX;
import static com.polopoly.ps.ci.tool.ProcessOperation.REINSTALL;
import static com.polopoly.ps.ci.tool.ProcessOperation.RESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.RUNNING;
import static com.polopoly.ps.ci.tool.ProcessOperation.START;
import static com.polopoly.ps.ci.tool.ProcessOperation.STOP;
import static com.polopoly.ps.ci.tool.ProcessOperation.TAIL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.Executor;
import com.polopoly.ps.ci.ProcessInfo;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public abstract class ProcessTool<P extends ProcessParameters> extends ProcessUtil
        implements Tool<P>, DoesNotRequireRunningPolopoly {
    
    private static final int DEFAULT_OFFSET = 300;
    private static final int DEFAULT_SLEEPTIME = 100;
    
    @Override
    public void execute(PolopolyContext context, P parameters) throws FatalToolException {
        ProcessOperation operation = parameters.getOperation();

        if (operation == STOP) {
            stopProcess();
        } else if (operation == START) {
            startProcess();
        } else if (operation == RESTART) {
            stopProcess();
            startProcess();
        } else if (operation == HARDRESTART) {
            killProcess();
            startProcess();
        } else if (operation == KILL) {
            killProcess();
        } else if (operation == LOG) {
            log();
        } else if (operation == RUNNING) {
            if (isProcessRunning()) {
                System.out.println(getProcessName() + " is running.");
            } else {
                System.out.println(getProcessName() + " is not running.");
            }
        } else if (operation == JSTACK) {
            jstack();
        } else if (operation == TAIL) {
            tail();
        } else if (operation == REDEPLOY) {
            redeploy();
        } else if (operation == REINSTALL) {
            reinstall();
        } else if (operation == REINDEX) {
            reindex();
        } else {
            throw new FatalToolException("Unknown operation " + operation + ".");
        }
    }

    protected void jstack() {
        for (ProcessInfo process : getProcessList()) {
            if (isThisToolsProcess(process)) {
                new Executor("jstack " + process.getPid()).execute();
            }
        }
    }

    protected abstract boolean isThisToolsProcess(ProcessInfo process);

    protected void reinstall() {
        throw new FatalToolException("This tool does not support this operation.");
    }

    protected void redeploy() {
        throw new FatalToolException("This tool does not support this operation.");
    }

    protected void reindex() {
        throw new FatalToolException("This tool does not support this operation.");
    }

    /**
     * The point of this operation is to be able to do e.g. less `ci indexserver
     * log`.
     */
    protected void log() {
        try {
            System.out.println(getLogFile().getCanonicalPath());
        } catch (IOException e) {
            System.out.println(getLogFile().getAbsolutePath());
        }
    }

    protected void tail() {
        File logFile = getLogFile();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(logFile, "r");
            long originalLength = logFile.length();
            randomAccessFile.seek(originalLength - DEFAULT_OFFSET);

            randomAccessFile.readLine();

            while (true) {
                if (originalLength > logFile.length()) {
                    System.out.println(logFile.getName() + " was cleared. Restarting scan from beginning.");
                    randomAccessFile.seek(0);
                }

                while (true) {
                    String line;

                    while ((line = randomAccessFile.readLine()) != null) {
                        System.out.println(line);
                    }

                    Thread.sleep(DEFAULT_SLEEPTIME);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find log: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading from log: " + e);
        } catch (InterruptedException e) {
            System.out.println("Interrupted.");
        }
    }

    protected abstract File getLogFile();

    @Override
    public abstract P createParameters();

    @Override
    public String getHelp() {
        return "Starts and stops " + getProcessName();
    }

    protected abstract void stopProcess() throws CIException;

    protected abstract void startProcess() throws CIException;

    protected abstract void killProcess() throws CIException;

    protected abstract boolean isProcessRunning() throws CIException;

    protected abstract String getProcessName();

}
