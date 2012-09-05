package com.polopoly.ps.ci;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class SqlCommandRunner {
	protected String databaseUser;
	protected String databasePassword;
	protected String databaseSchema;
	protected String databaseHost;

	public SqlCommandRunner() {
		databaseUser = new Configuration().getDatabaseUser().getValue();
		databasePassword = new Configuration().getDatabasePassword().getValue();
		databaseSchema = new Configuration().getDatabaseSchema().getValue();
		databaseHost = new Configuration().getDatabaseHost().getValue();
	}

	protected String runCommand(String command, boolean allowFailure) throws CIException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);

		System.out.println("Running SQL command \"" + command + "\".");

		try {
			new Executor("mysql --host=" + databaseHost + " --user=" + databaseUser + " --password=" + databasePassword
					+ " " + (!command.contains("CREATE DATABASE") ? databaseSchema + " " : "") + "-e \"" + command
					+ "\"").pipeTo(out).execute();
		} catch (CIException e) {
			if (!allowFailure) {
				throw new CIException("Command \"" + command + "\" failed: " + new String(baos.toByteArray()));
			}
		}

		return new String(baos.toByteArray());
	}
}
