package net.hyperadapt.pxweave.logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Singleton class used to instantiate the net.hyperadapt.pxweave.aspects.operations
 * net.hyperadapt.pxweave.logger and to retrieve log4j loggers for different logging
 * tasks.
 * <p>
 * 
 * To change the loggers configuration use the static
 * {@link net.hyperadapt.pxweave.aspects.operations.logging.LoggerConfiguration#useDefaultLoggerConfig
 * useDefaultLoggerConfig variable} .
 * 
 * @author C. Bürger
 * 
 */
public class LoggerConfiguration {
	private static LoggerConfiguration instance;
	/**
	 * If useDefaultLoggerConfig == null, the default net.hyperadapt.pxweave.logger
	 * configuration file is used (net.hyperadapt.pxweave.aspects.operations.logging :
	 * net.hyperadapt.pxweave.logger.config). Otherwise the useDefaultLoggerConfig's
	 * String is used as absolut file name for the net.hyperadapt.pxweave.logger
	 * configuration file (UTF-8 encoding).
	 */
	private static String useDefaultLoggerConfig = null;

	/**
	 * Method used to load another log4j net.hyperadapt.pxweave.logger configuration
	 * property file instead the XMLWeavers default ones. If the argument given
	 * is null, the default net.hyperadapt.pxweave.logger configuration is loaded.
	 * 
	 * @param absolutFileName
	 *            The absolut file name (inclusive path) of the property
	 *            configuration file for the log4j loggers. If null, the default
	 *            net.hyperadapt.pxweave.logger is loaded.
	 */
	public static void changeLoggerConfiguration(final String absolutFileName) {
		useDefaultLoggerConfig = absolutFileName;

		final Properties config = new Properties();
		try {
			if (useDefaultLoggerConfig == null) {
				config.load(LoggerConfiguration.class
						.getResourceAsStream("logger.config"));
			} else {
				final Reader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(useDefaultLoggerConfig), "UTF-8"));
				config.load(reader);
			}
		} catch (final IOException exception) {
			String error = null;
			if (useDefaultLoggerConfig == null) {
				error = "FATAL : Not able to load default net.hyperadapt.pxweave.logger configuration.";
			} else {
				error = "ERROR : Not able to load net.hyperadapt.pxweave.logger configuration file "
						+ useDefaultLoggerConfig + " .";
			}
			throw new IllegalArgumentException(error);
		}

		PropertyConfigurator.configure(config);
	}

	private LoggerConfiguration() {
		Logger root = Logger.getRootLogger();
		if (!root.getAllAppenders().hasMoreElements()) {
			changeLoggerConfiguration(useDefaultLoggerConfig);
		}
	}

	/**
	 * Invokes the singleton.
	 * 
	 * @return The unique LoggerConfiguration instance.
	 */
	public static LoggerConfiguration instance() {
		if (instance == null) {
			instance = new LoggerConfiguration();
		}
		return instance;
	}

	/**
	 * Get a net.hyperadapt.pxweave.logger by name.
	 * 
	 * @param loggerName
	 *            The loggers name.
	 * @return The net.hyperadapt.pxweave.logger for the given name.
	 */
	public Logger getLogger(final String loggerName) {
		return Logger.getLogger(loggerName);
	}

	/**
	 * Get a net.hyperadapt.pxweave.logger for a class.
	 * 
	 * @param clazz
	 *            The class to log.
	 * @return A net.hyperadapt.pxweave.logger logging exclusivly the given class.
	 */
	@SuppressWarnings("unchecked")
	public Logger getLogger(final Class clazz) {
		return Logger.getLogger(clazz);
	}

	/**
	 * Returns the net.hyperadapt.pxweave.logger used to log events happening while
	 * executing an xml weaver recipe.
	 * 
	 * @return Logger for logging xml weaving recipe execution.
	 */
	public Logger getExecutionLogger() {
			return Logger
				.getLogger("net.hyperadapt.pxweave.PXWeaveLogger");
	}

	
}