package net.hyperadapt.pxweave.interpreter;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.w3c.dom.Document;

/**
 * @author msteinfeldt
 */
public interface IInterpreterArgument {
	/**
	 * Gets the path to the schema document that types this IInterpreterArgument
	 * @return   the path to the schema
	 */
	public abstract URI getSchemaURI();

	/**
	 * Sets the path to the schema that types this IInterpreterArgument
	 * @param  inputTypeConstraint 
	 */

	public abstract void setSchemaURI(URI schemaURI);


	/**
	 * Gets the id of the argument. For each {@link Aspect} bound
	 * {@link Interface#getCore()} identifies one InterpreterArgument, i.e. one
	 * core document
	 * 
	 * @param id
	 */
	public abstract String getId();
	
	public abstract Document getDocument();
	
	public File getOutputFile();
	
	public File getInputFile();
	
	public OutputStream getOutputStream();
	
	public void setOutputStream(OutputStream outputStream);
	
	public boolean isLoaded();
	
	public void loadDocument(IDOMParser parser, boolean validate) throws XMLWeaverException;

	public abstract void setDocument(Document result);
	
	public boolean isInputStream();

}