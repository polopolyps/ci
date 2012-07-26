package com.polopoly.ps.ci;

import java.io.File;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.configuration.PolopolyDirectories;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoDirectoryFoundException;
import com.polopoly.ps.ci.exception.RunningInMavenException;
import com.polopoly.ps.ci.tool.BackupId;

/**
 * Populates the custom/config and custom/client-lib directories in Polopoly by
 * running an assembly in the clientlib POM directory.
 */
public class ClientLibUpdater {
	private BackupId backupId = new EmptyBackupId();
	private Host host = new Host();
	private File configDirectory;
	private File clientlibDirectory;
	private File customDirectory;
	private File clientLibPomDir;

	public ClientLibUpdater() {
		configDirectory = new PolopolyDirectories().getConfigDirectory();
		clientlibDirectory = new File(new PolopolyDirectories().getClientLibDirectory().getAbsolutePath());
		customDirectory = new PolopolyDirectories().getCustomDirectory();
		clientLibPomDir = new Configuration().getClientLibPomDirectory().getValue();
	}

	public ClientLibUpdater(BackupId backupId) {
		this();
		
		this.backupId = backupId;
	}

	public ClientLibUpdater(BackupId backupId, Host host) {
		this(backupId);

		this.host = host;
	}

	public void updateClientLib(boolean generateFirst) throws CIException, RunningInMavenException {
		try {
			/**
			 * We are not moving the config files since these contain server-specific Nodexxx.properties files on production.
			 * We ARE, however, moving the client-lib since there could have been JARs in the old release that are not in the
			 * new release. 
			 */
			backupId.backupDir(configDirectory, host, false);
			backupId.backupDir(clientlibDirectory, host, true);

			if (generateFirst) {
				generateClientLib();
			}

			File installDirectory;

			try {
				installDirectory = new LocateDirectoryContaining().locateDirectoryContaining(new File(clientLibPomDir,
						"target"), "custom");
			} catch (NoDirectoryFoundException e) {
				throw new CIException("After running mvn assembly:assembly: " + e.getMessage());
			}

			File installClientlibDirectory = new File(installDirectory, "custom/" + clientlibDirectory.getName());

			File installConfigDirectory = new File(installDirectory, "custom/" + configDirectory.getName());

			if (!installClientlibDirectory.exists()) {
				throw new CIException("When running mvn assembly:assembly in " + clientLibPomDir.getAbsolutePath()
						+ " did not generate a client-lib directory in " + installClientlibDirectory.getAbsolutePath()
						+ ".");
			}

			if (!installConfigDirectory.exists()) {
				throw new CIException("When running mvn assembly:assembly in " + clientLibPomDir.getAbsolutePath()
						+ " did not generate a config directory in " + installConfigDirectory.getAbsolutePath() + ".");
			}

			removeDuplicateVersions(installClientlibDirectory);

			new CopyUtil().copyContentsOfDirectory(installClientlibDirectory, new Host(), clientlibDirectory, host);
			new CopyUtil().copyContentsOfDirectory(installConfigDirectory, new Host(), configDirectory, host);

	        System.out.println("Done.");
		} catch (CIException e) {
			throw new CIException("While generating custom/client-lib and custom/config: " + e.getMessage(), e);
		}
	}

	protected void generateClientLib() {
		System.out.println("Generating custom/config and custom/client-lib directories...");

		// we are not clearing config to preserve any Node_*.properties fields.
		new DirectoryUtil().ensureDirectoryExists(configDirectory);
		new DirectoryUtil().ensureDirectoryExistsAndIsEmpty(clientlibDirectory);

		new Executor("mvn assembly:assembly -DskipTests").setDirectory(clientLibPomDir).execute();
	}

	private void removeDuplicateVersions(File installClientlibDirectory) {
		NonDuplicatingClasspathBuilder classpath = new NonDuplicatingClasspathBuilder();
		classpath.addJarDirectory(installClientlibDirectory);

		for (File duplicateJar : classpath.getDuplicates()) {
			System.out.println("Deleting " + duplicateJar);

			duplicateJar.delete();
		}
	}

}
