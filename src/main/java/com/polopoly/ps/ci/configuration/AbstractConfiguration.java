package com.polopoly.ps.ci.configuration;

import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.polopoly.ps.ci.Host;
import com.polopoly.ps.ci.VerboseLogging;
import com.polopoly.ps.ci.exception.CIException;
import com.polopoly.ps.ci.exception.NoSuchFileException;
import com.polopoly.ps.ci.exception.NotConfiguredException;

public class AbstractConfiguration {

	private static final String CI_PROPERTIES_FILE = "ci.properties";

	private static Properties lazyProperties;

	private static Map<String, String> computedConfiguration = new HashMap<String, String>();

	private static File propertiesFile;

	private static File homeDirectory = new File(".");

	private Properties getProperties() {
		if (lazyProperties != null) {
			return lazyProperties;
		}

		File propertiesFileDirectory;

		try {
			propertiesFileDirectory = new HudsonVariables().getHudsonHome();
		} catch (CIException e) {
			propertiesFileDirectory = homeDirectory;
		}

		String project = System.getenv("CI_PROJECT");

		if (project != null) {
			propertiesFile = new File(propertiesFileDirectory, project + ".properties");
		} else {
			propertiesFile = new File(propertiesFileDirectory, CI_PROPERTIES_FILE);
		}

		if (!propertiesFile.exists()) {
			System.err.println("Could not find configuration file " + propertiesFile.getAbsolutePath()
					+ ". Will use default values.");

			lazyProperties = new Properties();
		} else {
			lazyProperties = new PropertiesReader(propertiesFile).read();
		}

		return lazyProperties;
	}

	/**
	 * The directory where the ci.properties file is expected.
	 */
	public static void setHomeDirectory(File directory) {
		homeDirectory = directory;
	}

	protected ConfigurationStringValue string(String variableName, DefaultValue defaultValue) {
		return new ConfigurationStringValue(variableName, defaultValue);
	}

	protected ConfigurationStringValue string(String variableName) {
		return new ConfigurationStringValue(variableName);
	}

	protected ConfigurationHostValue host(String variableName) {
		return new ConfigurationHostValue(variableName);
	}

	protected ConfigurationHostValue host(String variableName, DefaultValue defaultValue) {
		return new ConfigurationHostValue(variableName, defaultValue);
	}

	protected ConfigurationIntegerValue integer(String variableName, DefaultValue defaultValue) {
		return new ConfigurationIntegerValue(variableName, defaultValue);
	}

	protected ConfigurationFileValue file(String variableName, DefaultValue defaultValue,
			ConfigurationFileValue relativeTo) {
		return new ConfigurationFileValue(variableName, defaultValue, relativeTo);
	}

	protected ConfigurationFileValue file(String variableName, DefaultValue defaultValue) {
		return new ConfigurationFileValue(variableName, defaultValue);
	}

	protected ConfigurationFileValue file(String variableName) {
		return new ConfigurationFileValue(variableName, null);
	}

	protected ConfigurationUrlValue url(String variableName, DefaultValue defaultValue) {
		return new ConfigurationUrlValue(variableName, defaultValue);
	}

	protected DefaultValue defaultToString(final String string) {
		return new DefaultValue() {
			@Override
			public String compute() {
				return string;
			}

			public String toString() {
				return string;
			}
		};
	}

	protected DefaultValue defaultToPrompt(final String prompt) {
		return new DefaultValue() {

			@Override
			public String compute() {
				System.out.print(prompt + " (could have been configured but it was not): ");
				return readFromStdIn();
			}

			private String readFromStdIn() {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				try {
					return br.readLine();
				} catch (IOException ioe) {
					throw new CIException("Could not read input user.");
				}
			}

			public String toString() {
				return "asking for value";
			}
		};
	}

	protected DefaultValue defaultToConfigurationString(final ConfigurationValue value) {
		return new DefaultValue() {

			@Override
			public String compute() {
				return value.getConfigurationString();
			}

			public String toString() {
				return value + " (evaluates to " + compute() + ").";
			}
		};
	}

	protected DefaultValue defaultToConfigurationFile(final FileValue basePath, final String relativePath) {
		return new DefaultValue() {

			@Override
			public String compute() {
				return new File(basePath.getNonExistingFile(), relativePath).getAbsolutePath();
			}

			public String toString() {
				return relativePath + " in " + basePath + " (evaluates to " + compute() + ").";
			}
		};
	}

	protected class ConfigurationValue {
		protected String variableName;
		protected DefaultValue defaultValue;

		protected ConfigurationValue(String variableName, DefaultValue defaultValue) {
			this(variableName);
			this.defaultValue = defaultValue;
		}

		protected ConfigurationValue(String variableName) {
			this.variableName = variableName;
		}

