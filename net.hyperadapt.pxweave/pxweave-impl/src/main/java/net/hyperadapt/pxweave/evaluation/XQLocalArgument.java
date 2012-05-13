/**
 * 
 */
package net.hyperadapt.pxweave.evaluation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.trans.XPathException;

/**
 * This class is part of a decorator pattern - it's the decorator.
 * 
 * It can be used to decorate an instance of {@link XQArgument} i.e. to decorate
 * its {@link #externalVarDecl} and its {@link #dynamicQueryContext}. This way
 * it can reuse and extend the external variable declaration and the bound
 * values of the decorated XQArgument to create a local XQuery expression that
 * contains bound variables from a global scope.
 * 
 */
public class XQLocalArgument extends XQAbstractArgument {
	private String externalVarDecl = "";
	private final IXQAbstractArgument xqArgument;
	private final DynamicQueryContext dynamicQueryContext;

	/**
	 * Constructor of the XQLocalArgument.
	 * 
	 * @param argument
	 *            The {@link XQAbstractArgument} that is to decorated.
	 */
	@SuppressWarnings("unchecked")
	public XQLocalArgument(final IXQAbstractArgument argument, NamespaceContext nsContext, NamespaceResolver nsResolver) {
		super(nsContext, nsResolver);
		this.xqArgument = argument;
		final DynamicQueryContext dqc = argument.getDynamicQueryContext();

		final Configuration config = new Configuration();
		final StaticQueryContext sqc = new StaticQueryContext(config);
		sqc.setExternalNamespaceResolver(super.getNamespaceResolver());
		dynamicQueryContext = new DynamicQueryContext(config);
		
		final DOMSource domSource = new DOMSource(argument.getDocument());
		try {
			dynamicQueryContext.setContextItem(sqc.buildDocument(domSource));
		} catch (final XPathException e) {
			e.printStackTrace();
		}
		
		final HashMap<String, Object> parameterValueMap = dqc.getParameters();
		final Set<String> parameters = parameterValueMap.keySet();
		final Iterator<String> iter = parameters.iterator();
		while (iter.hasNext()) {
			final String paraName = iter.next();
			final Object o = parameterValueMap.get(paraName);
			this.addToDynamicQueryContext(paraName, o);
		}

	}

	/**
	 * see {@link XQAbstractArgument#addToDynamicQueryContext(String, Object)}
	 **/
	@Override
	protected void addToDynamicQueryContext(final String name,
			final Object value) {
		dynamicQueryContext.setParameter(name, value);
	}

	/**
	 * see {@link XQAbstractArgument#getDocument()}
	 **/
	@Override
	public Document getDocument() {
		return xqArgument.getDocument();
	}

	/**
	 * see {@link XQAbstractArgument#getDynamicQueryContext()}
	 **/
	@Override
	public DynamicQueryContext getDynamicQueryContext() {
		return this.dynamicQueryContext;
	}

	/**
	 * see {@link XQAbstractArgument#getExternalVarDecl()}
	 **/
	@Override
	public String getExternalVarDecl() {
		return xqArgument.getExternalVarDecl().concat(externalVarDecl);
	}

	/**
	 * see {@link XQAbstractArgument#addToExternalVarDecl(String)}
	 **/
	@Override
	protected void addToExternalVarDecl(final String externalVarDeclaration) {
		externalVarDecl = externalVarDecl.concat(externalVarDeclaration);
	}

}
