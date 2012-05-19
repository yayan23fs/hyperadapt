/**
 * 
 */
package net.hyperadapt.pxweave.evaluation;

import java.util.ArrayList;
import java.util.Properties;

import javax.xml.transform.dom.DOMResult;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This Class provides methods to evaluate a {@link XQueryExpression} that is
 * encapsulated in a {@link XQAbstractArgument}. The static methods can be used
 * depending on the desired result to evaluate such a query.
 */
public class XQEvaluator {

	/**
	 * This method evaluates a {@link XQueryExpression} that is encapsulated in
	 * a {@link XQAbstractArgument}.
	 * 
	 * @param xqAbstractArgument
	 *            A {@link XQAbstractArgument} that encapsulates the
	 *            {@link XQueryExpression}
	 * @return an {@link ArrayList} that contains the results
	 * @throws IllegalArgumentException
	 *             if the underlying expression can't be evaluated.
	 * @throws XMLWeaverException
	 *             if one of the results is not an instance of {@link Element}.
	 */
	public static ArrayList<Element> evaluateToElementNodes(
			final IXQAbstractArgument xqAbstractArgument)
			throws IllegalArgumentException, XMLWeaverException {
		final ArrayList<Element> nl = new ArrayList<Element>();
		try {
			final Document doc = xqAbstractArgument.getDocument();
			final DynamicQueryContext dynamicQueryContext = xqAbstractArgument
					.getDynamicQueryContext();
			final DOMResult result = new DOMResult(doc.createElement("wrapper"));
			final XQueryExpression xqe = xqAbstractArgument.getXQExpression();
			xqe.run(dynamicQueryContext, result, new Properties());
			final NodeList resultList = result.getNode().getChildNodes();
			for (int i = 0; i < result.getNode().getChildNodes().getLength(); i++) {
				if (resultList.item(i) instanceof Element) {
					final Element element = (Element) resultList.item(i);
					nl.add(element);
				} else {
					throw new XMLWeaverException("result is not an element.");
				}
			}
		} catch (final XPathException e) {
			throw new IllegalArgumentException("xpath can't be evaluated"
					+ e.getMessage());
		}
		return nl;
	}

	/**
	 * This method evaluates a {@link XQueryExpression} that is encapsulated in
	 * a {@link XQAbstractArgument}.
	 * 
	 * @param xqAbstractArgument
	 *            A {@link XQAbstractArgument} that encapsulates the
	 *            {@link XQueryExpression}
	 * @return an {@link ArrayList} that contains the results
	 * @throws IllegalArgumentException
	 *             if the underlying expression can't be evaluated.
	 * @throws XMLWeaverException
	 *             if one of the results is not an instance of {@link Node}.
	 */
	public static ArrayList<Node> evaluateToNodes(
			final IXQAbstractArgument xqAbstractArgument)
			throws IllegalArgumentException, XMLWeaverException {
		final ArrayList<Node> nl = new ArrayList<Node>();
		try {
			final DynamicQueryContext dynamicQueryContext = xqAbstractArgument
					.getDynamicQueryContext();
			final Document doc = xqAbstractArgument.getDocument();
			final XQueryExpression xqe = xqAbstractArgument.getXQExpression();

			final DOMResult result = new DOMResult(doc.createElement("wrapper"));

			xqe.run(dynamicQueryContext, result, new Properties());
			final NodeList resultList = result.getNode().getChildNodes();
			for (int i = 0; i < result.getNode().getChildNodes().getLength(); i++) {
				if (resultList.item(i) instanceof Node) {
					final Node node = resultList.item(i);
					nl.add(node);
				} else {
					throw new XMLWeaverException("result is not a Node.");
				}
			}
		} catch (final XPathException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return nl;
	}

	/**
	 * This method evaluates a {@link XQueryExpression} that is encapsulated in
	 * a {@link XQAbstractArgument}. Results that are instances of {@link Attr}
	 * are transformed to an instance of {@link Text}, i.e. the value of the
	 * attribute is used to create a new textNode with the nodeValue value.
	 * 
	 * @param xqAbstractArgument
	 *            A {@link XQAbstractArgument} that encapsulates the
	 *            {@link XQueryExpression}
	 * @return an {@link ArrayList} that contains the results
	 * @throws IllegalArgumentException
	 *             if the underlying expression can't be evaluated.
	 * @throws XMLWeaverException
	 *             if one of the results is not an instance of {@link Text} or
	 *             {@link Attr}
	 */
	public static ArrayList<Text> evaluateToTextNodes(
			final IXQAbstractArgument xqAbstractArgument)
			throws XMLWeaverException, IllegalArgumentException {
		final ArrayList<Text> nl = new ArrayList<Text>();
		try {
			final Document doc = xqAbstractArgument.getDocument();
			final DynamicQueryContext dynamicQueryContext = xqAbstractArgument
					.getDynamicQueryContext();
			final XQueryExpression xQueryExpression = xqAbstractArgument.getXQExpression();
			
			final SequenceIterator it = xQueryExpression.iterator(dynamicQueryContext);
			
			while (true) {
				final Object result = it.next();
				if (result == null) {
					break;
				}
				if (result instanceof AtomicValue) {
					nl.add(doc.createTextNode(((AtomicValue) result)
							.getStringValue()));
				} else {
					final Object o = ((NodeWrapper) result).getUnderlyingNode();
					if (o instanceof Text) {
						final Text text = (Text) o;
						if (text.getOwnerDocument() != doc) {
							nl.add((Text) doc.importNode(text, false));
						} else {
							nl.add((Text) text.cloneNode(false));
						}
					} else if (o instanceof Attr) {
						final Attr attr = (Attr) o;
						nl.add(doc.createTextNode(attr.getNodeValue()));

					} else {
						throw new XMLWeaverException(
								"Result must not be an elementNode");
					}
				}
			}
		} catch (final XPathException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		return nl;
	}
}
