package net.hyperadapt.pxweave.validation;

import java.net.URI;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.validation.DocumentEditVAL;
import org.w3c.dom.validation.ElementEditVAL;
import org.w3c.dom.validation.NodeEditVAL;

/**
 *Implements a DOM Level 3 Validation Validator. Is able to validate
 * {@link Element}s and {@link Document}s, that implement the
 * {@link DocumentEditVAL} and the {@link ElementEditVAL} interfaces.
 * 
 */
//TODO check whole class

public class DOML3ValidationValidator extends DOMValidator {
	protected DOML3ValidationValidator() {
	}

	/**
	 *Validates an Element
	 * 
	 * @param element
	 *            {@link Element} that is to be validated. The element must be
	 *            an instance of {@linkElementEditVAL}, the method returns
	 *            {@link NodeEditVAL#VAL_UNKNOWN} otherwise
	 *@return a short that represents the validation outcome.
	 */
	@Override
	protected short elementIsValid(final Element element) {
		//TODO check if this works
		if (element instanceof ElementEditVAL) {
			return ((ElementEditVAL) element)
					.nodeValidity(NodeEditVAL.VAL_SCHEMA);
		} else {
			System.out.println("element \"" + element.getNodeName()
					+ "\"is not an instance of ElementEditVAL");
			return NodeEditVAL.VAL_UNKNOWN;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.validation.DOMValidator#documentIsValid(org.w3c.dom.Document
	 * )
	 */
	@Override
	/**
	 * *Validates a DocumentNode
	 * 
	 * @param element {@link Document} that is to be validated.The document must
	 * be an instance of {@link DocumentEditVAL},the method returns {@link
	 * NodeEditVAL#VAL_UNKNOWN} otherwise
	 * 
	 * @return a short that represents the validation outcome.
	 */
	protected short documentIsValid(final Document document) {
		if (document instanceof ElementEditVAL) {
			return ((DocumentEditVAL) document)
					.nodeValidity(NodeEditVAL.VAL_SCHEMA);
		} else {
			throw new IllegalArgumentException(
					"documentNode is not an instance of DocumentEditVAL");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.DOMValidator#needsSchema()
	 */
	@Override
	public boolean needsSchema() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.DOMValidator#setSchema(java.net.URI)
	 */
	@Override
	public void setSchema(URI schemaURI) {
	}
}
