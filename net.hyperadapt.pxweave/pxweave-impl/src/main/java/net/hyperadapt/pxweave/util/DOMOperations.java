/**
 * 
 */
package net.hyperadapt.pxweave.util;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.logger.LoggerConfiguration;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class for working with W3C's DOM
 * 
 */
public class DOMOperations {
	private static Logger logger = LoggerConfiguration.instance().getLogger(DOMOperations.class);

	/**
	 * This method uses a {@link Transformer} to generate a String
	 * representation of the given {@link Node}
	 * 
	 * @param node
	 *            {@link Node} that is to be represented as a String
	 * @return String representation of the Node node
	 * @throws IXMLWeaverException
	 *             if there was an error during the transformation
	 */
	public static String convertNodeToString(final Node node)
			throws XMLWeaverException {
		try {
			final Source source = new DOMSource(node);
			final StringWriter stringWriter = new StringWriter();
			final Result result = new StreamResult(stringWriter);
			final TransformerFactory factory = TransformerFactory.newInstance();
			final Transformer transformer = factory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (Exception e) {
			throw new XMLWeaverException(
					"Unable to create a String representation of node '"
							+ node.getNodeName() + "'.", e);
		}
	}

	/**
	 * This method returns a string representation for the different node types,
	 * "attributeNode" for Node.Attribute_Node for example.
	 * 
	 * @param nodeType
	 *            - The node type as short according to {@link Node}
	 * @return - The string naming the node's type
	 */
	public static String getNodeTypeAsPhrase(final short nodeType) {
		switch (nodeType) {
		case Node.ATTRIBUTE_NODE:
			return "attribute";
		case Node.ELEMENT_NODE:
			return "element";
		case Node.DOCUMENT_NODE:
			return "document";
		case Node.PROCESSING_INSTRUCTION_NODE:
			return "processing instruction";
		case Node.TEXT_NODE:
			return "text";
		case Node.COMMENT_NODE:
			return "comment";
		default:
			return "unknown";
		}
	}

	/**
	 * Checks type of a node, throws an exception if type is other than
	 * expected.
	 * 
	 * @param node
	 *            - The {@link Node} which is to be type-checked
	 * @param expectedType
	 *            - A list of short arguments representing the node's allowed
	 *            types according to {@link Node}
	 * @throws IXMLWeaverException
	 *             - If the node's type is not equal to expectedType.
	 */
	public static void checkNodeType(final Node node,
			final short... expectedType) throws XMLWeaverException {
		boolean typeOK = false;
		String phraseTypes = "";
		for (final short type : expectedType) {
			phraseTypes = phraseTypes.concat(DOMOperations
					.getNodeTypeAsPhrase(type)
					+ ",");
			if (node.getNodeType() == type) {
				typeOK = true;
			}
		}
		if (!typeOK) {
			throw new XMLWeaverException(
					"Unexpected node type. One of the following was expected:"
							+ phraseTypes + ". Actual type is:"
							+ DOMOperations.getNodeTypeAsPhrase(node.getNodeType())+".");
		}
	}

	/**
	 * Checks type of a node, throws an exception if type is other than
	 * expected.
	 * 
	 * @param node
	 *            - The {@link Node} which is to be type-checked
	 * @param expectedType
	 *            - A short representing the node's allowed type according to
	 *            {@link Node}
	 * @throws IXMLWeaverException
	 *             - If the node's type is not equal to the expectedType.
	 */
	public static void checkNodeType(final Node node, final short expectedType)
			throws XMLWeaverException {
		if (node.getNodeType() != expectedType) {
			throw new XMLWeaverException("Unexpected node type. One of the following was expected:"
					+ DOMOperations.getNodeTypeAsPhrase(expectedType)
					+ "Actual type is: "
					+ DOMOperations.getNodeTypeAsPhrase(node.getNodeType()));
		}
	}

	/**
	 * Inserts a {@link Node} in the given {@link Node} at the given position.
	 * For any negative value the node is appended. Positive values of position
	 * must not exceed the number of child nodes in the target node. The
	 * inserted Node must not be an {@link Attr}.
	 * 
	 * @param target
	 *            An instance of {@link Node} in which the node is to be
	 *            inserted.
	 * @param insertedNode
	 *            An instance of {@link Node} that is supposed to be inserted.
	 * @param position
	 *            The position at which the node is to be inserted - 0 for first
	 *            position.
	 * @throws IXMLWeaverException
	 *             if the number of children is smaller than the given position,
	 *             if target and source nodes are the same or the inserted node
	 *             contains the target.
	 */
	public static void insertNode(final Node target, final Node insertedNode,
			final int position) throws XMLWeaverException {
		logger.debug("Inserting "+ getNodeTypeAsPhrase(insertedNode.getNodeType()) +" node with name "
				+ insertedNode.getNodeName() +".");
		if (position > target.getChildNodes().getLength()) {
			throw new XMLWeaverException(
					"Position of Node in target exceeds number of children in target");
		}
		if (target.isSameNode(insertedNode)) {
			throw new XMLWeaverException(
					"Source elementNode and target elementNode must not be equal each other");
		}
		// append for negative positions
		if (position < 0 || position == target.getChildNodes().getLength()) {
			try {
				target.appendChild(insertedNode);
			} catch (final DOMException e) {
				if (e.code == DOMException.HIERARCHY_REQUEST_ERR) {
					throw new XMLWeaverException(
							"HIERARCHY_REQUEST_ERR, relation of position of source and target is not allowed");
				} else {
					throw new DOMException(e.code, e.getLocalizedMessage());
				}
			}
		} else {
			final NodeList targetChildren = target.getChildNodes();
			if (targetChildren.getLength() == 0) {
				target.appendChild(insertedNode);
			} else {
				target
						.insertBefore(insertedNode, targetChildren
								.item(position));
			}
		}
	}

	/**
	 * Removes all textNodes of an ElementNode and inserts a new textNode. This
	 * method is used to realize several advices. If the textNode text is null,
	 * the element's textNodes are removed - no exception is thrown
	 * 
	 * @param element
	 *            - the {@link Element} to be worked on
	 * @param text
	 *            - the new {@link Text}
	 * @return The changed {@link Element}
	 */
	@SuppressWarnings("unused")
	public static Element changeTextContent(final Element element,
			final Text text) {
		logger.debug("Changing text content of element node with name "
				+ element.getNodeName()+".");
		final NodeList children = element.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			if (children.item(j).getNodeType() == Node.TEXT_NODE) {
				element.removeChild(children.item(j));
			}
		}
		if (text != null) {
			if (children != null) {
				element.insertBefore(text, element.getFirstChild());
			} else {
				element.appendChild(text);
			}
		}
		return element;
	}

	/**
	 * Deletes a {@link Node}.
	 * 
	 * @param node
	 * @return The parent of the deleted {@link Node}.
	 */
	public static Node deleteNode(final Node node) {
		logger.debug("Deleting " + getNodeTypeAsPhrase(node.getNodeType()) + " node with name " + node.getNodeName()+".");
		Node parent;
		if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
			parent = ((Attr) node).getOwnerElement();
			((Element) parent).removeAttribute(node.getNodeName());
		} else {
			parent = node.getParentNode();
			parent.getChildNodes();
			parent.removeChild(node);
		}
		return parent;
	}
}
