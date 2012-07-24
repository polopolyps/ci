package com.polopoly.ps.ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.JbossDirectories;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.JbossBelongsToOtherProjectException;
import com.polopoly.ps.ci.exception.PolopolyBelongsToOtherProjectException;
import com.polopoly.ps.ci.exception.PolopolyPartiallyRunningException;
import com.polopoly.ps.ci.exception.RunningInMavenException;
import com.polopoly.ps.ci.exception.UrlMonitorFailedException;

public class ProcessUtil {
	private static final long INDEXSERVER_WAIT_TIME = 10000;
	private Configuration configuration = new Configuration();

	public boolean isRunning(Process process) {
		try {
			process.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

	public boolean isPolopolyRunning() throws CIException {
		if (new Configuration().isRunInNitro()) {
			return isJbossRunning();
		}

		boolean modulesRunning = false;

		for (ProcessInfo process : getProcessList()) {
			if (process.isJ2EEContainerProcess()) {
				return true;
			} else if (process.isPolopolyProcess()) {
				modulesRunning = true;
			}
		}

		if (modulesRunning) {
			throw new PolopolyPartiallyRunningException(
					"There were some Polopoly processes running but not the j2eecontainer.");
		}

		return false;
	}

	/**
	 * Kills all Polopoly processes as well as Jetty, but not Tomcat
	 */
	public int killPolopoly() throws CIException {
		if (new Configuration().isRunInNitro()) {
			return 0;
		}

		System.out.println("Killing Polopoly and Jetty...");
		int killedProcesses = 0;

		for (ProcessInfo process : getProcessList()) {
			boolean isPolopolyProcess;

			try {
				isPolopolyProcess = process.isPolopolyProcess();
			} catch (PolopolyBelongsToOtherProjectException e) {
				isPolopolyProcess = true;
			}

			if (isPolopolyProcess) {
				System.out.println("Killing Polopoly process (" + shorten(process.getCommand()) + ").");
				process.hardKill();
				killedProcesses++;
			}

			if (process.isJettyProcess()) {
				System.out.println("Killing Jetty process (" + shorten(process.getCommand()) + ").");
				process.hardKill();
				killedProcesses++;
			}
		}

		System.out.println("Killed " + killedProcesses + " process(es).");

		return killedProcesses;
	}

	private String shorten(String string) {
		if (string.length() > 200) {
			return string.substring(0, 200);
		} else {
			return string;
		}
	}

	protected List<ProcessInfo> getProcessList() throws CIException {
		return getProcessList(null);
	}

	protected List<ProcessInfo> getProcessList(Host host) throws CIException {
		String ps = new Executor("ps -ef").setHost(host).setOutputOnConsole(false).execute();

		List<ProcessInfo> result = new ArrayList<ProcessInfo>();

		boolean first = true;

		for (String line : ps.split("\n")) {
			if (first) {
				first = false;
			} else {
				result.add(new ProcessInfo(line, host));
			}
		}

		return result;
	}

	public boolean isIndexServerRunning() throws CIException {
		int indexServerProcesses = 0;

		for (ProcessInfo process : getProcessList(getIndexServerHost())) {
			if (process.isIndexServerProcess()) {
				System.out.println("Index Server is Running with Process Id " + process.getPid() + "....");
				indexServerProcesses++;
			}
		}

		if (indexServerProcesses == 1) {
			return true;
		} else if (indexServerProcesses == 0) {
			return false;
		} else {
			throw new CIException("There were " + indexServerProcesses + " index server processes running.");
		}
	}

	private Host getIndexServerHost() {
		return new Configuration().getIndexServerHost().getValue();
	}

	public void startPolopoly() throws CIException {
		System.out.println("Starting Polopoly...");

		try {
			new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath() + "/bin/polopoly start")
					.execute();
		} catch (RunningInMavenException e) {
			new Executor("mvn jboss:start").setDirectory(new Configuration().getProjectHomeDirectory().getValue())
					.execute();
		}
	}

	public void stopPolopoly() throws CIException {
		// stopping jboss is the same thing as stopping polopoly when run from
		// Maven.
		if (new Configuration().isRunInNitro()) {
			return;
		}

		System.out.println("Stopping Polopoly...");

		try {
			new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath() + "/bin/polopoly stop")
					.execute();
		} catch (RunningInMavenException e) {
			new Executor("mvn jboss:stop").setDirectory(new Configuration().getProjectHomeDirectory().getValue())
					.execute();
		}
	}

