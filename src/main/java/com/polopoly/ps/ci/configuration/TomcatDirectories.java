package com.polopoly.ps.ci.configuration;

import java.io.File;

public class TomcatDirectories extends AbstractDirectories {

	public File getLog() {
		return verifyFileExistence(new Configuration().getTomcatLogFile().getValue());
	}

	public File getBinDirectory() {
		return  verifyDirectoryExistence(new File(new Configuration().getTomcatDirectory().getValue(), "bin"));
	}

}
