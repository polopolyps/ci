package com.polopoly.ps.ci.configuration;

import java.io.File;

import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NotConfiguredException;

public class Configuration extends AbstractConfiguration {

	private static final String VERSION_MAVEN_WAS_INTRODUCED = "10.3";

	private static final String DEFAULT_DISTRIBUTION_URL = "http://support.polopoly.com/confluence/download/attachments/27821981/Polopoly_dist_10-4-0-0e345d8.jar";

	// should be kept at more or less the current versions.
	private static final String DEFAULT_TOOLS_VERSION = "10.4.0-0e345d8-3";
	private static final String DEFAULT_POLOPOLY_VERSION = "10-4-0-0e345d8";

	/**
	 * The MySQL user to use to access the database.
	 */
	public ConfigurationStringValue getDatabaseUser() {
		return string("database.user", defaultToString("cmuser"));
	}

	/**
	 * The password for the user {@link #getDatabaseUser()}.
	 */
	public ConfigurationStringValue getDatabasePassword() {
		return string("database.password", defaultToPrompt("MySQL password of user " + getDatabaseUser()));
	}

	/**
	 * The database schema the Polopoly installation is using.
	 */
	public ConfigurationStringValue getDatabaseSchema() {
		return string("database.schema", defaultToString("polopoly"));
	}

	/**
	 * The database host the Polopoly installation is using.
	 */
	public ConfigurationStringValue getDatabaseHost() {
		return string("database.host", defaultToString("localhost"));
	}

	/**
	 * The install script to use for installation.
	 */
	public ConfigurationFileValue getInstallScript() {
		return file("scripts.install.antscript",
				defaultToConfigurationFile(getProjectHomeDirectory(), "common/install.xml"), getProjectHomeDirectory());
	}

	/**
	 * The import script to use for installation. The script is assumed to
	 * import the content present in the current directory.
	 */
	public ConfigurationFileValue getImportScript() {
		return file("scripts.import.antscript",
				defaultToConfigurationFile(getProjectHomeDirectory(), "common/import.xml"), getProjectHomeDirectory());
	}

	/**
	 * Returns the server name JBoss is running on.
	 */
	public ConfigurationHostValue getJbossHost() {
		return host("jboss.host", defaultToString("localhost"));
	}

	public ConfigurationIntegerValue getJbossJmxPort() {
		return integer("jboss.jmx.port", defaultToString("9999"));
	}

	public ConfigurationStringValue getJbossJmxPassword() {
		return string("jboss.jmx.password", defaultToString("monitorRole"));
	}

	/**
	 * Returns the JBoss web port. This is the port SOLR is using if deployed on
	 * JBoss.
	 */
	public ConfigurationIntegerValue getJbossWebPort() {
		return integer("jboss.web.port", defaultToString("8081"));
	}

	/**
	 * Return the HTTP port of the server the GUI and preview applications are
	 * running.
	 */
	public ConfigurationIntegerValue getGuiServerPort() {
		return integer("guiserver.port", defaultToString("8090"));
	}

	public ConfigurationHostValue getGuiServerHost() {
		return host("guiserver.host", defaultToConfigurationString(getJbossHost()));
	}

	public ConfigurationIntegerValue getGuiServerJmxPort() {
		return integer("guiserver.jmx.port", defaultToString("9999"));
	}

	public ConfigurationStringValue getGuiServerJmxPassword() {
		return string("guiserver.jmx.password", defaultToString("monitorRole"));
	}

	/**
	 * Return the port SOLR is running on.
	 */
	public ConfigurationIntegerValue getSolrPort() {
		return integer("solr.port", defaultToConfigurationString(getJbossWebPort()));
	}

	/**
	 * Returns the directory in which the POM for the preview webapp is located.
	 */
	public ConfigurationFileValue getWebPomDirectory() {
		return file("project.web.pom.directory",
				defaultToConfigurationFile(getProjectHomeDirectory(), "webapp-dispatcher"), getProjectHomeDirectory());
	}

	/**
	 * Returns the directory in which the POM for the webapp that starts the GUI
	 * application is located. This is where Jetty will be started.
	 */
	public ConfigurationFileValue getGuiPomDirectory() {
		return file("project.gui.pom.directory",
				defaultToConfigurationFile(getProjectHomeDirectory(), "webapp-polopoly"), getProjectHomeDirectory());
	}

