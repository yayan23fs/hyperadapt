package net.hyperadapt.pxweave.validation;


import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.validation.DocumentEditVAL;
import org.w3c.dom.validation.NodeEditVAL;
import org.xml.sax.SAXException;

/**
 * Validator for DOM, Parser does not need to provide an implementation of
 * {@link DocumentEditVAL} for the validation methods of this validator.
 * 
 */
public class DOML3Validator extends DOMValidator {
	private final HashMap<URI, Schema> schemas = new HashMap<URI, Schema>();
	private Schema schema;
	private SchemaFactory factory;
	private final String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	private Validator validator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.validation.DOMValidator#validateElement(org.w3c.dom.Element
	 * )
	 */
	@Override
	protected short elementIsValid(final Element element) {
		try {
			validator.validate(new DOMSource(element));
		} catch (final SAXException e) {
			return NodeEditVAL.VAL_FALSE;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return NodeEditVAL.VAL_TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.validation.DOMValidator#documentIsValid(org.w3c.dom.Document
	 * )
	 */
	@Override
	protected short documentIsValid(final Document document) {
		try {
			validator.validate(new DOMSource(document));
		} catch (final SAXException e) {
			return NodeEditVAL.VAL_FALSE;
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
		return NodeEditVAL.VAL_TRUE;
	}

	protected DOML3Validator() {

	}

	public void setSchema(URI schemaURI) {
		if (!schemas.containsKey(schemaURI)) {
			Schema newSchema = null;
			try {
				factory = SchemaFactory.newInstance(language);
				newSchema = factory.newSchema(schemaURI.toURL());
				schemas.put(schemaURI, newSchema);

			} catch (final Exception e) {
				throw new IllegalArgumentException(e);
			}
		} 
		schema = schemas.get(schemaURI);
		validator = schema.newValidator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.DOMValidator#needsSchema()
	 */
	public boolean needsSchema() {
		return true;
	}

}
