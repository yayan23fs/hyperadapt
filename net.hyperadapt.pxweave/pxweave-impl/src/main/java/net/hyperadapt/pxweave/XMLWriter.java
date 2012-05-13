package net.hyperadapt.pxweave;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.logger.Logable;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This Singleton class is used to write a {@link Document} to the given file.
 * 
 * @author msteinfeldt
 */
public class XMLWriter extends Logable {

	private Transformer serializer;
	private final TransformerFactory tf;

	private static class Holder {
		private static final XMLWriter INSTANCE = new XMLWriter();
	}

	/**
	 * Get an Instance of {@link XMLWriter}
	 * 
	 * @return an instance of {@link XMLWriter}
	 */
	public static XMLWriter getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Writes a {@link Document} to file or in a stream.
	 * 
	 * @param document
	 *            - the {@link Document}
	 * @param pathName
	 *            - the path for the file
	 * @param encoding
	 *            - the encoding that will be used
	 * @param prettyPrinting
	 *            - turn prettyPrinting on/off
	 */
	public OutputStream write(final IInterpreterArgument argument,
			final Boolean prettyPrinting) {

		String pathName = argument.getOutputFile() != null ? argument
				.getOutputFile().getAbsolutePath() : null;
		// if (pathName == null || !new File(pathName).exists()) {
			Document document = argument.getDocument();
			String encoding = argument.getDocument().getXmlEncoding();

			if (encoding == null) {
				encoding = "UTF-8";
			}
			String indent = null;
			if (prettyPrinting) {
				indent = "yes";
			} else {
				indent = "no";
			}
			final DOMSource domSource = new DOMSource(document);

			StreamResult strResult = null;
			if (pathName != null) {
				final File fileOutput = new File(pathName);
				strResult = new StreamResult(fileOutput);
			} else {
				final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				strResult = new StreamResult(stream);
			}
			serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
			serializer.setOutputProperty(OutputKeys.INDENT, indent);
			try {
				serializer.transform(domSource, strResult);
				getLogger().info("Writing to file '" + pathName + "'");
			} catch (final TransformerException e) {
				getLogger().error(
						"Could not write to file '" + pathName + "'.", e);
			}
			return strResult.getOutputStream();
//		}
//		return null;
	}

	private XMLWriter() {
		tf = TransformerFactory.newInstance();
		try {
			serializer = tf.newTransformer();
		} catch (final TransformerConfigurationException e) {
			getLogger().error("Could not initialise XMLWriter.", e);
		}

	}

}
