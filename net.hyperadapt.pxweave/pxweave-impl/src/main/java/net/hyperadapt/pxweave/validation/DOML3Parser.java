/**
 * 
 */
package net.hyperadapt.pxweave.validation;

import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.validation.DocumentEditVAL;


/**
 * This is a validating IDOMParser, i.e. it validates documents while parsing
 * them. which does not build a {@link Document} which implements
 * {@link DocumentEditVAL}.
 * 
 * 
 */
//TODO more transparent validation handling
public class DOML3Parser extends AbstractDOMParser implements IDOMParser {
	
	private boolean validateOnLoad = true;
	
	public DOML3Parser(){}

	
	public DOML3Parser(boolean validateOnLoad){
		this.validateOnLoad = validateOnLoad;
	}
	
	
	public Document buildDOM(final URI documentURI, final URI xsdURI)throws XMLWeaverException{
		return buildDOM(documentURI.toString(),xsdURI.toString());	
	}

	
	private Document buildDOM(final String documentURI, final String xsdURI)
			throws XMLWeaverException {
			LSParser builder = this.getDefaultParser();
			final DOMConfiguration config = builder.getDomConfig();
			if(validateOnLoad){
				config.setParameter("validate", true);
				config.setParameter("schema-location", xsdURI);
				config.setParameter("schema-type","http://www.w3.org/2001/XMLSchema");

			}
			try{
				return builder.parseURI(documentURI);
			}
			catch(IllegalArgumentException e){
				throw new XMLWeaverException("Document is not valid. Reason was "+e.getMessage()+".",e);
			}
			
	}


	@Override
	protected String getRegistryName() {
		return "org.apache.xerces.dom.DOMXSImplementationSourceImpl";
	}

}