	public void startIndexServer() throws CIException, RunningInMavenException {
		try {
			if (isIndexServerRunning()) {
				throw new CIException("Index server is already running before starting it.");
			}

			System.out.println("Starting Index Server...");

			if (getIndexServerHost().isLocalHost()) {
				LogFileMonitor monitor = new LogFileMonitor(new PolopolyDirectories().getIndexServerLogFile());

				// only "Starting IndexServer" message will logged when
				// starting index server
				// "IndexServer started" message will logged when
				// there are lucene index (not always have lucene index)
				monitor.addSuccessString("Starting IndexServer");
				// we need to add this failure string to ensure the
				// error does not occur after success string
				monitor.addFailureString("An exception occured when starting IndexServer");
				monitor.addFailureString("OutOfMemory");

				reallyStartIndexServer();

				monitor.monitor();
				
				if (!isIndexServerRunning()) {
					throw new CIException("Index server was not running after starting it.");
				}
			}
			else {
				reallyStartIndexServer();

				int timeElapsed = 0;
				
				do {
					if (timeElapsed > 30000) {
						throw new CIException("Failed to start withing 30s");
					}

					Thread.sleep(2000);
					
					timeElapsed += 2000;
				}
				while (!isIndexServerRunning());
			}
		} catch (CIException e) {
			throw new CIException("While starting indexserver: " + e.getMessage(), e);
		} catch (InterruptedException e) {
		}
	}

