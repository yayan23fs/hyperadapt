package net.hyperadapt.pxweave.validation;

import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import oracle.xml.parser.schema.XMLSchema;
import oracle.xml.parser.schema.XSDBuilder;
import oracle.xml.parser.v2.XMLDocument;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.SAXParseException;

/**
 * XML parser that uses the Oracle XDK 10g for validation on-the-fly.
 * 
 * @author msteinfeld
 * 
 */
public class DOML3ValidationParser extends AbstractDOMParser implements
		IDOMParser {

	// TODO Improve problem reporting
	private Document buildDOM(final String documentURI, final String schemaURI)
			throws XMLWeaverException {
		LSParser parser = this.getDefaultParser();
		DOMConfiguration config = getDOMConfig(parser);
		config.setParameter("schema-location", schemaURI);
		config.setParameter("well-formed", true);

		XMLSchema schemadoc = null;
		XSDBuilder xsdbuilder = null;

		final XMLDocument document = (XMLDocument) (parser
				.parseURI(documentURI));
		try {
			xsdbuilder = new XSDBuilder();
			schemadoc = (XMLSchema) xsdbuilder.build(schemaURI);
		} catch (final Exception e1) {
			throw new XMLWeaverException("Couldn't build Schema " + schemaURI
					+ ". Reason:" + e1.getMessage(), e1);
		}

		//document.setSchema(schemadoc);
		try {
			final boolean valid = document.validateContent(schemadoc);
			if (!valid) {
				throw new XMLWeaverException("Document \""
						+ document.getLocalName() + "\" is not valid");
			}
		} catch (SAXParseException e) {
			throw new XMLWeaverException("Document  \""
					+ document.getLocalName() + "\"can't be validated ", e);
		}

		return document;
	}

	public Document buildDOM(URI sourceURI, URI schemaURI)
			throws XMLWeaverException {
		return buildDOM(sourceURI.toString(), schemaURI.toString());
	}

	@Override
	protected String getRegistryName() {
		return "oracle.xml.parser.v2.XMLDOMImplementationSource";
	}
}
