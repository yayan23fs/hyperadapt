/**
 * 
 */
package net.hyperadapt.pxweave.evaluation.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.Environment.NSContext;
import net.hyperadapt.pxweave.evaluation.IXQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.evaluation.XQArgument;
import net.hyperadapt.pxweave.evaluation.XQLocalArgument;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author msteinfeldt
 * 
 */
public class IXQAbstractArgumentTest {
	private IXQAbstractArgument argument;
	private IXQAbstractArgument decoratingArgument;
	private IXQAbstractArgument decoratingDecoratorArgument;
	private XPathEvaluator xPathEvaluator;
	private Document document;
 
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IDOMParser parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
		NSContext nsContext = new NSContext(){{this.getNamespaces().put("books", URI.create("http://books"));}};
		argument = new XQArgument(document,nsContext,nsContext);
		decoratingArgument = new XQLocalArgument(argument,nsContext,nsContext);
		decoratingDecoratorArgument = new XQLocalArgument(decoratingArgument,nsContext,nsContext);
		xPathEvaluator = new XPathEvaluator(nsContext);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getDynamicQueryContext()}
	 * .
	 */
	@Test
	public void testGetDynamicQueryContext() {
		argument.addVariableAndBindValue("testParameter", new Integer(1234));
		final DynamicQueryContext dqc1 = argument.getDynamicQueryContext();

		assertEquals("Parameter was bound to dqc", 1234, dqc1
				.getParameter("testParameter"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getDocument()}.
	 */
	@Test
	public void testGetDocument() {
		final Document doc1 = argument.getDocument();
		final Document doc2 = decoratingArgument.getDocument();
		final Document doc3 = decoratingDecoratorArgument.getDocument();
		assertTrue("All documents must be the same",
				((doc1 == doc2) & doc1 == doc3));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#addVariableAndBindEvValue(java.lang.String, java.lang.String)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testAddVariableAndBindEvValue() throws XMLWeaverException {
		argument.addVariableAndBindEvValue("book", "//books:book[1]");
		final Node node = (Node) argument.getDynamicQueryContext()
				.getParameter("book");
		assertEquals("The node must be bound correctly", xPathEvaluator
				.evaluateXPath("//books:book[1]", document).item(0), node);
		final String declaration = argument.getExternalVarDecl();
		assertTrue("", declaration
				.contentEquals("declare variable $book external;"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#addVariableAndBindEvValue(java.lang.String, java.lang.String)}
	 * . Throws illegalArgumentException since the result of the evaluation is
	 * not a single value
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testAddVariableAndBindEvValueMultiple()
			throws XMLWeaverException {
		argument.addVariableAndBindEvValue("books", "//books:book");

	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#getXQExpression()}.
	 * Throws illegalArgumentException since the result of the evaluation is not
	 * a single value
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testGetXQExpressionDuplicateParameterName()
			throws XMLWeaverException {
		argument.addVariableAndBindEvValue("books", "//books:book[1]");
		argument.addVariableAndBindEvValue("books", "//books:book[1]");
		argument.declarePointcut("//books:title");
		argument.setExpression("1+1");
		argument.getXQExpression();
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#addVariableAndBindValue(java.lang.String, java.lang.Object)}
	 * .
	 */
	@Test
	public void testAddVariableAndBindValue() {
		argument.addVariableAndBindValue("testParameter1", new Integer(1234));
		decoratingArgument.addVariableAndBindValue("testParameter2",
				new Integer(1234));
		decoratingDecoratorArgument.addVariableAndBindValue("testParameter3",
				new Integer(1234));

		final DynamicQueryContext dqc1 = argument.getDynamicQueryContext();
		final DynamicQueryContext dqc2 = decoratingArgument
				.getDynamicQueryContext();
		final DynamicQueryContext dqc3 = decoratingDecoratorArgument
				.getDynamicQueryContext();
		final String declaration1 = argument.getExternalVarDecl();
		final String declaration2 = decoratingArgument.getExternalVarDecl();
		final String declaration3 = decoratingDecoratorArgument
				.getExternalVarDecl();

		assertEquals("Parameter was bound to dqc", 1234, dqc1
				.getParameter("testParameter1"));
		assertEquals("Parameter was bound to dqc", 1234, dqc2
				.getParameter("testParameter2"));
		assertEquals("Parameter was bound to dqc", 1234, dqc3
				.getParameter("testParameter3"));
		assertNull("Parameter not known in decorated IXQArgument", dqc1
				.getParameter("testParameter2"));
		assertNull("Parameter not known in decorated IXQArgument", dqc2
				.getParameter("testParameter3"));
		assertTrue("External variable declaration is correct", declaration1
				.contentEquals("declare variable $testParameter1 external;"));
		assertTrue(
				"External variable declaration is correct",
				declaration2
						.contentEquals("declare variable $testParameter1 external;declare variable $testParameter2 external;"));
		assertTrue(
				"External variable declaration is correct",
				declaration3
						.contentEquals("declare variable $testParameter1 external;declare variable $testParameter2 external;declare variable $testParameter3 external;"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#declarePointcut(java.lang.String)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testDeclarePointcut() throws XMLWeaverException {
		//TODO check this xpath
		argument.declarePointcut("//book");
		argument.setExpression("1+1");
		final XQueryExpression xqe = argument.getXQExpression();
		assertTrue(
				"PointCut was declared as for-expression",
				xqe
						.getExpression()
						.toString()
						.contentEquals(
								"ForExpression(((/)/descendant::element(book, xs:anyType)), 2)"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.evaluation.IXQAbstractArgument#setExpression(java.lang.String)}
	 * .
	 */
	@Test
	public void testSetExpression() {
		argument.setExpression("1+1");
		final String expression = argument.getExpression();
		assertTrue("Expression was set", expression.contentEquals("1+1"));
	}
}
