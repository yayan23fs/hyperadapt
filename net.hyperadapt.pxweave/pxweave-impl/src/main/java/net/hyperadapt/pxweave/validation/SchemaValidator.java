package net.hyperadapt.pxweave.validation;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.Document;
import org.w3c.dom.validation.NodeEditVAL;
import org.xml.sax.SAXException;

/**
 * The purpose of this class is to validate an
 * instance of {@link Document} against a xsd-file. Any xsd that is used is
 * parsed and stored as {@link Schema} for later use, so that in case the same
 * schema has to be used again, no parsing is necessary anymore.
 * 
 */
//TODO check this class
public class SchemaValidator {
	private final HashMap<URI, Schema> schemas = new HashMap<URI, Schema>();

	/**
	 * @author  danielkadner
	 */
	private static class Holder {
		/**
		 * @uml.property  name="iNSTANCE"
		 * @uml.associationEnd  
		 */
		private static final SchemaValidator INSTANCE = new SchemaValidator();
	}

	public static SchemaValidator getInstance() {
		return Holder.INSTANCE;
	}

	private SchemaValidator() {
	}

	/**
	 * Checks if a document is valid, throws an {@link IXMLWeaverException} if
	 * not.
	 * 
	 * @param document
	 *            - the {@link Document} that is to be checked
	 * @param schemaPath
	 *            - the path to the xsd-file that is used for the validation
	 * @return
	 * @throws XMLWeaverException
	 */
	public short documentIsValid(Document document, URI schemaURI) throws XMLWeaverException {
	
		if (!schemas.containsKey(schemaURI)) {
			Schema schema = null;
			try {
				final String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
				final SchemaFactory factory = SchemaFactory.newInstance(language);
				schema = factory.newSchema(schemaURI.toURL());
				schemas.put(schemaURI, schema);
			} catch (final Exception e) {
				throw new XMLWeaverException(e);
			}
		}
		
		
		try {
			Schema schema = schemas.get(schemaURI);
			Validator validator = schema.newValidator();
			validator.validate(new DOMSource(document));
		} catch (final SAXException e) {
			throw new XMLWeaverException(e);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return NodeEditVAL.VAL_TRUE;
	}
}
