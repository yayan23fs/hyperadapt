package net.hyperadapt.pxweave.evaluation;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.logger.Logable;

import org.w3c.dom.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;

/**
 * This class is used to evaluate XPath expressions on a {@link Document}.
 */
public class XPathEvaluator extends Logable{
	
	private static final XPathFactory factory = XPathFactory.newInstance();
	private NamespaceContext nsContext;
	

	public XPathEvaluator(NamespaceContext nsContext) {
		this.nsContext = nsContext;
	}
	
	
	/**
	 * Evaluates a XPath expression given as a String.
	 * 
	 * @param path
	 *            The XPath expression that is supposed to be evaluated.
	 * @param document
	 * @return a {@link NodeList} that contains the result of the evaluation.
	 * @throws XMLWeaverException
	 *             if any {@link XPathExpressionException} occurred.
	 */
	public NodeList evaluateXPath(final String path,final Document document) throws XMLWeaverException {
		final XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(nsContext);
		XPathExpression expr;
		
		try {
			expr = xpath.compile(path);
			final Object result = expr.evaluate(document,
					XPathConstants.NODESET);
			final NodeList nodes = (NodeList) result;
			return nodes;
		} catch (final XPathExpressionException e) {
			getLogger().error("Could not evaluate XPath expression.", e);
			throw new XMLWeaverException("Could not evaluate XPath expression : '" + path
					+ e.getMessage() +"'", e);
		}
	}

}