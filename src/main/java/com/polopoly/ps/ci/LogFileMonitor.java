package com.polopoly.ps.ci;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.FileMonitorFailedException;
import com.polopoly.ps.ci.exception.FileMonitorTimeoutException;

public class LogFileMonitor {
    private static final int MAX_LINES = 50;
    private File logFile;
    private long filePointer;

    private Set<String> successStrings = new HashSet<String>();
    private Set<String> failureStrings = new HashSet<String>();
    private int timeoutInMs = 30000;
    private long originalLength;

    private OutputFilter outputFilter = OutputFilter.ALL;

    public LogFileMonitor(File logFile) {
        this(logFile, logFile.length());
    }

    public LogFileMonitor(File logFile, long filePointer) {
        this.logFile = Require.require(logFile);
        this.filePointer = filePointer;

        originalLength = logFile.length();
    }

    public void addSuccessString(String successString) {
        successStrings.add(successString);
    }

    public void addFailureString(String failureString) {
        failureStrings.add(failureString);
    }

    public void setTimeout(int timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }

    public String monitor() throws CIException {
        System.out.println("Waiting for success message in " + logFile.getAbsolutePath() + "...");

        try {
            long timer = 0;

            RandomAccessFile randomAccessFile = new RandomAccessFile(logFile, "r");
            randomAccessFile.seek(filePointer);

            while (true) {
                if (originalLength > logFile.length()) {
                    System.out.println(logFile.getName() + " was cleared. Restarting scan from beginning.");
                    randomAccessFile.seek(0);
                    originalLength = logFile.length();
                }

                try {
                    String lastSuccess = "";
                    String line;

                    int lineCount = 0;

                    while ((line = randomAccessFile.readLine()) != null) {
                        filePointer = randomAccessFile.getFilePointer();

                        if (outputFilter.shouldBePrinted(line)) {
                            if (++lineCount == MAX_LINES) {
                                System.out
                                        .println("This is your CI script: There is a lot of activity in the log file. Skipping a few lines...");
                            } else if (lineCount < MAX_LINES) {
                                System.out.println(line);
                            }
                        }

                        for (String successString : successStrings) {
                            if (line.contains(successString)) {
                                successStrings.remove(successString);

                                if (lineCount >= MAX_LINES) {
                                    System.out.println(line);
                                }

                                lastSuccess = line;

                                break;
                            }
                        }

                        for (String failureString : failureStrings) {
                            if (line.contains(failureString)) {
                                throw new FileMonitorFailedException("Log contained \"" + line
                                        + "\" which would indicate failure.");
                            }
                        }
                    }

                    if (successStrings.isEmpty()) {
                        System.out.println("Found all success messages for the operation in " + logFile.getName() + ".");

                        randomAccessFile.close();

                        return lastSuccess;
                    }

                    if (timer >= timeoutInMs) {
                        throw new FileMonitorTimeoutException("Did not find messages " + successStrings
                                + " indicating success in " + logFile + " after waiting " + (timeoutInMs / 1000) + " seconds");
                    }

                    Thread.sleep(500);
                    timer += 500;
                } catch (InterruptedException e) {
                    // swallow
                }
            }
        } catch (FileNotFoundException e) {
            throw new CIException("Could not find the log file: " + e);
        } catch (IOException e) {
            throw new CIException("Could not find the log file: " + e);
        }
    }

    public void setOutputFilter(OutputFilter outputFilter) {
        this.outputFilter = Require.require(outputFilter);
    }
}