	/**
	 * Returns the directory in which the POM for the project containing
	 * demodata is located.
	 */
	public ConfigurationFileValue getDemodataPomDirectory() {
		return file("project.demodata.pom.directory",
				defaultToConfigurationFile(getProjectHomeDirectory(), "src/resources"), getProjectHomeDirectory());
	}

	/**
	 * Returns the directory in which the POM for the project containing all
	 * content and templates is located.
	 */
	public ConfigurationFileValue getContentPomDirectory() {
		return file("project.content.pom.directory",
				defaultToConfigurationFile(getProjectHomeDirectory(), "src/resources"), getProjectHomeDirectory());
	}

	/**
	 * Returns the directory in which a POM that generates an assembly with the
	 * contents of the custom/client-lib and custom/config directories for
	 * installation is located.
	 */
	public ConfigurationFileValue getClientLibPomDirectory() {
		return file("project.clientlib.pom.directory",
				defaultToConfigurationFile(getProjectHomeDirectory(), "webapp-dispatcher"), getProjectHomeDirectory());
	}

	/**
	 * Returns the project root directory. The Maven modules it composes are
	 * assumed to be in subdirectories of it.
	 */
	public ConfigurationFileValue getProjectParentPomDirectory() {
		return file("project.parent.pom.directory",
				defaultToString(getProjectHomeDirectory().getConfigurationString()), getProjectHomeDirectory());
	}

	/**
	 * Returns the project root directory. The Maven modules it composes are
	 * assumed to be in subdirectories of it.
	 */
	public ConfigurationFileValue getProjectHomeDirectory() {
		return file("project.home.directory", defaultToString("/projects/mvntemplate"));
	}

	/**
	 * A list of URLs that are guaranteed to exist in the project; for use when
	 * sanity checking tomcat. URLs dont include the hostname and should be
	 * valid both in preview and front.
	 */
	public ConfigurationListValue<ConfigurationStringValue> getProjectUrls() {
		return new ConfigurationListValue<AbstractConfiguration.ConfigurationStringValue>("project.url") {
			@Override
			protected ConfigurationStringValue createValue(String variableName) {
				return string(variableName);
			}

			@Override
			protected ConfigurationStringValue createDefaultValue() {
				return string(variableName + ".0", defaultToString("/"));
			}
		};
	}

	/**
	 * Returns the directory JBoss is installed in.
	 */
	public ConfigurationFileValue getJbossDirectory() {
		return file("jboss.directory", defaultToConfigurationFile(getProjectHomeDirectory(), "install/jboss"),
				getProjectHomeDirectory());
	}

	public boolean isRunInNitro() {
		if (VERSION_MAVEN_WAS_INTRODUCED.compareTo(getPolopolyVersion().getValue()) < 0) {
			return "true".equalsIgnoreCase(string("polopoly.maven", defaultToString("true")).getValue().toString());
		} else {
			return false;
		}
	}

	/**
	 * Returns the directory Polopoly is installed in.
	 */
	public ConfigurationFileValue getPolopolyDirectory() {
		return file("polopoly.directory", defaultToConfigurationFile(getProjectHomeDirectory(), "install/polopoly"),
				getProjectHomeDirectory());
	}

	/**
	 * The password of sysadmin. It will be changed to this password when
	 * installing and it will be used to log in when connecting.
	 */
	public ConfigurationStringValue getPolopolySysadminPassword() {
		// intentially not defaulting to anything. the caller should just not
		// log in if not set.
		return string("polopoly.sysadmin.password");
	}

	/**
	 * Returns the active JBoss profile (defaults to "default"). Used to
	 * determine directories within JBoss.
	 */
	public ConfigurationStringValue getJbossProfile() {
		return string("jboss.profile", defaultToString("default"));
	}

	/**
	 * Returns how long the scripts will wait for the GUI web server to start.
	 */
	public ConfigurationIntegerValue getGuiServerStartupTimeoutSeconds() {
		/**
		 * The timeout is extremely long since if the caches are cold and there
		 * is lots of new data the startup can actually take this long.
		 */
		return integer("guiserver.startup.timeout.seconds", defaultToString("600"));
	}

	/**
	 * Returns any command-line parameters to pass to the JVM when starting the
	 * GUI webserver.
	 */
	public ConfigurationStringValue getJettyJDKOptions() {
		return string("guiserver.jdk.options", defaultToString(""));
	}

