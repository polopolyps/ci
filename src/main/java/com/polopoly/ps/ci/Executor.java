package com.polopoly.ps.ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.polopoly.ps.ci.exception.CIException;

/**
 * Executes a command given as a Java String, prints the output and checks the
 * result code and aborts if the command failed.
 */
public class Executor {
	private String command;
	private File directory = new File(".");
	private PrintStream out = System.out;
	private boolean wait = true;
	private Process process;
	private boolean outputOnConsole = true;
	private Host host = new Host();

	public Executor(String command) {
		this.command = command;
	}

	public Executor setWaitFor(boolean wait) {
		this.wait = wait;

		return this;
	}

	Executor setOutputOnConsole(boolean outputOnConsole) {
		this.outputOnConsole = outputOnConsole;

		return this;
	}

	public Executor setDirectory(File directory) {
		this.directory = directory;

		return this;
	}

	Executor pipeTo(PrintStream out) {
		this.out = out;

		return this;
	}

	private String[] addShellPrefix(String command) {
		String[] commandArray;

		if (host.isLocalHost()) {
			commandArray = new String[3];
			commandArray[0] = "sh";
			commandArray[1] = "-c";
			commandArray[2] = command;
		} else {
			commandArray = new String[3];
			commandArray[0] = "/usr/bin/ssh";
			commandArray[1] = host.toString();
			commandArray[2] = command;
		}

		return commandArray;
	}

	public String execute() throws CIException {
		System.out.println("Running " + this + "...");

		StringBuffer result = new StringBuffer();

		try {
			String realCommand = command;

			if (host != null && directory != null) {
				realCommand = "cd " + directory + "; " + command;
			}

			ProcessBuilder processBuilder = new ProcessBuilder(addShellPrefix(realCommand));

			if (host == null && directory != null) {
				processBuilder.directory(directory);
			}

			process = processBuilder.redirectErrorStream(true).start();
		} catch (IOException e) {
			throw new CIException(e);
		}

		if (wait) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;

			try {
				while ((line = reader.readLine()) != null) {
					if (outputOnConsole) {
						out.println(line);
					}

					result.append(line + "\n");
				}
			} catch (IOException e) {
				// ignore
			}

			waitFor();

			return result.toString();
		}

		return "";
	}

	void waitFor() {
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// ignore.
		}

		checkExitValue();
	}

	void checkExitValue() {
		try {
			if (process.exitValue() != 0) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

				String line;

				try {
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				} catch (IOException e) {
					// ignore
				}

				String message = "The command \"" + command + "\""
						+ (!directory.getName().equals(".") ? " run in directory " + directory + " " : "")
						+ (host != null ? " on host " + host : "") + " failed.";

				System.err.println(message);
				throw new CIException(message);
			}
		} catch (IllegalThreadStateException e) {
			// the process is still executing. leave it alone.
		}
	}

	public Executor setHost(Host host) {
		if (host == null) {
			host = new Host();
		}

		this.host = host;

		return this;
	}

	public Process getProcess() {
		return process;
	}

	public String toString() {
		return command + (!host.isLocalHost() ? " on host " + host : "")
				+ (directory.getName().equals(".") ? "" : " in " + directory.getAbsolutePath());
	}
}
