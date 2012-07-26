package com.polopoly.ps.ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;

public class MavenClassPathParser {
	private File pomDirectory;
	private boolean testScope;

	MavenClassPathParser(File pomDirectory) {
		this.pomDirectory = Require.require(pomDirectory);
	}

	public Collection<File> getClasspath() {
		try {
			if (!new File(pomDirectory, "pom.xml").exists()) {
				throw new CIException("Expected " + pomDirectory + " to contain a pom.xml file.");
			}

			String classPathString = getClasspathString();

			ArrayList<File> result = new ArrayList<File>(100);

			for (String classPathEntry : classPathString.split(":")) {
				File file = new File(classPathEntry);

				if (!file.exists()) {
					VerboseLogging.log("The classpath entry " + classPathEntry + " was not an existing file.");
				} else {
					if (file.getName().endsWith(".jar")) {
						result.add(file);
					} else if (file.getName().endsWith(".war")) {
						VerboseLogging.log("Ignoring WAR file " + classPathEntry + " in classpath.");
					} else {
						VerboseLogging.log("Unknown classpath entry " + classPathEntry);
					}
				}
			}

			return result;
		} catch (CIException e) {
			throw new CIException("While generating classpath: " + e.getMessage(), e);
		}
	}

	protected String getClasspathString() {
		File classpathCache = new File(pomDirectory, ".classpath.ci" + (testScope ? ".test" : ""));

		String pomChecksum = calculatePomChecksum();

		if (classpathCache.exists()) {
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader(classpathCache));

				String classpath = reader.readLine();

				String checksum = reader.readLine();

				if (pomChecksum.equals(checksum)) {
					return classpath;
				}

				System.out.println("POMs had been modified. Rebuilding classpath.");

				classpathCache.delete();
			} catch (FileNotFoundException e) {
				System.out.println("While reading cached classpath " + e.getMessage());
			} catch (IOException e) {
				System.out.println("While reading cached classpath " + e.getMessage());
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		String result = getClasspathStringFromMaven();

		try {
			FileWriter writer = new FileWriter(classpathCache);

			writer.write(result);
			writer.write('\n');
			writer.write(pomChecksum);
			writer.write('\n');

			writer.close();
		} catch (IOException e) {
			System.out.println("While saving cached classpath " + e.getMessage());
		}

		return result;
	}

	private String calculatePomChecksum() {
		Checksum checksum = new Checksum();

		checksum.add(new File(pomDirectory, "pom.xml"));

		File parentDirectory = new Configuration().getProjectParentPomDirectory().getValue();

		checksum.add(new File(parentDirectory, "pom.xml"));

		for (File child : parentDirectory.listFiles()) {
			if (child.isDirectory()) {
				File childPom = new File(child, "pom.xml");

				if (childPom.exists()) {
					checksum.add(child.getName());
					checksum.add(childPom);
				}
			}
		}

		return checksum.toString();
	}

	protected String getClasspathStringFromMaven() {
		System.out.println("Generating classpath...");

		String dependencyOutput = new Executor("mvn dependency:resolve dependency:build-classpath -DincludeScope="
				+ (testScope ? "test" : "compile")).setDirectory(pomDirectory).setOutputOnConsole(true).execute();

		boolean isClasspath = false;

		String classPathString = null;

		for (String line : dependencyOutput.split("\n")) {
			if (line.contains("Dependencies classpath")) {
				isClasspath = true;
			} else if (isClasspath) {
				classPathString = line;
				break;
			}
		}

		if (!isClasspath) {
			throw new CIException("Could not find classpath in output from mvn dependency:build-classpath: \n"
					+ dependencyOutput);
		}

		System.out.println("Done generating classpath...");

		return classPathString;
	}

	public MavenClassPathParser setTestScope(boolean testScope) {
		this.testScope = testScope;

		return this;
	}
}
