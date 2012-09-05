package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileInRepo;

public class DependencyResolver {
	/**
	 * @param artifact
	 *            <group>:<artifact>:<version>
	 */
	public File resolve(String artifact) {
		try {
			return getLocalRepoFile(artifact);
		} catch (NoSuchFileInRepo e) {
			new Executor("mvn org.apache.maven.plugins:maven-dependency-plugin:2.3:get "
					+ "-DrepoUrl=http://maven.polopoly.com/nexus/content/repositories/professional-services "
					+ "-DrepoId=polopoly-ps -Dartifact=" + artifact).execute();

			try {
				return getLocalRepoFile(artifact);
			} catch (NoSuchFileInRepo e1) {
				throw new CIException("Failed loading the artifact " + artifact
						+ ". Even after calling the Maven dependency plugin it wasn't in the repository: "
						+ e.getMessage());
			}
		}

	}

	private File getLocalRepoFile(String artifact) throws NoSuchFileInRepo {
		String fileName = stripGroupId(artifact).replace(':', '-') + ".jar";
		String directory = getGroupId(artifact).replace('.', '/') + "/" + stripGroupId(artifact).replace(':', '/');

		File result = new File(getRepoRoot(), directory + "/" + fileName);

		if (!result.exists()) {
			File currentDirectoryFile = new File(".", fileName);

			// useful fallback for systems without Maven.
			if (currentDirectoryFile.exists()) {
				return currentDirectoryFile;
			}

			throw new NoSuchFileInRepo("The file " + result + " (artifact " + artifact
					+ ") did not exist in local repo or current directory.");
		}

		return result;
	}

	private String stripGroupId(String artifact) {
		int i = artifact.indexOf(':');

		if (i == -1) {
			throw new CIException("Expected the artifact " + artifact
					+ " to be of the form <group>:<artifact>:<version>");
		}

		return artifact.substring(i + 1);
	}

	private String getGroupId(String artifact) {
		int i = artifact.indexOf(':');

		if (i == -1) {
			throw new CIException("Expected the artifact " + artifact
					+ " to be of the form <group>:<artifact>:<version>");
		}

		return artifact.substring(0, i);
	}

	private File getRepoRoot() {
		String userHome = System.getProperty("user.home");

		if (userHome == null) {
			throw new CIException("Could not get user home.");
		}

		return new File(new File(userHome), ".m2/repository");
	}
}
