package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;
import com.polopoly.ps.ci.exception.RunningInMavenException;

public class PolopolyDirectories extends AbstractDirectories {
	public File getPolopolyDirectory(boolean mustExist) throws RunningInMavenException {
		if (new Configuration().isRunInNitro()) {
			throw new RunningInMavenException("This operation is not supported when Polopoly is started from Maven.");
		}

		return new Configuration().getPolopolyDirectory().getValue(mustExist);
	}

	public File getPolopolyDirectory() throws RunningInMavenException {
		return getPolopolyDirectory(true);
	}

	public File getIndexDirectory() throws CIException, RunningInMavenException {
		return getPolopolySubdirectory("pear/work/ears/polopoly/searchindexes", true);
	}

	public File getPearDirectory() throws CIException, RunningInMavenException {
		return getPolopolySubdirectory("pear", true);
	}

	public File getSolrDirectory(boolean mustExist) throws RunningInMavenException {
		return getPolopolySubdirectory("pear/work/solr", mustExist);
	}

	public File getSolrDirectory() throws CIException, RunningInMavenException {
		return getSolrDirectory(true);
	}

	public File getClientLibDirectory() throws CIException, RunningInMavenException {
		return getPolopolySubdirectory("custom/client-lib", true);
	}

	public File getConfigDirectory() throws RunningInMavenException {
		return getPolopolySubdirectory("custom/config", true);
	}

	public File getCustomDirectory() throws RunningInMavenException {
		return getPolopolySubdirectory("custom", true);
	}

	public File getContainerClientlibDirectory() throws RunningInMavenException {
		return getPolopolySubdirectory("contrib-archives/container-client-lib", true);
	}

	private File getPolopolySubdirectory(String relativeDirectoryName, boolean mustExist) throws CIException,
			NoSuchFileException, RunningInMavenException {
		File result = new File(getPolopolyDirectory(), relativeDirectoryName);

		if (mustExist) {
			return verifyDirectoryExistence(result);
		} else {
			return result;
		}
	}

	private File getPolopolyFile(String relativeFileName) throws CIException, NoSuchFileException,
			RunningInMavenException {
		return verifyFileExistence(new File(getPolopolyDirectory(), relativeFileName));
	}

	public File getIndexServerLogFile() throws CIException, RunningInMavenException {
		String path = getPolopolyDirectory().getAbsolutePath() + "/pear/logs/indexserver.log";
		verifyFileExistence(new File(path));
		return new File(path);
	}

	public File getInstallScript() throws RunningInMavenException {
		return getPolopolyFile("bin/install.xml");
	}

	public File getPolopolyScript() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("bin/polopoly");
	}

	public File getAntExecutable() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("ant/bin/ant");
	}

	public File getCmServerEar() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("pear/j2ee/cm-server.ear");
	}

	public File getSolrWar() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("pear/j2ee/webapps/solr.war");
	}

	public File getStatisticsWar() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("pear/work/ears/polopoly/statistics.war");
	}

	public File getLibDirectory() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolySubdirectory("install/lib", true);
	}

	public File getLogFile() throws NoSuchFileException, CIException, RunningInMavenException {
		return getPolopolyFile("pear/logs/indexserver.log");
	}
}
