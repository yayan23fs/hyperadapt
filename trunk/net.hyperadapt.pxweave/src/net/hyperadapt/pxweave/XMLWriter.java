package net.hyperadapt.pxweave;

import java.io.File;

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
	 * Writes a {@link Document} to file.
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
	public void writeToFile(final Document document, final String pathName,
			String encoding, final Boolean prettyPrinting) {
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
		final File fileOutput = new File(pathName);
		final StreamResult strResult = new StreamResult(fileOutput);
		serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
		serializer.setOutputProperty(OutputKeys.INDENT, indent);
		try {
			serializer.transform(domSource, strResult);
			getLogger().info("Writing to file '" + pathName + "'");
		} catch (final TransformerException e) {
			getLogger().error("Could not write to file '" + pathName+"'.",e);
		}
	}

	private XMLWriter() {
		tf = TransformerFactory.newInstance();
		try {
			serializer = tf.newTransformer();
		} catch (final TransformerConfigurationException e) {
			getLogger().error("Could not initialise XMLWriter.",e);
		}

	}

}
