package net.hyperadapt.pxweave.integration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.hyperadapt.pxweave.integration.common.IntegrationConstraints;

/**
 * The helper class loads extern property files to configure the system
 * behavior.
 * 
 * @author Martin Lehmann
 */
public class PropertyHelper {
	private Properties prop;
	private static String resourcePath;
	private static String contextPath;

	/**
	 * Starting with the class path, the resource path and the context path will
	 * be determine.
	 */
	static {
		String classPath = System
				.getProperty(IntegrationConstraints.APPLICATION_CLASSPATH);
		String s = File.separator;
		int endIndex = classPath.indexOf(s
				+ IntegrationConstraints.APPLICATION_FOLDER_BIN);
		classPath = classPath.substring(0, endIndex);
		resourcePath = classPath.concat(s
				+ IntegrationConstraints.APPLICATION_FOLDER_WEBAPPS + s
				+ IntegrationConstraints.APPLICATION_PARAM_CONTEXT + s
				+ IntegrationConstraints.APPLICATION_FOLDER_RESOURCES + s
				+ IntegrationConstraints.APPLICATION_PARAM_PROPFILE);
		contextPath = classPath.concat(s
				+ IntegrationConstraints.APPLICATION_FOLDER_WEBAPPS + s
				+ IntegrationConstraints.APPLICATION_PARAM_CONTEXT + s);
	}

	/**
	 * Constructor method is initialized with the current context name and the
	 * reference to a property file.
	 * 
	 * @param context
	 *            - current context name
	 * @param propFile
	 *            - reference to a property file
	 */
	public PropertyHelper(final String context, final String propFile) {
		loadProperties(context, propFile);
	}

	/**
	 * The property file is loaded with with the current context name and the
	 * reference to this file.
	 * 
	 * @param context
	 *            - current context name
	 * @param propFile
	 *            - reference to a property file
	 */
	public void loadProperties(final String context, final String propFile) {
		prop = new Properties();
		try {
			prop.load(new FileInputStream(getResourcePath(context, propFile)));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * With the help of the context name the resource path of an resource will
	 * be determine.
	 * 
	 * @param context
	 *            - current context name
	 * @param propFile
	 *            - resource name
	 * @return String - path of a resource
	 */
	public static String getResourcePath(final String context,
			final String propFile) {
		String replacePath = resourcePath.replace(
				IntegrationConstraints.APPLICATION_PARAM_CONTEXT, context);
		replacePath = replacePath.replace(
				IntegrationConstraints.APPLICATION_PARAM_PROPFILE, propFile);
		return replacePath;
	}

	/**
	 * With the help of the context name the context path will be determine.
	 * 
	 * @param context
	 *            - current context name
	 * @return String - path of the context
	 */
	public static String getContextPath(final String context) {
		return contextPath.replace(
				IntegrationConstraints.APPLICATION_PARAM_CONTEXT, context);
	}

	/**
	 * Returns the value of a commited key within the property file.
	 * 
	 * @param key - a key within the property file
	 * @param defaultValue - value, if the key doesn't exist
	 * @return String - value of the key
	 */
	private String getProperty(final String key, final String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}
	
	/**
	 * Returns a bool expression with the help of the given key.
	 * 
	 * @param key - a key within the property file
	 * @param defaultValue - value, if the key doesn't exist (true, false)
	 * @return - value of the key (true, false)
	 */
	public Boolean getBooleanProperty(final String key,
			final Boolean defaultValue) {
		String value = getProperty(key, null);
		if (value == null) {
			return defaultValue;
		}

		if (value.equals("true")) {
			return true;
		}

		return false;
	}
}
