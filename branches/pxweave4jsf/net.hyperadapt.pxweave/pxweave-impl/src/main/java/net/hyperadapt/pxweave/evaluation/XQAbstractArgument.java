package net.hyperadapt.pxweave.evaluation;

import javax.xml.namespace.NamespaceContext;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is part of a decorator pattern - it's the abstractMimicedClass. It
 * provides functionality to manipulate a static and dynamic query context and
 * create an instance of {@link XQueryExpression}
 */
public abstract class XQAbstractArgument implements IXQAbstractArgument {
	
	private String forExpression = "";
	private String expression = "";
	private final Configuration config = new Configuration();
	private final StaticQueryContext sqc = new StaticQueryContext(config);
	private NamespaceContext nsContext;
	private NamespaceResolver nsResolver;
	private XPathEvaluator xPathEvaluator;
	
	public XQAbstractArgument(NamespaceContext nsContext, NamespaceResolver nsResolver){
		this.nsResolver = nsResolver;
		this.nsContext = nsContext;
		xPathEvaluator = new XPathEvaluator(nsContext);
	}
	
		public void addVariableAndBindEvValue(final String name, final String xpath)
			throws XMLWeaverException {
		// evaluates each variable with xpathEvaluator and binds them
		// can't us the regular let-mechanism since the multiplicity of the
		// resulting variable bindings must be checked
		Node resultNode = null;
		try {
			final NodeList result = xPathEvaluator.evaluateXPath(xpath,
					getDocument());
			if (result.getLength() != 1) {
				throw new IllegalArgumentException(
						"Evaluation of xpath did not result in a unique value or is null---xpath:"
								+ xpath);
			} else {
				if (result.item(0) instanceof Attr) {
					resultNode = getDocument().createTextNode(
							result.item(0).getNodeValue());
				} else {
					resultNode = result.item(0);
				}
			}

		} catch (final Exception e) {
			throw new XMLWeaverException("Can't bind value of variable \""
					+ name + "\"  because: " + e.getMessage());
		}

		addToExternalVarDecl("declare variable $" + name + " external;");
		addToDynamicQueryContext(name, resultNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#addVariableAndBindValue
	 * (java.lang.String, java.lang.Object)
	 */
	public void addVariableAndBindValue(final String name, final Object value) {
		addToDynamicQueryContext(name, value);
		addToExternalVarDecl("declare variable $" + name + " external;");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#declarePointcut(java.lang
	 * .String)
	 */
	public void declarePointcut(final String xpath) {
		forExpression = "for $joinPoint in " + xpath + " ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#setExpression(java.lang
	 * .String)
	 */
	public void setExpression(final String expression) {
		this.expression = expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#setExpression(java.lang
	 * .String)
	 */
	public String getExpression() {
		return this.expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getXQExpression()
	 */
	public XQueryExpression getXQExpression() throws XMLWeaverException {
		XQueryExpression xqe = null;
		if (!forExpression.equals("")) {
			expression = "return ".concat(expression);
		}
		// dirty bugfix: replaces the xmlns that has gotten into the expression somehow
		//**
		String sub=expression.replaceFirst("xmlns=\"http://www.hyperadapt.org/aspects\" ", "");
		expression = sub;
		//**
		
		try {
			sqc.setExternalNamespaceResolver(nsResolver);
			String query = getExternalVarDecl() + forExpression+ expression;
			xqe = sqc.compileQuery(query);
		} catch (final XPathException e) {
			throw new XMLWeaverException("Error during XQuery compilation.",e);
		}
		return xqe;
	}
	
	protected NamespaceContext getNamespaceContext(){
		return nsContext;
	}
	
	protected NamespaceResolver getNamespaceResolver(){
		return nsResolver;
	}
	
	protected XPathEvaluator getXPathEvaluator(){
		return xPathEvaluator;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getDynamicQueryContext()
	 */
	public abstract DynamicQueryContext getDynamicQueryContext();

	/**
	 * Gets the externalVarDecl, a String that contains the declarations for
	 * external variables in a XQuery. For more details see
	 * {@link #setExpression(String)}.
	 * 
	 * @return String that contains the external variable declaration
	 */
	public abstract String getExternalVarDecl();

	/**
	 * Adds a variable to the external variable declaration. For more details
	 * see {@link #setExpression(String)}
	 * 
	 * @param externalVarDecl
	 *            The declaration that is supposed to be added.
	 */
	protected abstract void addToExternalVarDecl(String externalVarDecl);

	/**
	 * Adds a value to the variable given by its name to the dynamic context.
	 * 
	 * @param name
	 *            The name of the variable.
	 * @param value
	 *            The associated value.
	 */
	protected abstract void addToDynamicQueryContext(String name, Object value);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getDocument()
	 */
	public abstract Document getDocument();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#addVariableAndBindEvValue
	 * (java.lang.String, java.lang.String)
	 */


}
