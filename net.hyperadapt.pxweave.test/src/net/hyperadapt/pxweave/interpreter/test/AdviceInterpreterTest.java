/**
 * 
 */
package net.hyperadapt.pxweave.interpreter.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.Environment.NSContext;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
import net.hyperadapt.pxweave.aspects.ast.ObjectFactory;
import net.hyperadapt.pxweave.aspects.ast.Scope;
import net.hyperadapt.pxweave.aspects.ast.XPath;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.interpreter.AdviceContext;
import net.hyperadapt.pxweave.interpreter.AdviceInterpreter;
import net.hyperadapt.pxweave.interpreter.OperationInterpreter;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author martin
 * 
 */
public class AdviceInterpreterTest {
	private AdviceInterpreter adviceInterpreter;
	private Document document;
	private final List<BasicAdvice> advices = new ArrayList<BasicAdvice>();
	private final ObjectFactory factory = new ObjectFactory();
	private final Scope scope = new Scope();
	private IEnvironment environment;
	private NSContext nsContext;
	private XPathEvaluator xPathEvaluator;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IDOMParser parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());

		nsContext = new Environment.NSContext(){{this.getNamespaces().put("books",URI.create("http://books"));}};
		xPathEvaluator = new XPathEvaluator(nsContext);
		environment = Environment.create(new LinkedList<Aspect>(), null,nsContext,null);
		final AdviceContext adviceContext = new AdviceContext(scope,
				"testAspect");
		scope.getXpath().add(new XPath() {
			{
				value = "/books:bookList";
			}
		});
		adviceInterpreter = new AdviceInterpreter(adviceContext, document,
				null, new OperationInterpreter(),environment);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AdviceInterpreter#interpreteAdvices(java.util.List)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteAdvices() throws XMLWeaverException {
		scope.getXpath().remove(0);
		scope.getXpath().add(new XPath() {
			{
				value = "/books:bookList/books:book[1]";
			}
		});
		final BasicAdvice delete = factory.createDelete();
		delete.setPointcut(new XPath());
		delete.getPointcut().setValue("books:author");
		advices.add(delete);
		adviceInterpreter.interpreteAdvices(advices);
		NodeList result = xPathEvaluator.evaluateXPath("//books:author", document);
		assertEquals("Advice effected only Nodes in scope", 2, result
				.getLength());
		scope.getXpath().remove(0);
		scope.getXpath().add(new XPath() {
			{
				value = "/books:bookList/books:book[2]";
			}
		});
		scope.getXpath().add(new XPath() {
			{
				value = "/books:bookList/books:book[3]";
			}
		});
		adviceInterpreter.interpreteAdvices(advices);
		result = xPathEvaluator.evaluateXPath("//books:author", document);
		assertEquals("Advice effected all Nodes in the different scopes", 0,
				result.getLength());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AdviceInterpreter#checkTemplateSize(java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testCheckTemplateSize1() throws XMLWeaverException {
		final ArrayList<Element> templates = null;
		AdviceInterpreter.checkTemplateSize(templates);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AdviceInterpreter#checkTemplateSize(java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testCheckTemplateSize2() throws XMLWeaverException {
		final ArrayList<Element> templates = new ArrayList<Element>();
		AdviceInterpreter.checkTemplateSize(templates);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AdviceInterpreter#checkTemplateSize(java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testCheckTemplateSize3() throws XMLWeaverException {
		final ArrayList<Element> templates = new ArrayList<Element>();
		final Element element = null;
		templates.add(element);
		AdviceInterpreter.checkTemplateSize(templates);
	}

}
