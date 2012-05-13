package net.hyperadapt.pxweave.validation;

import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author msteinfeldt
 * 
 */
public interface IDOMValidator {

	/**
	 * States whether the concrete subclass of this class needs a schema to
	 * perform the validations. If so, the schema should be set with the
	 * {@link #setSchema(String)} method.
	 * 
	 * @return
	 */
	public abstract boolean needsSchema();

	/**
	 * Sets the schema that is used for validation if needed. See at
	 * {@link #needsSchema()} This schema is then used for all following
	 * validations until a different schema is set.
	 * 
	 * @param schemaPath
	 *            - the path to the schema file (xsd)
	 */
	public abstract void setSchema(URI schemaURI);

	/**
	 * Template Method; Validates an {@link Element}.
	 * 
	 * @param element
	 * @return {@link NodeEditVAL#VAL_TRUE} if the element is valid.
	 * @throws XMLWeaverException
	 *             if the element is invalid or the validation result is
	 *             unknown.
	 */
	public abstract short validateElement(final Element element)
			throws XMLWeaverException;

	/**
	 * Template Method; Validates an {@link Document}.
	 * 
	 * @param document
	 * @return {@link NodeEditVAL#VAL_TRUE} if the document is valid.
	 * @throws XMLWeaverException
	 *             if the document is invalid or the validation result is
	 *             unknown.
	 */
	public abstract short validateDocument(final Document document)
			throws XMLWeaverException;

}