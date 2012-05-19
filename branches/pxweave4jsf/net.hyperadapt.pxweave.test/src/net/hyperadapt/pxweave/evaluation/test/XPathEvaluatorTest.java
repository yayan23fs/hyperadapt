/**
 * 
 */
package net.hyperadapt.pxweave.evaluation.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.Environment.NSContext;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author martin
 * 
 */
public class XPathEvaluatorTest {
	private Document document;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IDOMParser parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XPathEvaluator#evaluateXPath(java.lang.String, org.w3c.dom.Document)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testEvaluateXPath() throws XMLWeaverException {
		NSContext nsContext = new NSContext(){{this.getNamespaces().put("books", URI.create("http://books"));}};
		XPathEvaluator xPathEvaluator = new XPathEvaluator(nsContext);
		NodeList result = xPathEvaluator.evaluateXPath("//books:book", document);
		assertEquals("Result is a NodeList with 3 Nodes", 3, result.getLength());
		assertTrue("The nodes are elementNodes named \"book\"", (result.item(0)
				.getNodeType() == Node.ELEMENT_NODE)
				& (result.item(0).getNodeName().contentEquals("book")));
		result = xPathEvaluator.evaluateXPath("//books:book/books:title/text()", document);
		assertEquals("Result is a NodeList with 3 Nodes", 3, result.getLength());
		assertTrue("The nodes are textNodes",
				result.item(0).getNodeType() == Node.TEXT_NODE);
		result = xPathEvaluator.evaluateXPath("//books:book/attribute::inStock",
				document);
		assertEquals("Result is a NodeList with 3 Nodes", 3, result.getLength());
		assertTrue("The nodes are attributeNodes",
				result.item(0).getNodeType() == Node.ATTRIBUTE_NODE);

		result = xPathEvaluator.evaluateXPath("/someElement", document);
		assertEquals("Result is an empty NodeList", 0, result.getLength());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XPathEvaluator#evaluateXPath(java.lang.String, org.w3c.dom.Document)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testEvaluateXPathPathError() throws XMLWeaverException {
		NSContext nsContext = new NSContext();
		XPathEvaluator xPathEvaluator = new XPathEvaluator(nsContext);
		xPathEvaluator.evaluateXPath("bogusData///", document);
	}

}