		protected String getConfigurationString() {
			String result = computedConfiguration.get(variableName);

			if (result != null) {
				return result;
			}

			result = System.getProperty(variableName);

			if (result != null) {
				VerboseLogging.log("Configuration variable " + variableName + " was \"" + result
						+ "\" (found in system property).");
			} else {
				result = getProperties().getProperty(variableName);

				if (result != null) {
					result = result.trim();

					VerboseLogging.log("Configuration variable " + variableName + " was \"" + result
							+ "\" (found in properties file " + propertiesFile.getAbsolutePath() + ").");
				}
			}

			if (result == null) {
				String errorMessage = "Did not find a value for " + variableName + " either in the environment nor in "
						+ propertiesFile.getAbsolutePath();

				if (defaultValue == null) {
					throw new NotConfiguredException(errorMessage);
				}

				VerboseLogging.log(errorMessage + ". Using default value " + defaultValue + ".");

				result = defaultValue.compute();
			}

			computedConfiguration.put(variableName, result);

			return result;
		}

		public String toString() {
			return variableName;
		}
	}

	private static Map<String, File> computedFileValue = new HashMap<String, File>();

	public class ConfigurationUrlValue extends ConfigurationValue {
		protected ConfigurationUrlValue(String variableName, DefaultValue defaultValue) {
			super(variableName, defaultValue);
		}

		public URL getValue() {
			try {
				return new URL(getConfigurationString());
			} catch (MalformedURLException e) {
				throw new CIException("Expected the configuration parameter " + variableName + " to be a URL: " + e);
			}
		}
	}

	public interface FileValue {
		File getNonExistingFile();
	}

	public class ConfigurationFileValue extends ConfigurationValue implements FileValue {
		private ConfigurationFileValue relativeTo;

		protected ConfigurationFileValue(String configurationString, DefaultValue defaultValue) {
			this(configurationString, defaultValue, null);
		}

		protected ConfigurationFileValue(String configurationString, DefaultValue defaultValue,
				ConfigurationFileValue relativeTo) {
			super(configurationString, defaultValue);

			this.relativeTo = relativeTo;
		}

		public File getNonExistingFile() {
			File result = computedFileValue.get(variableName);

			if (result == null) {
				result = computeFile();

				computedFileValue.put(variableName, result);
			}

			return result;
		}

		private File computeFile() {
			File result;
			String fileName = getConfigurationString();

			result = new File(fileName);

			if (!result.isAbsolute() && relativeTo != null) {
				VerboseLogging.log("The value of " + variableName
						+ " is not an absolute path; it will be implemented relative to " + relativeTo + ".");

				result = new File(relativeTo.getValue(), fileName);
			}

			return result;
		}

		public File getValue() throws NoSuchFileException {
			File result = getNonExistingFile();

			if (!result.exists()) {
				throw new NoSuchFileException("Expected the file/directory " + result.getAbsolutePath() + " to exist.");
			}

			return result;
		}

		public File getValue(boolean mustExist) {
			if (mustExist) {
				return getValue();
			} else {
				return getNonExistingFile();
			}
		}
	}

	public class ConfigurationStringValue extends ConfigurationValue {
		protected ConfigurationStringValue(String configurationString, DefaultValue defaultValue) {
			super(configurationString, defaultValue);
		}

		protected ConfigurationStringValue(String configurationString) {
			super(configurationString);
		}

		public String getValue() {
			return getConfigurationString();
		}
	}

	public class ConfigurationIntegerValue extends ConfigurationValue {
		protected ConfigurationIntegerValue(String configurationString, DefaultValue defaultValue) {
			super(configurationString, defaultValue);
		}

		public int getValue() {
			String value = getConfigurationString();

			try {
				return parseInt(value);
			} catch (NumberFormatException e) {
				System.err.println("The configuration variable \"" + variableName + "\" had the value " + value
						+ " which is not a number. Assuming \"" + defaultValue + "\".");

				return parseInt(defaultValue.compute());
			}
		}
	}

	public class ConfigurationHostValue extends ConfigurationValue {
		protected ConfigurationHostValue(String configurationString, DefaultValue defaultValue) {
			super(configurationString, defaultValue);
		}

		protected ConfigurationHostValue(String configurationString) {
			super(configurationString);
		}

		public Host getValue() {
			return new Host(getConfigurationString());
		}
	}

	public abstract class ConfigurationListValue<T extends ConfigurationValue> extends ConfigurationValue {
		private String variablePrefix;

		protected ConfigurationListValue(String variableName) {
			super(variableName);

			this.variablePrefix = variableName;
		}

		public List<T> getValue() {
			List<T> result = new ArrayList<T>();

			int i = 0;

			try {
				do {
					variableName = variablePrefix + "." + i;

					getConfigurationString();

					result.add(createValue(variableName));

					i++;

					if (i > 100) {
						System.err
								.println("internal error: list "
										+ variablePrefix
										+ " seems unreasonably long. current value is "
										+ result.get(result.size() - 1)
										+ ". Note that no default value may be configured in the individual entry configuration values.");
						break;
					}
				} while (true);
			} catch (NotConfiguredException e) {
				// fine. end of list.
			}

			if (result.isEmpty()) {
				result.add(createDefaultValue());
			}

			return result;
		}

		protected abstract T createValue(String variableName);

		protected abstract T createDefaultValue();
	}
}
