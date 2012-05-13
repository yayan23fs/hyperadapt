/**
 * 
 */
package net.hyperadapt.pxweave.evaluation;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.w3c.dom.Document;

/**
 * @author msteinfeldt
 */
public interface IXQAbstractArgument {

	public abstract DynamicQueryContext getDynamicQueryContext();

	/**
	 * Gets the {@link Document} the XQuery expression is to be evaluated
	 * against.
	 * 
	 * @return an instance of {@link Document}
	 */
	public abstract Document getDocument();

	/**
	 * Adds a variable and binds its value that is given as an XPath expression.
	 * 
	 * @param name
	 *            The name of the variable
	 * @param xpath
	 *            A String that holds a XPath expression to select a value for
	 *            the variable.
	 * @throws XMLWeaverException
	 */
	public abstract void addVariableAndBindEvValue(final String name,
			final String xpath) throws XMLWeaverException;

	/**
	 * Declares a variable as external variable and binds its value. See
	 * {@link #addToDynamicQueryContext(String, Object)} for details.
	 * 
	 * @param name
	 *            The name of the variable.
	 * @param value
	 *            The value of the variable.
	 */
	public abstract void addVariableAndBindValue(final String name,
			final Object value);

	/**
	 * Sets the "for"-part of an XQuery expression see
	 * {@link #setExpression(String)} for details.
	 * 
	 * @param xpath
	 *            a String representing an XPath expression;
	 */
	public abstract void declarePointcut(final String xpath);

	/**
	 * Sets the expression that is supposed to be evaluated. The expression is part of an XQuery expression that consists of external variable declarations given by   {@link #getExternalVarDecl()}   a "for expression"  {@link #forExpression}   and the expression.
	 * @param expression   a String that resembles the "return"-part of an XQuery  expression.
	 */
	public abstract void setExpression(final String expression);

	/**
	 * This method uses the variables and their values bound to the static and
	 * dynamic context with the methods
	 * {@link #addVariableAndBindEvValue(String, String)} and
	 * {@link #addVariableAndBindValue(String, Object)} to compile an
	 * {@link XQueryExpression}.
	 * 
	 * @return an {@link XQueryExpression} object.
	 * @throws XMLWeaverException 
	 */
	public abstract XQueryExpression getXQExpression() throws XMLWeaverException;

	public abstract String getExternalVarDecl();

	public abstract String getExpression();

}