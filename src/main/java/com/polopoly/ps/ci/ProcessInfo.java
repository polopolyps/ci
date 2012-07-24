package com.polopoly.ps.ci;

import java.io.File;
import java.io.IOException;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.CannotConvertToCanonicalPathException;
import com.polopoly.ps.ci.exception.JbossBelongsToOtherProjectException;
import com.polopoly.ps.ci.exception.PolopolyBelongsToOtherProjectException;

public class ProcessInfo {
    private static final String JBOSS_PATH_END = "/lib/endorsed";
    private static final String JBOSS_PATH_START = "-Djava.endorsed.dirs=";

    private int pid;
    private String command;
	private Host host;

    public ProcessInfo(String psLine, Host host) {
        this.pid = getPid(psLine);
        this.command = psLine;
        this.host = host;
    }

    public int getPid() {
        return pid;
    }

    public String getCommand() {
        return command;
    }

    private static int getPid(String line) throws CIException {
        String[] elements = line.split(" ");

        int atElement = 0;

        for (String element : elements) {
            if (!element.equals("")) {
                if (atElement == 1) {
                    try {
                        return Integer.parseInt(element);
                    } catch (NumberFormatException e) {
                        throw new CIException("Could not find the PID in the ps output \"" + line + "\".");
                    }
                }

                atElement++;
            }
        }

        throw new CIException("Could not find the PID in the ps output \"" + line + "\".");
    }

    public boolean isIndexServerProcess() throws PolopolyBelongsToOtherProjectException {
        return command.contains("module.name=indexserver") && isPolopolyProcess();
    }

    public boolean isPolopolyProcess() throws PolopolyBelongsToOtherProjectException {
        // it would be nice to check for
        // process.contains("com.polopoly.pear.ApplicationFactory")
        // but that is truncated away if the classpath is long enough.
        boolean result = command.contains("-Dmodule.name=");

        if (result) {
            File polopolyDir = new Configuration().getPolopolyDirectory().getValue();

            String polopolyPath;

            try {
                polopolyPath = polopolyDir.getCanonicalPath();
            } catch (IOException e) {
                polopolyPath = polopolyDir.getAbsolutePath();

                if (polopolyPath.contains("./")) {
                    // can't check if the configured directory contains relative
                    // paths
                    return result;
                }
            }

            if (!command.contains(polopolyPath)) {
                throw new PolopolyBelongsToOtherProjectException();
            }
        }

        return result;

    }

    protected boolean isJ2EEContainerProcess() throws PolopolyBelongsToOtherProjectException {
        return command.contains("module.name=j2eecontainer") && isPolopolyProcess();
    }

    public boolean isJbossProcess() throws JbossBelongsToOtherProjectException {
        boolean result = command.contains("org.jboss.Main");

        if (result) {
            try {
                File jbossDir = new Configuration().getJbossDirectory().getNonExistingFile();

                String configuredJbossPath = getCanonicalPath(jbossDir);

                String runningJbossPath = extractJbossPath();

                if (!runningJbossPath.equals(configuredJbossPath)) {
                    throw new JbossBelongsToOtherProjectException("Running JBoss was " + runningJbossPath
                            + " whereas current project JBoss was " + configuredJbossPath);
                } else {
                    return true;
                }
            } catch (CannotConvertToCanonicalPathException e) {
                // can't verify
                return true;
            }
        }

        return result;
    }

    private String extractJbossPath() throws CannotConvertToCanonicalPathException {
        int i = command.indexOf(JBOSS_PATH_START);
        int j = command.indexOf(JBOSS_PATH_END, i + 1);

        if (i == -1 || j == -1) {
            throw new CIException("Could not find endorsed dirs in " + command);
        }

        return getCanonicalPath(new File(command.substring(i + JBOSS_PATH_START.length(), j)));
    }

    private String getCanonicalPath(File file) throws CannotConvertToCanonicalPathException {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            String result = file.getAbsolutePath();

            if (result.contains("./")) {
                throw new CannotConvertToCanonicalPathException();
            }

            return result;
        }
    }


	public boolean isTomcatProcess() {
		return command.contains("org.apache.catalina.startup.Bootstrap");
	}

	public boolean isJettyProcess() {
        /*
         * When ps -ef | grep jetty for maven 3.x, the output shows
         * org.codehaus.plexus.classworlds.launcher.Launcher jetty:run To make
         * sure it is backward compatibility for maven 2.x, using OR operator.
         */
        return command.contains("org.codehaus.classworlds.Launcher")
                || command.contains("org.codehaus.plexus.classworlds.launcher.Launcher");
    }

    protected void hardKill() throws CIException {
    	kill("-9");
    }

    protected void softKill() throws CIException {
    	kill("");
    }
    
    private void kill(String parameter) throws CIException {
        new Executor("kill " + parameter + " " + pid).setHost(host).execute();
    }

}
