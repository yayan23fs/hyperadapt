/**
 * 
 */
package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.List;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.Main;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.AspectWeaver;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author msteindeldt,skarol
 * 
 */
public class WeavingOperationsTest {
	
	
	private final String in = "in:testData/book.xml";
	private final String in_music = "in:testData/music_db.xml";
	private final String out = "out:testData/result.xml";
	private final String core = "coreID:core";
	private final String[] args = { in, out, core };
	private final String[] args_music = { in_music, out, core };
	
	private URI baseURI;
	private URI contextURI;
	private URI matrixURI;
	
	private IDOMParser parser; 


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = new DOML3Parser(false);
		File base = new File("./testData/weavingOperationsTest");
		baseURI = base.toURI();
		File matrix = new File("./patternConflicts.xml");
		matrixURI = matrix.toURI();
		contextURI = URI.create("context.xml");
	}

	
	@Test
	public void testChangeOrder() throws XMLWeaverException {
		URI configURI = URI.create("ConfigChangeOrder.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI,matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
		
		AspectWeaver.weave(environment,arguments);

		NodeList result = xPathEvaluator.evaluateXPath("//books:book[3]/books:author",
				parser.buildDOM(URI.create("testData/result.xml"))).item(0)
				.getChildNodes();
		assertTrue("Changed order of joinPoint's children", result.item(0)
				.getNodeName().contentEquals("name")
				& result.item(1).getNodeName().contentEquals("lastName"));
	}
	
	@Test
	public void testDelete() throws XMLWeaverException {	
		URI configURI = URI.create("ConfigDelete.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
			
		AspectWeaver.weave(environment,arguments);
		NodeList result = xPathEvaluator.evaluateXPath("//books:book",
				parser.buildDOM(URI.create("testData/result.xml")));
		assertEquals("Nodes were removed", 1, result.getLength());
	}
	
	@Test
	public void testChangeValue() throws XMLWeaverException {
		URI configURI = URI.create("ConfigChangeValue.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
		
		final NodeList oldPrices = xPathEvaluator.evaluateXPath(
				"//books:book/attribute::price", parser
						.buildDOM(URI.create("testData/book.xml")));
		
		AspectWeaver.weave(environment,arguments);
		
		NodeList result = xPathEvaluator.evaluateXPath("//books:book/attribute::price",
				parser.buildDOM(URI.create("testData/result.xml")));
		for (int i = 0; i < oldPrices.getLength(); i++) {
			assertTrue("Attribute price was doubled", Float.parseFloat(result
					.item(i).getNodeValue()) == Float.parseFloat(oldPrices
					.item(i).getNodeValue()) * 2);
		}
	}
	
	
	@Test
	public void testChooseVariant() throws XMLWeaverException {
		URI configURI = URI.create("ConfigChooseVariant.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
		
		AspectWeaver.weave(environment,arguments);
		
		NodeList result = xPathEvaluator.evaluateXPath("//books:book/books:author/books:name",
				parser.buildDOM(URI.create("testData/result.xml")));
		assertEquals("Only one item left", 1, result.getLength());
		assertTrue("The correct item is left", result.item(0).getLastChild()
				.getNodeValue().contentEquals("Sam"));

	}
	
	
	@Test
	public void testChooseVariant2() throws XMLWeaverException {
		URI configURI = URI.create("ConfigChooseVariantMusic.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args_music); 	
		
		AspectWeaver.weave(environment,arguments);
		
		NodeList result = xPathEvaluator.evaluateXPath("//music:image/attribute::size",
				parser.buildDOM(URI.create("testData/result.xml")));
		assertTrue("Value should be SMALL.", result
				.item(0).getNodeValue().equals("SMALL"));
	

	}
	
	@Test
	public void testCollapse() throws XMLWeaverException {
		URI configURI = URI.create("ConfigCollapse.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
		NodeList result =xPathEvaluator.evaluateXPath("//books:book[3]/*", parser
				.buildDOM(URI.create("testData/result.xml")));
		Node resultNode = xPathEvaluator.evaluateXPath("//books:book[3]",
				parser.buildDOM(URI.create("testData/result.xml"))).item(0);
		assertEquals("Elements were removed", 0, result.getLength());
		System.out.println(resultNode.getTextContent());
		assertTrue("TextNode was added", resultNode.getTextContent()
				.contentEquals("Some Title"));
	}
	
	
	@Test
	public void testEnrichContent() throws XMLWeaverException{
		URI configURI = URI.create("ConfigEnrichContent.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
		
		Node resultNode = xPathEvaluator.evaluateXPath("//books:book[3]/books:author",
				parser.buildDOM(URI.create("testData/result.xml"))).item(0);
		System.out.println(resultNode.getLastChild().getNodeValue());
		assertTrue("textNode was added", resultNode.getLastChild()
				.getNodeValue().contentEquals("testText"));
	}
	
	
	@Test
	public void testReduceContent() throws XMLWeaverException{
		URI configURI = URI.create("ConfigReduceContent.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
		
		NodeList result = xPathEvaluator.evaluateXPath("//book/author/text()",
				parser.buildDOM(URI.create("testData/result.xml")));
		assertTrue("Text content must be removed", result.getLength() == 0);
	}

	@Test
	public void testMove() throws XMLWeaverException{
		URI configURI = URI.create("ConfigMove.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
		Document doc = parser.buildDOM(URI.create("testData/result.xml"));
		NodeList result = xPathEvaluator.evaluateXPath("//books:book[1]/books:title", doc);
		assertEquals("Element was inserted", 2, result.getLength());
		result = xPathEvaluator.evaluateXPath("//books:book[2]/books:title", doc);
		assertEquals("Element was removed", 0, result.getLength());	
	}		
	
	@Test
	public void testFillComponentByID() throws XMLWeaverException{
		URI configURI = URI.create("ConfigFillComponentByID.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		final String[] args = { "in:testData/bookList.xml", out, core };
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
		//BE AWARE: we use no namespaces for the input file but for the core document!
		//As a result, book and booklist nodes do not belong to a namespace while title does
		NodeList result = xPathEvaluator.evaluateXPath("//book/books:title", parser
				.buildDOM(URI.create("testData/result.xml")));
		assertEquals("Elements were filled", 3, result.getLength());
		assertTrue("Elements were filled", result.item(0).getTextContent()
				.contentEquals("Principles: How they got lost.")
				& result.item(1).getTextContent().contentEquals(
						"The Moon Story"));
	}
	
	@Test
	public void testExpandElement() throws XMLWeaverException{
		URI configURI = URI.create("ConfigExpandElement.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI, matrixURI);	
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
	
		AspectWeaver.weave(environment,arguments);
	
		Document doc = parser.buildDOM(URI.create("testData/result.xml"));
		NodeList result = xPathEvaluator.evaluateXPath("//books:book/books:title/text()", doc);
		assertTrue("Text content must be removed", result.getLength() == 0);
		result = xPathEvaluator.evaluateXPath("//books:book/books:title/*", doc);
		assertTrue("Elements were created", result.getLength() == 3);
	}
	

}
