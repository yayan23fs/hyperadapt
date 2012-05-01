package net.hyperadapt.pxweave.integration.generic;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper class, which keeps open the response stream for adaptation purposes.
 * 
 * @author Martin Lehmann
 * 
 */
public class PXWeaveResponseWrapper extends HttpServletResponseWrapper {
	private CharArrayWriter output;

	/**
	 * Method to get the response stream in the form of a string value.
	 */
	public String toString() {
		return output.toString();
	}

	/**
	 * Constructor method initialized the wrapper and act as a response during
	 * the processing of the webframework.
	 * 
	 * @param response
	 *            - which will be wrapped
	 */
	public PXWeaveResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new CharArrayWriter();
	}

	/**
	 * The returning writer allows the manipulation of the response stream.
	 */
	public PrintWriter getWriter() {
		return new PrintWriter(output);
	}
}