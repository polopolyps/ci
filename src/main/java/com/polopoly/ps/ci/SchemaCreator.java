package com.polopoly.ps.ci;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.polopoly.ps.ci.exception.CIException;

public class SchemaCreator extends SqlCommandRunner {

	public void createSchemaIfRequired() {
		try {
			String result = runCommand("show tables", true);

			if (result.contains("Tables_in") || result.equals("")) {
				System.out.println("The database schema existed.");
				return;
			}

			if (result.contains("ERROR 1044") /* authorization error */) {
				throw new CIException("The specified database user " + databaseUser
						+ " was not root and the schema does not exist or could not be accessed by it. "
						+ "Cannot create the schema using this user; please specify root: " + result);
			}

			if (result.contains("ERROR 1049") /* unknown database */) {
				definitelyCreateSchema();
			} else {
				throw new CIException("Expected the mysql command to either succeed or "
						+ "return an authorization error if the database does not exist: " + result);
			}
		} catch (CIException e) {
			throw new CIException("While creating database schema: " + e.getMessage(), e);
		}
	}

	private void definitelyCreateSchema() {
		System.out.println("Creating database schema...");

		runCommand("CREATE DATABASE " + databaseSchema, false);
		grantPrivileges("localhost");
		grantPrivileges(getHostname());

		System.out.println("Done creating database schema.");
	}

	private String getHostname() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);

		new Executor("hostname").pipeTo(out).execute();

		return new String(baos.toByteArray());
	}

	private void grantPrivileges(String host) {
		runCommand("GRANT ALL PRIVILEGES ON polopoly.* TO '" + databaseUser + "'@'" + host + "' IDENTIFIED BY '"
				+ databasePassword + "'", false);
	}
}