	/**
	 * Returns how long the scripts will wait for reindexing to terminate.
	 */
	public ConfigurationIntegerValue getSolrReindexingTimeoutSeconds() {
		return integer("solr.reindexing.timeout.seconds", defaultToString("900"));
	}

	/**
	 * Returns any command-line parameters to pass to the JVM when starting
	 * JBoss.
	 */
	public ConfigurationStringValue getJBossJDKOptions() {
		return string("jboss.jdk.options", defaultToString(""));
	}

	/**
	 * Returns the command using which JBoss is started.
	 */
	public ConfigurationStringValue getJbossRunCommand() {
		return string("jboss.start.script", defaultToConfigurationFile(getJbossDirectory(), "bin/run.sh"));
	}

	/**
	 * Returns the directory to be in when starting JBoss.
	 */
	public ConfigurationFileValue getJbossStartupDirectory() {
		return file("jboss.start.directory", defaultToConfigurationFile(getJbossDirectory(), "bin"),
				getJbossDirectory());
	}

	/**
	 * The Polopoly distribution. Used during installation. If it is not present
	 * on the local files system, this is where it will be downloaded to.
	 * 
	 * @see #getPolopolyDistributionUrl()
	 */
	public ConfigurationFileValue getPolopolyDistributionFile() {
		return file("polopoly.distribution.file",
				defaultToConfigurationFile(getProjectHomeDirectory(), "lib/polopoly-dist.jar"),
				getProjectHomeDirectory());
	}

	/**
	 * Where the Polopoly distribution can be downloaded from if not on the
	 * local file system.
	 * 
	 * @see #getPolopolyDistributionFile()
	 */
	public ConfigurationUrlValue getPolopolyDistributionUrl() {
		return url("polopoly.distribution.url", defaultToString(DEFAULT_DISTRIBUTION_URL));
	}

	public ConfigurationStringValue getSupportPassword() {
		return string("support.password", defaultToPrompt("Your support password"));
	}

	public ConfigurationStringValue getSupportUsername() {
		return string("support.username", defaultToPrompt("Your support user name"));
	}

	public ConfigurationUrlValue getJBossDistributionUrl() {
		return url(
				"jboss.distribution.url",
				defaultToString("http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.0.5.GA/jboss-4.0.5.GA.zip/download"));
	}

	public ConfigurationFileValue getJBossDistributionFile() {
		return file("jboss.distribution.file",
				defaultToConfigurationFile(getProjectHomeDirectory(), "lib/jboss-4.0.5.GA.zip"),
				getProjectHomeDirectory());
	}

	/**
	 * Where to download the MySQL connector if it doesn't exist locally.
	 */
	public ConfigurationUrlValue getMySQLConnectorDistributionUrl() {
		return url("mysql.connector.distribution.url",
				defaultToString("http://www.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.17.zip/from/"
						+ "http://sunsite.informatik.rwth-aachen.de/mysql/"));
	}

	/**
	 * Where the MySQL connector distribution will be stored after downloading.
	 * Note that the distribution is more than just the JAR.
	 * 
	 * @see #getMySQLConnectorFile()
	 */
	public ConfigurationFileValue getMySQLConnectorDistributionFile() {
		return file("mysql.connector.distribution.file",
				defaultToConfigurationFile(getProjectHomeDirectory(), "lib/mysql-connector-java-5.1.17.zip"),
				getProjectHomeDirectory());
	}

	/**
	 * The MySQL connector driver. Will be extracted from the
	 * {@link #getMySQLConnectorDistributionFile()} if not present.
	 */
	public ConfigurationFileValue getMySQLConnectorFile() {
		return file("mysql.connector.file",
				defaultToConfigurationFile(getProjectHomeDirectory(), "lib/mysql-connector-java-5.1.17-bin.jar"),
				getProjectHomeDirectory());
	}

	public ConfigurationUrlValue getSourceRepositoryUrl() {
		return url("sourcerepository.url", defaultToString("http://projects.polopoly.com/svn/mvntemplate/trunk"));
	}

	public ConfigurationStringValue getSourceRepositoryUser() {
		return string("sourcerepository.user", defaultToPrompt("Source code repository user"));
	}

	public ConfigurationStringValue getSourceRepositoryPassword() {
		return string("sourcerepository.password", defaultToPrompt("Source code repository password"));
	}

