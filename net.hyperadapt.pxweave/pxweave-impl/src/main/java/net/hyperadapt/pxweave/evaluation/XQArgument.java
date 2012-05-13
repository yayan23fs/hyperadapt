package net.hyperadapt.pxweave.evaluation;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

/**
 * This class is part of a decorator pattern - it's the mimicedConcreteClass. It
 * holds a dynamic query context for a XQuery expression, i.e. a
 * {@link DynamicQueryContext} for an {@link XQueryExpression}, an external
 * variable declaration and the associated document for a XQuery expression. It
 * provides access to those by implementing the abstract methods of
 * {@link XQAbstractArgument}.
 */
public class XQArgument extends XQAbstractArgument {
	private final DynamicQueryContext dynamicQueryContext;
	private String externalVarDecl = "";
	private final Document doc;
	private StaticQueryContext sqc;

	/**
	 * Constructor for XQArgument
	 * 
	 * @param doc
	 *            The document for which an XQuery expression is to be evaluated
	 *            against.
	 */
	public XQArgument(final Document doc, NamespaceContext nsContext, NamespaceResolver nsResolver) {
		super(nsContext,nsResolver);
		this.doc = doc;
		final Configuration config = new Configuration();
		sqc = new StaticQueryContext(config);
		sqc.setExternalNamespaceResolver(super.getNamespaceResolver());
		dynamicQueryContext = new DynamicQueryContext(config);
		try {
			final DOMSource domSource = new DOMSource(doc);
			dynamicQueryContext.setContextItem(sqc.buildDocument(domSource));
		} catch (final XPathException e) {
			e.printStackTrace();
		}
	}

	/**
	 * see {@link XQAbstractArgument#addToDynamicQueryContext(String, Object)}
	 **/
	@Override
	protected void addToDynamicQueryContext(final String name,
			final Object value) {
		this.dynamicQueryContext.setParameter(name, value);
	}

	/**
	 * see {@link XQAbstractArgument#addToExternalVarDecl(String)}
	 **/
	@Override
	protected void addToExternalVarDecl(final String externalVarDeclaration) {
		externalVarDecl = externalVarDecl.concat(externalVarDeclaration);
	}

	/**
	 * see {@link XQAbstractArgument#getDocument()}
	 **/
	@Override
	public Document getDocument() {
		return doc;
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
		return externalVarDecl;
	}
}
