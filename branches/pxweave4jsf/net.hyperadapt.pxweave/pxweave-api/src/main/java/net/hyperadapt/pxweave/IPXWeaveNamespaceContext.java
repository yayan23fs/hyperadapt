package net.hyperadapt.pxweave;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import net.sf.saxon.om.NamespaceResolver;

@SuppressWarnings("restriction")
public interface IPXWeaveNamespaceContext extends NamespaceContext,
		NamespaceResolver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public abstract String getNamespaceURI(String prefix);

	public abstract Map<String, URI> getNamespaces();

	public abstract Map<URI, URI> getDefinitions();

	public abstract URI getDefinitionURI(URI namespace);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public abstract String getPrefix(String namespaceURI);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	public abstract Iterator<String> getPrefixes(String namespaceURI);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.saxon.om.NamespaceResolver#getURIForPrefix(java.lang.String,
	 * boolean)
	 */
	public abstract String getURIForPrefix(String prefix, boolean useDefault);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.saxon.om.NamespaceResolver#iteratePrefixes()
	 */
	public abstract Iterator<String> iteratePrefixes();

}