	protected void reallyStartIndexServer() {
		new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath()
				+ "/bin/polopoly start-java -Dmodule.name=indexserver").setHost(new Configuration().getIndexServerHost().getValue()).execute();
	}

	public void stopIndexServer() throws CIException, RunningInMavenException {
		try {
			if (!isIndexServerRunning()) {
				System.out.println("Indexserver was not running.");
				return;
			}

			System.out.println("Stopping Index Server...");

			try {
				new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath()
						+ "/bin/polopoly stop-java -Dmodule.name=indexserver").setHost(getIndexServerHost()).execute();

				long timer = 0;

				do {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// swallow
					}

					timer += 500;
				} while (timer < INDEXSERVER_WAIT_TIME && isIndexServerRunning());
			} catch (CIException e) {
				System.out.println("Error trying to stop index server nicely: " + e);
			}

			if (isIndexServerRunning()) {
				System.out.println("Index server is still running. Killing it instead.");

				killIndexServer();
			}
		} catch (CIException e) {
			throw new CIException("While stopping indexserver: " + e.getMessage(), e);
		}
	}

	protected void killIndexServer() {
		for (ProcessInfo process : getProcessList(getIndexServerHost())) {
			if (process.isIndexServerProcess()) {
				process.hardKill();
			}
		}
	}

	public void pauseIndexing() throws CIException, RunningInMavenException {
		System.out.println("Pausing Indexing...");
		new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath()
				+ "/bin/polopoly tools pause-indexer").execute();
	}

	public void resumeIndexing() throws CIException, RunningInMavenException {
		System.out.println("Resuming Indexing...");
		new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath()
				+ "/bin/polopoly tools resume-indexer").execute();
	}

	public boolean isJbossRunning() throws CIException {
		System.out.println("Checking if Jboss is running...");

		if (!isJbossProcessRunning()) {
			System.out.println("Jboss was not running (no JBoss process could be found).");

			return false;
		}

		Host host = configuration.getJbossHost().getValue();
		int port = configuration.getJbossWebPort().getValue();

		try {
			URL jboss = new URL("http://" + host + ":" + port + "/jmx-console");
			URLConnection jbossConnection = jboss.openConnection();
			new BufferedReader(new InputStreamReader(jbossConnection.getInputStream()));
		} catch (IOException e) {
			throw new CIException("The JBoss process was running but no response was given from http://" + host + ":"
					+ port + "/ Was the Jboss web port misconfigured?");
		}

		System.out.println("JBoss is running on " + host + " and port " + port);

		return true;
	}

	private boolean isJbossProcessRunning() throws CIException {
		int jbossProcesses = 0;

		for (ProcessInfo process : getProcessList()) {
			if (process.isJbossProcess()) {
				jbossProcesses++;
			}
		}

		if (jbossProcesses == 0) {
			System.out.println("No JBoss process found doing ps.");

			return false;
		} else if (jbossProcesses > 1) {
			throw new CIException("There were " + jbossProcesses + " JBoss processes running.");
		}

		return true;
	}

	public void startJboss() throws CIException {
		try {
			System.out.println("Starting Jboss in background...");

			File jbossLog = new JbossDirectories().getJbossLog(false);

			if (!jbossLog.exists()) {
				if (!jbossLog.getParentFile().exists()) {
					jbossLog.getParentFile().mkdir();
				}

				try {
					jbossLog.createNewFile();
				} catch (IOException e) {
					throw new CIException("While creating " + jbossLog + ": " + e);
				}
			}

			Executor run = new Executor(configuration.getJbossRunCommand().getValue() + " -Dsolr.solr.home="
					+ new PolopolyDirectories().getSolrDirectory(false) + configuration.getJBossJDKOptions().getValue()
					+ " > /dev/null").setDirectory(new JbossDirectories().getJbossStartupDirectory()).setWaitFor(false);

			run.execute();

			LogFileMonitor monitor = new LogFileMonitor(jbossLog);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore.
			}

			run.checkExitValue();

			monitor.addSuccessString("Started in ");

			monitor.addFailureString("Protocol handler initialization failed:");
			monitor.addFailureString("java.net.BindException");
			monitor.addFailureString("Protocol handler start failed");
			monitor.addFailureString("Incomplete Deployment listing");
			monitor.addFailureString("OutOfMemory");

			monitor.setOutputFilter(OutputFilter.NO_DEBUG);

			String successString = monitor.monitor();

			if (!successString.contains("4.0.5.GA")) {
				System.out.println();
				System.out.println("WARNING: You seem to be using a different JBoss version from the "
						+ "recommended 4.0.5.GA. This could lead to subtle errors.");
				System.out.println();
			}

			if (!isJbossRunning()) {
				throw new CIException("Even after JBoss had been started it was not found to be running.");
			}

			System.out.println("Done.");
		} catch (RunningInMavenException e) {
			startPolopoly();
		} catch (CIException e) {
			throw new CIException("While starting JBoss: " + e.getMessage(), e);
		}
	}

	public int reindexSolr() throws CIException, RunningInMavenException {
		System.out.println("Starting SOLR reindex (in background)...");

		new Executor(new PolopolyDirectories().getPolopolyDirectory().getAbsolutePath()
				+ "/bin/polopoly tools reindex-solr").execute();

		return (int) new PolopolyDirectories().getIndexServerLogFile().length();
	}

	public void stopJboss() throws CIException {
		System.out.println("Stopping Jboss...");

		for (ProcessInfo process : getProcessList()) {
			boolean isJbossProcess;

			try {
				isJbossProcess = process.isJbossProcess();
			} catch (JbossBelongsToOtherProjectException e) {
				isJbossProcess = true;
			}

			if (isJbossProcess) {
				System.out.println("Killing Jboss process nicely (" + shorten(process.getCommand())
						+ "). Waiting for it to stop...");
				process.softKill();

				for (int i = 0; i < 50; i++) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// go on.
					}

					try {
						if (!isJbossProcessRunning()) {
							System.out.println("JBoss terminated.");

							return;
						}
					} catch (JbossBelongsToOtherProjectException e) {
						// obviously still alive.
					}
				}

				System.out.println("JBoss did not go down after a nice kill. Running a hard kill.");

				process.hardKill();
			}
		}

		if (isJbossRunning()) {
			throw new CIException("Even after killing Jboss is was still found to be running.");
		}
	}

	public boolean isJ2EEContainerRunning() throws CIException {
		if (!isJ2EEContainerProcessRunning()) {
			return false;
		}

		try {
			UrlMonitor monitor = new UrlMonitor(new URL("http://localhost:8040/connection.properties"));

			monitor.setTimeoutMs(5000);
			monitor.waitForUrl();

			return true;
		} catch (UrlMonitorFailedException e) {
			return false;
		} catch (MalformedURLException e) {
			throw new CIException(e);
		}
	}

	public boolean isJ2EEContainerProcessRunning() throws CIException {
		int processes = 0;

		for (ProcessInfo process : getProcessList()) {
			if (process.isJ2EEContainerProcess()) {
				System.out.println("J2EE container is running with process ID " + process.getPid() + "....");
				processes++;
			}
		}

		if (processes == 1) {
			return true;
		} else if (processes == 0) {
			return false;
		} else {
			throw new CIException("There were " + processes + " J2EE container processes running.");
		}
	}

	public void startJbossIfNotRunning() {
		boolean jbossRunning;

		try {
			jbossRunning = isJbossRunning();
		} catch (JbossBelongsToOtherProjectException e) {
			System.out.println("The JBoss that was running belongs to another project. Stopping it.");

			stopJboss();

			jbossRunning = false;
		}

		if (!jbossRunning) {
			System.out.println("JBoss was not running. Starting it first...");

			startJboss();
		}
	}

	public void startPolopolyIfNotRunning() {
		boolean polopolyRunning;

		try {
			polopolyRunning = isPolopolyRunning();
		} catch (PolopolyPartiallyRunningException e) {
			System.out.println(e.getMessage());
			System.out.println("Killing those processes that are running and restarting.");

			killPolopoly();

			polopolyRunning = false;
		} catch (PolopolyBelongsToOtherProjectException e) {
			System.out.println("The Polopoly that was running belongs to another project. Stopping it.");

			killPolopoly();

			polopolyRunning = false;
		}

		if (!polopolyRunning) {
			System.out.println("Polopoly was not running. Starting it first...");

			startPolopoly();
		}
	}

	public synchronized boolean isPortAvailable(int port) {
		ServerSocket serverSocket = null;
		DatagramSocket datagramSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			datagramSocket = new DatagramSocket(port);
			datagramSocket.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}

			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					throw new RuntimeException("While closing ServerSocket used to test if port: " + port
							+ " is avaialble. This should not happen!");
				}
			}
		}

		return false;
	}
}
