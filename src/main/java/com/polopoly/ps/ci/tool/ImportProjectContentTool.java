package com.polopoly.ps.ci.tool;

import java.io.File;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.ci.ContentImporter;
import com.polopoly.ps.ci.ProcessUtil;
import com.polopoly.ps.ci.configuration.Configuration;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.tool.DirectoryJarNameMapping;
import com.polopoly.ps.pcmd.tool.DoesNotRequireRunningPolopoly;
import com.polopoly.util.client.PolopolyContext;

public class ImportProjectContentTool implements Tool<ImportProjectContentParameters>, DoesNotRequireRunningPolopoly {

	@Override
	public void execute(PolopolyContext context, ImportProjectContentParameters parameters) throws FatalToolException {
		// the most important aspect here is to make sure the polopoly running
		// is the same as the project we are importing.
		new ProcessUtil().startPolopolyIfNotRunning();

		// note that this will currently load the clientlib project into the
		// classpath rather than content which is what we want to import.
		ContentImporter importer = new ContentImporter();

		importer.setImportTestContent(parameters.isIncludeTestContent());
		importer.setForce(parameters.isForce());

		File contentPomDirectory = new Configuration().getContentPomDirectory().getValue(false);

		DirectoryJarNameMapping contentProjectResources = ContentImporter
				.getMappedResourceDirectory(contentPomDirectory);

		if (parameters.isDemoData()) {
			importer.importDemoData(contentProjectResources);
		} else {
			importer.importDirectories(contentProjectResources);
		}
	}

	@Override
	public ImportProjectContentParameters createParameters() {
		return new ImportProjectContentParameters();
	}

	@Override
	public String getHelp() {
		return "Imports project content and templates.";
	}

}