	public ConfigurationFileValue getPolopolyCleanDumpFile() {
		return file("polopoly.clean.dump.file", defaultToConfigurationFile(new FileValue() {
			@Override
			public File getNonExistingFile() {
				try {
					return new HudsonVariables().getUserContentDirectory();
				} catch (CIException e) {
					return getProjectHomeDirectory().getNonExistingFile();
				}
			}
		}, "lib/clean-polopoly-dump.tar.gz"));
	}

	public ConfigurationStringValue getToolsVersion() {
		return string("tools.version", defaultToString(DEFAULT_TOOLS_VERSION));
	}

	/**
	 * Returns the Maven version required. Matches only as long as the returned
	 * string, so "2.2" matches only that exact version whereas "2" matches and
	 * 2.x version and "" matches any version.
	 */
	public ConfigurationStringValue getMavenVersion() {
		return string("maven.version", defaultToString(""));
	}

	public ConfigurationStringValue getPolopolyVersion() {
		return string("polopoly.version", defaultToString(DEFAULT_POLOPOLY_VERSION));
	}

	private class DefaultArtifactValue implements DefaultValue {
		private String artifact;
		private ConfigurationStringValue version;

		DefaultArtifactValue(String artifact, ConfigurationStringValue version) {
			this.artifact = artifact;
			this.version = version;
		}

		@Override
		public String compute() {
			return artifact + version.getValue();
		}

		@Override
		public String toString() {
			return version + " prefixed with the default group and artifact ID.";
		}
	}

	public ConfigurationStringValue getPolopolyArtifact() {
		return string("polopoly.artifact", new DefaultArtifactValue("com.polopoly:polopoly:", getPolopolyVersion()));
	}

	public ConfigurationStringValue getPcmdArtifact() {
		return string("tools.pcmd.artifact", new DefaultArtifactValue("com.polopoly.ps.tools:pcmd:", getToolsVersion()));
	}

	public ConfigurationStringValue getHotdeployArtifact() {
		return string("tools.hotdeploy.artifact", new DefaultArtifactValue("com.polopoly.ps.tools:hotdeploy:",
				getToolsVersion()));
	}

	public ConfigurationStringValue getHotdeployToolsArtifact() {
		return string("tools.hotdeploy-pcmd-tools.artifact", new DefaultArtifactValue(
				"com.polopoly.ps.tools:hotdeploy-pcmd-tools:", getToolsVersion()));
	}

	public ConfigurationStringValue getRepositoryType() {
		return string("sourcerepository.type");
	}

	public ConfigurationStringValue getGitRepository() {
		return string("git.repository");
	}

	public ConfigurationFileValue getTomcatDirectory() {
		return file("tomcat.directory");
	}

	public ConfigurationFileValue getTomcatLogFile() {
		return file("tomcat.log.file", defaultToConfigurationFile(getTomcatDirectory(), "logs/catalina.out"));
	}

	public ConfigurationStringValue getTomcatOptions() {
		return string("tomcat.options", defaultToString(""));
	}

	public ConfigurationListValue<ConfigurationHostValue> getFrontHosts() {
		return new ConfigurationListValue<AbstractConfiguration.ConfigurationHostValue>("front.host") {
			@Override
			protected ConfigurationHostValue createValue(String variableName) {
				return host(variableName);
			}

			@Override
			protected ConfigurationHostValue createDefaultValue() {
				throw new NotConfiguredException(
						"No fronts were configured (use the property front.host.<n>, <n> being a number starting with 1).");
			}
		};
	}

	public ConfigurationIntegerValue getFrontJmxPort() {
		return integer("front.jmx.port", defaultToString("9999"));
	}

	public ConfigurationStringValue getFrontJmxPassword() {
		return string("front.jmx.password", defaultToString("monitorRole"));
	}

	public ConfigurationHostValue getIndexServerHost() {
		return host("indexserver.host", defaultToString("localhost"));
	}

	public ConfigurationIntegerValue getIndexServerJmxPort() {
		return integer("indexserver.jmx.port", defaultToString("9999"));
	}

	public ConfigurationStringValue getIndexServerJmxPassword() {
		return string("indexserver.jmx.password", defaultToString("monitorRole"));
	}

	public ConfigurationFileValue getBackupDir() {
		return file("backup.directory", defaultToString("/opt/backup"));
	}

	public ConfigurationFileValue getJbossLogFile() {
		return file("jboss.log.file", defaultToConfigurationFile(getJbossDirectory(), "server/default/log/server.log"));
	}
}
