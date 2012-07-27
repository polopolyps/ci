package com.polopoly.ps.ci;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import com.polopoly.ps.ci.configuration.AbstractConfiguration.ConfigurationStringValue;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.UrlMonitorFailedException;

public abstract class AbstractWebserverController extends ProcessUtil {
	private static final int MAX_LINES = 50;
	private RandomAccessFile logFile;
	protected Host host = new Host();

	private int urlTimeoutSeconds = new Configuration().getGuiServerStartupTimeoutSeconds().getValue();
	private int singleRequestTimeoutSeconds = urlTimeoutSeconds;

	protected abstract boolean isWebserverProcess(ProcessInfo process);

	public AbstractWebserverController() {
	}

	public AbstractWebserverController(Host host) {
		this.host = host;
	}

	public void start() throws CIException {
		Configuration configuration = new Configuration();

		if (isPortAlreadyInUse()) {
			throw new CIException("The port " + configuration.getGuiServerPort().getValue() + " already in use.");
		}

		Executor executor = startWebserverProcess();

		verifyServerResponding(executor);
	}

	public void verifyServerResponding(Executor executor) {
		try {
			for (ConfigurationStringValue url : new Configuration().getProjectUrls().getValue()) {
				waitFor(url.getValue(), executor);
			}
		} catch (MalformedURLException e) {
			throw new CIException(e);
		}
	}

	protected abstract Executor startWebserverProcess();

	protected void waitFor(String uri, final Executor executor) throws MalformedURLException, CIException {
		UrlMonitor monitor = new UrlMonitor(new URL("http://" +
		/*
		 * Problems connecting to localhost for some funny reason on Wegener so
		 * use 127.0.0.1
		 */
		(host.isLocalHost() ? "127.0.0.1" : host) + ":" + getPort() + uri));

		if (executor != null) {
			monitor.setWhatToDoInBreaks(new Runnable() {
				@Override
				public void run() {
					try {
						printOutput(executor);
					} catch (CIException e) {
						System.err.println(e.toString());
					}
				}
			});
		}

		monitor.setTimeoutMs(urlTimeoutSeconds * 1000);
		monitor.setSingleRequestTimeoutMs(singleRequestTimeoutSeconds * 1000);
		monitor.waitForUrl();
	}

	public boolean isRunning() {
		for (ProcessInfo process : getProcessList(host)) {
			if (isWebserverProcess(process)) {
				for (ConfigurationStringValue url : new Configuration().getProjectUrls().getValue()) {
					UrlMonitor monitor;
					try {
						monitor = new UrlMonitor(new URL("http://" +
						/*
						 * Problems connecting to localhost for some funny
						 * reason on Wegener so use 127.0.0.1
						 */
						(host.isLocalHost() ? "127.0.0.1" : host) + ":" + getPort() + url.getValue()));
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}

					monitor.setTimeoutMs(3000);
					monitor.setSingleRequestTimeoutMs(30000);

					try {
						monitor.waitForUrl();
					} catch (UrlMonitorFailedException e) {
						System.out.println("Process was running but the server did not respond: " + e.getMessage());

						return false;
					}
				}

				return true;
			}
		}

		return false;
	}

	public boolean isPortAlreadyInUse() {
		if (host != null) {
			// can't check it on remote host.
			return false;
		}

		return !isPortAvailable(getPort());
	}

	protected int getPort() {
		return new Configuration().getGuiServerPort().getValue();
	}

	public void kill() {
		for (ProcessInfo process : getProcessList(host)) {
			if (isWebserverProcess(process)) {
				process.hardKill();
			}
		}
	}

	private void printOutput(Executor executor) throws CIException {
		try {
			if (logFile == null) {
				logFile = new RandomAccessFile(getLog(), "r");

				try {
					logFile.seek(logFile.length());
				} catch (IOException e) {
					// ignore.
				}
			}

			try {
				String line;

				while ((line = logFile.readLine()) != null) {
					int lineCount = 0;

					if (++lineCount == MAX_LINES) {
						System.out
								.println("This is your CI script: There is a lot of activity in the log file. Skipping a few lines...");
					} else if (lineCount < MAX_LINES) {
						System.out.println(line);
					}
				}
			} catch (IOException e) {
				// done.
			}
		} catch (FileNotFoundException e) {
			if (executor != null) {
				executor.checkExitValue();
			}

			System.out.println("Could not find webserver log file " + getLog()
					+ " but the server process does not seem to have terminated unnaturally.");
		}
	}

	public abstract File getLog();

	protected void checkForImmediateTermination(Executor executor) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// fine. go on.
		}

		// check if jetty terminated immediately.
		executor.checkExitValue();
	}

	public void setSingleRequestTimeoutSeconds(int singleRequestTimeoutSeconds) {
		this.singleRequestTimeoutSeconds = singleRequestTimeoutSeconds;
	}

	public void setUrlTimeoutSeconds(int urlTimeoutSeconds) {
		this.urlTimeoutSeconds = urlTimeoutSeconds;

		// if the request hangs we don't want to time out since we then trigger
		// a
		// new request which can overload the server so better to wait. if,
		// however,
		// we immediately get an exception, we want to retry.
		singleRequestTimeoutSeconds = urlTimeoutSeconds;
	}

	public Host getHost() {
		return host;
	}

}
