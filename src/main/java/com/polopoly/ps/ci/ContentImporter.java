package com.polopoly.ps.ci;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.pcmd.tool.DirectoryJarNameMapping;
import com.polopoly.ps.pcmd.tool.ImportParameters;
import com.polopoly.ps.pcmd.tool.ImportTool;
import com.polopoly.util.client.PolopolyContext;

public class ContentImporter {
	private boolean importTestContent;
	private boolean force = false;

	public void setImportTestContent(boolean importTestContent) {
		this.importTestContent = importTestContent;
	}

	public void importContent() throws CIException {
		importDirectories();
	}

	public void importDirectories(DirectoryJarNameMapping... directories) {
		PolopolyContext context = new PolopolyConnector().connect(importTestContent);

		ImportParameters parameters = new ImportParameters();
		parameters.setForce(force);

		List<File> directoryList = new ArrayList<File>();

		for (DirectoryJarNameMapping directory : directories) {
			directoryList.add(directory.getDirectory());
			parameters.addDirectory(directory);
		}

		new ImportTool().execute(context, parameters);
	}

	public void importDemoData(DirectoryJarNameMapping... directories) {
		System.out.println("Importing demodata content...");

		importDirectories(append(directories, getMappedResourceDirectory(new Configuration().getDemodataPomDirectory()
				.getValue())));

		System.out.println("Done.");
	}

	private DirectoryJarNameMapping[] append(DirectoryJarNameMapping[] directories, DirectoryJarNameMapping value) {
		DirectoryJarNameMapping[] result = new DirectoryJarNameMapping[directories.length + 1];

		for (int i = 0; i < directories.length; i++) {
			result[i] = directories[i];
		}

		result[result.length - 1] = value;

		return result;
	}

	public static DirectoryJarNameMapping getMappedResourceDirectory(File pomDirectory) {
		return new DirectoryJarNameMapping(getResourceDirectory(pomDirectory), pomDirectory.getName());
	}

	public static File getResourceDirectory(File pomDirectory) {
		File resourcesDirectory = new File(pomDirectory, "src/main/resources");

		if (!resourcesDirectory.exists()) {
			// this is less than ideal; should be solved differently.
			// unfortunately, I know of no way of getting the resources paths
			System.out.println("Could not find resource directory " + resourcesDirectory
					+ ". Falling back to target/classes.");
			resourcesDirectory = new File(pomDirectory, "target/classes");
		}

		return resourcesDirectory;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

}