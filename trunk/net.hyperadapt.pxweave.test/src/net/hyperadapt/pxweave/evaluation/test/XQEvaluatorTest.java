/**
 * 
 */
package net.hyperadapt.pxweave.evaluation.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.Environment.NSContext;
import net.hyperadapt.pxweave.evaluation.IXQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XQArgument;
import net.hyperadapt.pxweave.evaluation.XQEvaluator;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author martin
 * 
 */
public class XQEvaluatorTest {
	private Document document;
	private IXQAbstractArgument argument;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IDOMParser parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
		NSContext nsContext = new NSContext(){{this.getNamespaces().put("books",URI.create("http://books"));}};
		argument = new XQArgument(document,nsContext,nsContext);

	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XQEvaluator#evaluateToElementNodes(net.hyperadapt.pxweave.evaluation.IXQAbstractArgument)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testEvaluateToElementNodes() throws IllegalArgumentException,
			XMLWeaverException {
		argument.setExpression("<book/>");
		final Element element = XQEvaluator.evaluateToElementNodes(argument)
				.get(0);
		assertTrue("Result contains the correct element", element.getNodeName()
				.contentEquals("book"));

		argument.declarePointcut("//books:book");
		argument.setExpression("<br/>");
		final ArrayList<Element> elements = XQEvaluator
				.evaluateToElementNodes(argument);
		assertEquals("One result for each joinPoint", elements.size(), 3);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XQEvaluator#evaluateToElementNodes(net.hyperadapt.pxweave.evaluation.IXQAbstractArgument)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testEvaluateToElementNodesNotElements()
			throws IllegalArgumentException, XMLWeaverException {
		argument.setExpression("1+1");
		@SuppressWarnings("unused")
		final Element element = XQEvaluator.evaluateToElementNodes(argument)
				.get(0);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XQEvaluator#evaluateToNodes(net.hyperadapt.pxweave.evaluation.IXQAbstractArgument)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testEvaluateToNodes() throws IllegalArgumentException,
			XMLWeaverException {
		argument.declarePointcut("//books:book");
		argument.setExpression("<br/>");
		final ArrayList<Element> elements = XQEvaluator
				.evaluateToElementNodes(argument);
		assertEquals("One result for each joinPoint", elements.size(), 3);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.XQEvaluator#evaluateToTextNodes(net.hyperadapt.pxweave.evaluation.IXQAbstractArgument)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testEvaluateToTextNodes() throws IllegalArgumentException,
			XMLWeaverException {
		argument.setExpression("1+1");
		final int result = Integer.parseInt(XQEvaluator.evaluateToTextNodes(
				argument).get(0).getNodeValue());
		assertEquals(
				"Evaluation results in textNode, which contains the correct result",
				2, result);
	}

}
