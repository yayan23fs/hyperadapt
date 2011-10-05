/**
 * 
 */
package net.hyperadapt.pxweave.interpreter.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.Dependency;
import net.hyperadapt.pxweave.aspects.ast.ObjectFactory;
import net.hyperadapt.pxweave.evaluation.XQArgument;
import net.hyperadapt.pxweave.interpreter.AspectInterpreter;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.interpreter.InterpreterArgument;
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
public class AspectInterpreterTest {

	private Document document;
	private IEnvironment environment;
	private IEnvironment contradictingEnvironment;
	private IDOMParser parser;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
	
		this.environment = Environment.create((new File("./testData/aspectInterpreterTest")).toURI(),URI.create("weaverConfig.xml"),URI.create("context.xml"),(new File("./patternConflicts.xml")).toURI());
		this.contradictingEnvironment = Environment.create((new File("./testData/aspectInterpreterTest")).toURI(),URI.create("ContradictingWeaverConfig.xml"),URI.create("context.xml"),(new File("./patternConflicts.xml")).toURI());
		document = parser.buildDOM((new File("./testData/book.xml")).toURI());
	}
	

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#loadAndValidateArguments(java.util.HashMap)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@SuppressWarnings("serial")
	@Test
	public void testLoadArguments() throws XMLWeaverException {
		final IInterpreterArgument dArgument = new InterpreterArgument(
				document, "testDoc");
		dArgument.setSchemaURI((new File("testData/book.xsd")).toURI());
		
		final IInterpreterArgument fArgument = new InterpreterArgument(
				new File("testData/book.xml"), new File("testData/result.xml"),
				"testDocument");
		fArgument.setSchemaURI((new File("testData/book.xsd")).toURI());

		final AspectInterpreter aspectInterpreter = new AspectInterpreter(environment);
	
		HashMap<String,IInterpreterArgument> arguments = new HashMap<String, IInterpreterArgument>() {
			{
				put(fArgument.getId(), fArgument);
				put(fArgument.getId().concat("2"), fArgument);
			}
		};
		aspectInterpreter.loadAndValidateArguments(arguments);
		
		assertNotNull("Document of InterpreterFileArgument was parsed",fArgument.getDocument());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#initialiseArguments(ArrayList, ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInitialiseArguments() throws XMLWeaverException {
		final ArrayList<IInterpreterArgument> interpreterArguments = new ArrayList<IInterpreterArgument>();
		final IInterpreterArgument ia = new InterpreterArgument(new File(
				"testData/book.xml"), new File("testData/result.xml"), "core");
		interpreterArguments.add(ia);
	System.out.println(environment.getNamespaceContext().getDefinitionURI(URI.create("http://books")));
		final AspectInterpreter aspectInterpreter = new AspectInterpreter(environment);
		final HashMap<String, IInterpreterArgument> result = aspectInterpreter
				.initialiseArguments(interpreterArguments, environment.getAspects());
		assertTrue("CoreType path was set", result.get("core").getSchemaURI()
				.equals((new File("testData/book.xsd")).toURI()));
		assertTrue(
				"Aspects use the same core document, only one core argument should be added",
				result.size() == 1);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#initialiseArguments(ArrayList, ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInitialiseArgumentsDifferentCores()
			throws XMLWeaverException {
		final AspectInterpreter aspectInterpreter = new AspectInterpreter(contradictingEnvironment);

		final ArrayList<IInterpreterArgument> interpreterArguments = new ArrayList<IInterpreterArgument>();
		final IInterpreterArgument ia = new InterpreterArgument(new File(
				"testData/book.xml"), new File("testData/result.xml"), "core");
		final IInterpreterArgument ia2 = new InterpreterArgument(new File(
				"testData/book.xml"), new File("testData/result.xml"),
				"othercore");
		interpreterArguments.add(ia);
		interpreterArguments.add(ia2);
		final HashMap<String, IInterpreterArgument> result = aspectInterpreter
				.initialiseArguments(interpreterArguments, contradictingEnvironment.getAspects());
		assertTrue("CoreType path was set", result.get("core").getSchemaURI()
				.equals(new File(("testData/book.xsd")).toURI()));
		assertTrue("CoreType path was set", result.get("othercore")
				.getSchemaURI().equals((new File("testData/book.xsd")).toURI()));
		assertTrue("Aspects use different core document, both must be added",
				result.size() == 2);
	}

	
	

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#initialiseArguments(ArrayList, ArrayList)}
	 * . This test method must throw a {@link XMLWeaverException} because the
	 * core "core" declared in aspect is provided by more than one
	 * {@link InterpreterArgument}.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInitialiseArgumentsDuplicateCoresProvided()
			throws XMLWeaverException {
		AspectInterpreter aspectInterpreter = new AspectInterpreter(environment);
		
		IInterpreterArgument ia = new InterpreterArgument(new File(
				"testData/book.xml"), new File("testData/result.xml"), "core");
		IInterpreterArgument ia2 = new InterpreterArgument(new File(
				"testData/book.xml"), new File("testData/result.xml"), "core");
		ArrayList<IInterpreterArgument> interpreterArguments = new ArrayList<IInterpreterArgument>();
		interpreterArguments.add(ia);
		interpreterArguments.add(ia2);
		aspectInterpreter.initialiseArguments(interpreterArguments,environment.getAspects());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#evaluateDependency(net.hyperadapt.pxweave.aspects.ast.AdviceGroup, net.hyperadapt.pxweave.evaluation.IXQAbstractArgument)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testEvaluateDependency() throws XMLWeaverException {
		final ObjectFactory of = new ObjectFactory();
		final XQArgument xqA = new XQArgument(parser
				.buildDOM((new File("./testData/book.xml")).toURI()),environment.getNamespaceContext(),environment.getNamespaceContext());
		xqA.addVariableAndBindValue("test", new Integer(1));
		final Dependency dependencies = of.createDependency();
		dependencies.setBoolExpr("1+1=2");
		boolean result = AspectInterpreter
				.evaluateDependency(dependencies, xqA);
		assertTrue("the boolExpr must evaluate to TRUE", result);
		dependencies.setBoolExpr("$test+1=2");
		result = AspectInterpreter.evaluateDependency(dependencies, xqA);
		assertTrue("the boolExpr must evaluate to TRUE", result);
		dependencies.setBoolExpr("1+1=3");
		result = AspectInterpreter.evaluateDependency(dependencies, xqA);
		assertFalse("the boolExpr must evaluate to TRUE", result);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#outputArguments(java.util.HashMap)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@SuppressWarnings("serial")
	@Test
	public void testOutputArguments() throws XMLWeaverException {
		final AspectInterpreter aspectInterpreterPL = new AspectInterpreter(environment);
		final InterpreterArgument dArgument = new InterpreterArgument(
				document, "testDoc");
		dArgument.setSchemaURI((new File("testData/book.xsd")).toURI());
		final HashMap<String, IInterpreterArgument> arguments = new HashMap<String, IInterpreterArgument>() {
			{
				put(dArgument.getId(), dArgument);
				put(dArgument.getId().concat("2"), dArgument);
			}
		};
		final IInterpreterArgument result = aspectInterpreterPL
				.outputArguments(arguments).get(0);
		assertEquals("A validated, unchanged result was returned", dArgument,
				result);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.AspectInterpreter#outputArguments(java.util.HashMap)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@SuppressWarnings("serial")
	@Test(expected = XMLWeaverException.class)
	public void testOutputArgumentsInvalidDoc() throws XMLWeaverException {
		final Element ele = (Element) document.getElementsByTagName("book")
				.item(0);
		ele.removeChild(document.getElementsByTagName("author").item(0));

		final AspectInterpreter aspectInterpreterPL = new AspectInterpreter(environment);
		final InterpreterArgument dArgument = new InterpreterArgument(
				document, "testDoc");
		dArgument.setSchemaURI(URI.create("testData/book.xsd"));
		final HashMap<String, IInterpreterArgument> arguments = new HashMap<String, IInterpreterArgument>() {
			{
				put(dArgument.getId(), dArgument);
				put(dArgument.getId().concat("2"), dArgument);
			}
		};
		aspectInterpreterPL.outputArguments(arguments).get(0);
	}

}
