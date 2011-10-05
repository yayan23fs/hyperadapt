package net.hyperadapt.pxweave.validation;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.validation.NodeEditVAL;

/**
 * This class provides Methods to validate {@link Element}s and {@link Document}
 * s.
 * 
 */
public abstract class DOMValidator implements IDOMValidator {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.validation.IDOMValidator#validateElement(org.w3c.dom.Element
	 * )
	 */
	public short validateElement(final Element element)
			throws XMLWeaverException {
		final short valid = elementIsValid(element);
		switch (valid) {
		case NodeEditVAL.VAL_TRUE:
			return valid;
		case NodeEditVAL.VAL_FALSE: {
			throw new XMLWeaverException("\"" + element.getTagName()
					+ "\" would be invalid");
		}
		case NodeEditVAL.VAL_UNKNOWN: {
			throw new XMLWeaverException("validity of \""
					+ element.getTagName() + "\" would be unknown");
		}
		default: {
			throw new XMLWeaverException("\"" + element.getTagName()
					+ "\" would not be VAL_TRUE");
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.validation.IDOMValidator#validateDocument(org.w3c.dom.
	 * Document)
	 */
	public short validateDocument(final Document document)
			throws XMLWeaverException {
		final short valid = documentIsValid(document);
		switch (valid) { 
		case NodeEditVAL.VAL_TRUE:
			return valid;
		case NodeEditVAL.VAL_FALSE: {
			throw new XMLWeaverException("Document is invalid.");
		}
		case NodeEditVAL.VAL_UNKNOWN: {
			throw new XMLWeaverException(
					"validity of the documentNode would be unknown");
		}
		default: {
			throw new XMLWeaverException("\" document would not be VAL_TRUE");
		}
		}
	}

	/**
	 * Hook Method; Validates an {@link Element} and returns a {@link Short},
	 * that represents the validation result.
	 * 
	 * @param element
	 * @return
	 */
	protected abstract short elementIsValid(Element element);

	/**
	 * Hook Method; Validates an {@link Document} and returns a {@link Short},
	 * that represents the validation result.
	 * 
	 * @param document
	 * @return
	 */
	protected abstract short documentIsValid(Document document);
}
