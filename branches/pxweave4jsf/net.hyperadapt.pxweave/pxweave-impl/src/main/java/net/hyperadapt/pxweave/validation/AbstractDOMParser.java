package net.hyperadapt.pxweave.validation;

import java.lang.reflect.Method;
import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.logger.Logable;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;

public abstract class AbstractDOMParser  extends Logable implements IDOMParser {

	public abstract Document buildDOM(URI documentURI, URI xsdURI) throws XMLWeaverException;

	private Document buildDOM(String documentPath) throws XMLWeaverException {
		LSParser parser = getDefaultParser();
		return parser.parseURI(documentPath);
	}
	
	/* (non-Javadoc)
	 * @see net.hyperadapt.pxweave.validation.IDOMParser#buildDOM(java.lang.String)
	 */
	public Document buildDOM(URI documentURI) throws XMLWeaverException {
		return buildDOM(documentURI.toString());
	}

	
	/**
	 * Loads the LSParser's DOMConfiguration. Supports Java Binding of DOMLevel 3 Draft and Recommendation.
	 * 
	 * 
	 * @param parser - The LSParser
	 * @return The DOMConfiguration for the given parser or null if an error occurred.
	 * 
	 */
	protected DOMConfiguration getDOMConfig(LSParser parser){
		DOMConfiguration config = null;
		try {
			try{
				Method getConfig = parser.getClass().getMethod("getConfig");
				getLogger().warn("Parser implements DOM level 3 draft. Using reflection to access getConfig().");	
				config = (DOMConfiguration)getConfig.invoke(parser);
			}
			catch(NoSuchMethodException e){
				config = parser.getDomConfig();
			}
		} catch (Exception e) {
			getLogger().error("Could not load DOMConfig", e);
		} 
		
		return config;
	}
	
	/**
	 * Creates an LS parser using a non validating default configuration. This method 
	 * can be called by clients to instantiate a basic parser which can be further
	 * configured using {@link AbstractDOMParser.getDOMConfig(LSParser)}
	 * 
	 * @return A pre-configured LS parser
	 * @throws IXMLWeaverException
	 */
	protected LSParser getDefaultParser() throws XMLWeaverException{
		System.setProperty(DOMImplementationRegistry.PROPERTY,getRegistryName());
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		}
		catch (final Exception ex) {
				throw new XMLWeaverException("Could not instanciate LS parser.", ex);
		}
		final DOMImplementationLS impl = (DOMImplementationLS) registry
				.getDOMImplementation(getDOMImplementationName());
		LSParser parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,
				"http://www.w3.org/2001/XMLSchema");
		final DOMConfiguration config = getDOMConfig(parser);
		final DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("namespaces",Boolean.TRUE);
		return parser;
	}
	
	/**
	 * Clients should override this method an return a qualified name for 
	 * their DOMImplementationRegistrySource.
	 * 
	 * @return A qualified DOMImplementationRegistrySource name.
	 */
	protected abstract String getRegistryName();
	
	protected String getDOMImplementationName(){
		return "LS 3.0";
	}
}
