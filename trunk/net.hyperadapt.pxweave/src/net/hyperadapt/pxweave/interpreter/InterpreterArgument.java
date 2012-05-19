package net.hyperadapt.pxweave.interpreter;

import java.io.File;
import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.w3c.dom.Document;

/**
 * This class is an abstract super class of all concrete interpreterArguments
 * and provides shared mehtods for those.
 */

public class InterpreterArgument implements IInterpreterArgument {
	
	private URI schemaURI;
	private String id;
	
	private Document document;
	
	private File input;
	private File output;
	

	public InterpreterArgument(final String id) {
		this.id = id;
	}
	
	public InterpreterArgument(Document document, String id){
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
	public InterpreterArgument(File input,File output,String id) {
		this.id = id;
		this.input = input;
		this.output = output;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.interpreter.IInterpreterArgument#setInputTypeConstraint
	 * (java.lang.String)
	 */
	public void setSchemaURI(URI schemaURI) {
		this.schemaURI = schemaURI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.interpreter.IInterpreterArgument#setId(java.lang.String)
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
	 * @return   {@link Document}  
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the Document
	 * @param  document
	 */
	public void setDocument(final Document document) {
		this.document = document;
	}
	
	public File getInputFile() {
		return input;
	}

	public void setInputFile(final File input) {
		this.input = input;
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.interpreter.IInterpreterArgument#getInputTypeConstraint()
	 */
	public URI getSchemaURI() {
		return schemaURI;
	}

	@Override
	public void loadDocument(IDOMParser parser,boolean validate) throws XMLWeaverException{
		if(document==null && input!=null){		
			if(validate){
				this.document = parser.buildDOM(input.toURI(),this.getSchemaURI());
			}
			else{
				this.document = parser.buildDOM(input.toURI());
			}
		}	
	}

	@Override
	public boolean isLoaded() {
		return document!=null;
	}


}
