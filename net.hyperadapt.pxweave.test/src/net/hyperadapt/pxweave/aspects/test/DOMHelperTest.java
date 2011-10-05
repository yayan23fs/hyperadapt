/**
 * 
 */
package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import javax.xml.namespace.NamespaceContext;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;

import net.hyperadapt.pxweave.util.DOMOperations;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * @author martin
 * 
 */
public class DOMHelperTest {
	private Document document;
	private Node node;
	private Node attrNode;
	private Node textNode;
	private NamespaceContext nsContext;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IDOMParser parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
		node = document.createElement("test");
		attrNode = document.createAttribute("test");
		textNode = document.createTextNode("test");
		 nsContext = new Environment.NSContext(){{this.getNamespaces().put("books",URI.create("http://books"));}};
	}

	

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#checkNodeType(org.w3c.dom.Node, short[])}
	 * .
	 */
	@Test
	public void testCheckNodeTypeNodeShortArray() {
		final Node node = document.createElement("test");
		final Node attrNode = document.createAttribute("test");
		final Node textNode = document.createTextNode("test");
		boolean typeOK = true;
		try {
			DOMOperations.checkNodeType(node, Node.ELEMENT_NODE);
			DOMOperations.checkNodeType(attrNode, Node.ATTRIBUTE_NODE);
			DOMOperations.checkNodeType(textNode, Node.TEXT_NODE);
			DOMOperations.checkNodeType(textNode, Node.ELEMENT_NODE,
					Node.ATTRIBUTE_NODE, Node.TEXT_NODE);
		} catch (final XMLWeaverException e) {
			typeOK = false;
		}
		assertTrue("All types are correct" + Node.ELEMENT_NODE, typeOK);
	}

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#checkNodeType(org.w3c.dom.Node, short[])}
	 * .
	 */

	public void testCheckNodeTypeNodeShortWithWrTypes() {

		int counter = 0;
		try {
			DOMOperations.checkNodeType(node, Node.ATTRIBUTE_NODE);
		} catch (final XMLWeaverException e) {
			counter += 1;
		}
		try {
			DOMOperations
					.checkNodeType(attrNode, Node.ELEMENT_NODE, Node.TEXT_NODE);
		} catch (final XMLWeaverException e) {
			counter += 1;
		}
		try {
			DOMOperations.checkNodeType(document.createComment("test"),
					Node.ELEMENT_NODE, Node.ATTRIBUTE_NODE, Node.TEXT_NODE);
		} catch (final XMLWeaverException e) {
			counter += 1;
		}
		assertTrue("All types are incorrect" + Node.ELEMENT_NODE, counter == 3);
	}

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#insertNode(org.w3c.dom.Node, org.w3c.dom.Node, int)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInsertNode() throws XMLWeaverException {

		final Node n = document.getElementsByTagName("book").item(0);
		DOMOperations.insertNode(n, node, -10);
		final int index = n.getChildNodes().getLength() - 1;
		assertTrue("testElement should be appended to the list of childNodes",
				n.getChildNodes().item(index).getNodeName().contentEquals(
						"test"));
		DOMOperations.insertNode(n, node, 1);
		assertTrue("testElement should inserted at position==1", n
				.getChildNodes().item(1).getNodeName().contentEquals("test"));
		DOMOperations.insertNode(n, textNode, 2);
		attrNode.setNodeValue("test");
		assertTrue("textNode should inserted at position==2", n.getChildNodes()
				.item(2).getNodeValue().contentEquals("test"));
	}

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#insertNode(org.w3c.dom.Node, org.w3c.dom.Node, int)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInsertNodeExceedingPos() throws XMLWeaverException {

		final Node n = document.getElementsByTagName("book").item(0);
		DOMOperations.insertNode(n, node, 12);
	}

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#changeTextContent(org.w3c.dom.Element, org.w3c.dom.Text)}
	 * .
	 */
	@Test
	public void testChangeTextContent() {
		final Node node = document.getElementsByTagName("title").item(0);
		Text text = document.createTextNode("testData");
		DOMOperations.changeTextContent((Element) node, text);
		assertTrue(
				"The Node should have one textNode with content \"testData\"",
				node.getChildNodes().item(0).getNodeValue() == "testData");
		assertTrue(
				"The Node should have ONE textNode with content \"testData\"",
				node.getChildNodes().getLength() == 1);
		DOMOperations.changeTextContent((Element) node, text);
		assertTrue(
				"The Node should have one textNode with content \"testData\"",
				node.getChildNodes().item(0).getNodeValue() == "testData");
		assertTrue(
				"The Node should have ONE textNode with content \"testData\"",
				node.getChildNodes().getLength() == 1);
		text = null;
		DOMOperations.changeTextContent((Element) node, text);
		assertTrue(
				"The Node should have one textNode with content \"testData\"",
				node.getChildNodes().getLength() == 0);

	}

	/**
	 * Test method for
	 * {@link net.DOMOperations.hyperadapt.pxweave.util.DOMHelper#deleteNode(org.w3c.dom.Node)}.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testDeleteNode() throws XMLWeaverException {
		XPathEvaluator xPathEvaluator = new XPathEvaluator(nsContext);
		Node node =xPathEvaluator.evaluateXPath("//books:book[3]", document).item(0);
		DOMOperations.deleteNode(node);
		assertNull("only two elements must be left", xPathEvaluator
				.evaluateXPath("//books:book[3]", document).item(0));

		node = xPathEvaluator.evaluateXPath("//books:book[1]/attribute::inStock",
				document).item(0);
		DOMOperations.deleteNode(node);
		assertNull("only two attributes must be left", xPathEvaluator
				.evaluateXPath("//books:book/attribute::inStock", document).item(3));

		node = xPathEvaluator.evaluateXPath("//books:book[1]/books:title/text()", document)
				.item(0);
		DOMOperations.deleteNode(node);
		assertNull("textNode was removed", xPathEvaluator.evaluateXPath(
				"//books:book[1]/books:title/text()", document).item(0));
	}

}
