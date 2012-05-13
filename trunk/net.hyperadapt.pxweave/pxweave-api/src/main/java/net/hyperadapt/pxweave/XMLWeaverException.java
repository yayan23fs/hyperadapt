package net.hyperadapt.pxweave;

import org.apache.log4j.Logger;


/**
 * Every syntactic or semantic error of an xml weaving recipe will cause an
 * XMLWeaverException. The behavior is like the {@link java.lang.Exception
 * Exception} class ones.
 * <p>
 * 
 * The XMLWeaverException class supports logging exceptions. For details see
 * {@link net.hyperadapt.pxweave.aspects.operations.logging.LoggerConfiguration
 * LoggerConfiguration} .
 * 
 * @author cbuerger
 * 
 */
final public class XMLWeaverException extends Exception {
	public final static long serialVersionUID = 1L;

	/**
	 * See {@link java.lang.Exception#Exception(String) method}.
	 */
	public XMLWeaverException(final String errorMessage) {
		super(errorMessage);
	}

	/**
	 * See {@link java.lang.Exception#Exception(Throwable) method}.
	 */
	public XMLWeaverException(final Throwable nestedError) {
		super(nestedError);
	}

	/**
	 * See {@link java.lang.Exception#Exception(String, Throwable) method}.
	 */
	public XMLWeaverException(final String errorMessage,
			final Throwable nestedError) {
		super(errorMessage, nestedError);
	}

	/**
	 * See {@link java.lang.Exception#Exception(String) method}.
	 * <p>
	 * 
	 * In addition to a standard exception logging can be done.
	 */
	public XMLWeaverException(final String errorMessage, final Logger logger) {
		super(errorMessage);
		logger.error(errorMessage);
	} 

	/**
	 * See {@link java.lang.Exception#Exception(Throwable) method}.
	 * <p>
	 * 
	 * In addition to a standard exception logging can be done.
	 */
	public XMLWeaverException(final Throwable nestedError, final Logger logger) { 
		super(nestedError);
		logger.error(this.getMessage());
	}

	/**
	 * See {@link java.lang.Exception#Exception(String, Throwable) method}.
	 * <p>
	 * 
	 * In addition to a standard exception logging can be done.
	 */
	public XMLWeaverException(final String errorMessage,
			final Throwable nestedError, final Logger logger) {
		super(errorMessage, nestedError);
		logger.error(errorMessage);
	}
}