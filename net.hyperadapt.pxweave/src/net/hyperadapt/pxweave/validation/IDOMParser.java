/**
 * 
 */
package net.hyperadapt.pxweave.validation;

import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.Document;

/**
 * Builds the DOM, i.e. the {@link Document}, validates the XML-Document during
 * parsing
 */
public interface IDOMParser {
	/**
	 * Builds the DOM, i.e. the {@link Document}, validates the XML-Document
	 * during parsing
	 * 
	 * @param documentPath
	 *            - the path to the Document
	 * @param xsdPath
	 *            - the path to the schema-file (xsd)
	 * @return a {@link Document}
	 * @throws XMLWeaverException
	 *             if the document is not valid
	 */
	public Document buildDOM(URI documentURI, URI xsdURI)
			throws XMLWeaverException;
	/**
	 * Builds the DOM, i.e. the {@link Document} without validating
	 * 
	 * @param documentPath
	 *            - the path to the Document
	 * @return a {@link Document}
	 * @throws XMLWeaverException
	 *             if the document is not valid
	 */
	public Document buildDOM(URI documentURI) throws XMLWeaverException;
}
