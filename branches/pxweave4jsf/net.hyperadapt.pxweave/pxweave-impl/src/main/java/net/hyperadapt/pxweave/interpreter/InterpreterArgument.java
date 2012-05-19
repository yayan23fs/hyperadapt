package net.hyperadapt.pxweave.interpreter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is an abstract super class of all concrete interpreterArguments
 * and provides shared mehtods for those.
 */

public class InterpreterArgument implements IInterpreterArgument {

	private URI schemaURI;
	private String id;

	private Document document;

	private File inputFile;
	private File output;

	private InputStream inputStream;
	private boolean handleStreams = false;
	private OutputStream outputStream;
	
	public boolean isInputStream() {
		return handleStreams;
	}

	public InterpreterArgument(final String id) {
		this.id = id;
	}

	public InterpreterArgument(Document document, String id) {
		this.document = document;
		this.id = id;
	}

	/**
	 * Creates an {@link InterpreterFileArgument}.
	 * 
	 * @param input
	 *            - the associated input file
	 * @param output
	 *            - the associated output file
	 * @param id
	 *            - the id of the core, i.e., the input file
	 */
	public InterpreterArgument(File input, File output, String id) {
		this.id = id;
		this.inputFile = input;
		this.output = output;
	}

	public InterpreterArgument(InputStream input, String id) {
		this.id = id;
		this.inputStream = input;
		handleStreams = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.interpreter.IInterpreterArgument#
	 * setInputTypeConstraint (java.lang.String)
	 */
	public void setSchemaURI(URI schemaURI) {
		this.schemaURI = schemaURI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.interpreter.IInterpreterArgument#setId(java.lang
	 * .String)
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.interpreter.IInterpreterArgument#getId()
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the document
	 * 
	 * @return {@link Document}
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the Document
	 * 
	 * @param document
	 */
	public void setDocument(final Document document) {
		this.document = document;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(final File input) {
		this.inputFile = input;
	}

	public File getOutputFile() {
		return output;
	}

	/**
	 * Sets the output for this {@link InterpreterFileArgument}
	 * 
	 * @param output
	 */
	public void setOutputFile(final File output) {
		this.output = output;
	}
	

	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void setOutputStream(OutputStream aOutputStream) {
		outputStream = aOutputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.interpreter.IInterpreterArgument#
	 * getInputTypeConstraint()
	 */
	public URI getSchemaURI() {
		return schemaURI;
	}

	public void loadDocument(IDOMParser parser, boolean validate)
			throws XMLWeaverException {
		if (document == null && inputFile != null && !handleStreams) {
			if (validate) {
				this.document = parser.buildDOM(inputFile.toURI(),
						this.getSchemaURI());
			} else {
				// this.document = parser.buildDOM(input.toURI());

				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					factory.setNamespaceAware(true);
					factory.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
					DocumentBuilder builder = factory.newDocumentBuilder();
					this.document = builder.parse(inputFile);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} else if (document == null && inputFile == null && handleStreams) {
			try {
				InputSource inSource = new InputSource(new InputStreamReader(inputStream, "UTF-8")); 
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				factory.setFeature(
						"http://apache.org/xml/features/nonvalidating/load-external-dtd",
						false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				this.document = builder.parse(inSource);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isLoaded() {
		return document != null;
	}

}
