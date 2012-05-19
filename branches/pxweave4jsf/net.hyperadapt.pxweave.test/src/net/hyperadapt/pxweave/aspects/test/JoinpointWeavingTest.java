package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.List;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.ExecutionState;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.Main;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.XMLWriter;
import net.hyperadapt.pxweave.aspects.AspectWeaver;
import net.hyperadapt.pxweave.aspects.IProgrammaticJoinpoint;
import net.hyperadapt.pxweave.aspects.PipeStageJoinpoint;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.NodeList;

public class JoinpointWeavingTest {
	private final String in = "in:testData/book.xml";
	private final String outPath = "./testData/joinpointWeavingTest/result.xml";
	private final String out = "out:" + outPath;
	private final String core = "coreID:core";
	private final String[] args = { in, out, core };
	
	private URI baseURI;
	private URI contextURI;
	private URI matrixURI;
	
	private IDOMParser parser; 


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = new DOML3Parser();
		File base = new File("./testData/joinpointWeavingTest");
		baseURI = base.toURI();
		contextURI = URI.create("context.xml");
		matrixURI = URI.create("../../patternConflicts.xml");
	}

	
	@Test
	public void testInsertSomeBooks() throws XMLWeaverException {
		URI configURI = URI.create("ConfigAddSomeBooks.xml");	
		IEnvironment environment = Environment.create(baseURI, configURI, contextURI,matrixURI);	
		
		@SuppressWarnings("unused")
		AspectWeaver weaver = new AspectWeaver(environment);
		
		final String stageName = "SomeStage";
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 	
		assertTrue("Argument list should only contain one core document.",arguments.size()==1);
		IInterpreterArgument outArgument = arguments.get(0);
	
		
		IProgrammaticJoinpoint jp = new PipeStageJoinpoint(stageName);
		IWeavingContext context = environment.getExecutionState().getContextModel();
		
		IExecutionState state = new ExecutionState(jp,context,arguments);
		
		XPathEvaluator xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
		
		System.out.println("Testing before ...");
		environment.beforeExecutionState(state);

		System.out.println("Testing after ...");
		environment.updateExecutionState(state);	
		
		NodeList result1 = xPathEvaluator.evaluateXPath("//books:book[1]/books:ISBN",
				outArgument.getDocument());
		assertTrue("Book not inserted or inserted in the wrong place. ISBN is "+result1.item(0).getTextContent()+".", 
				"0340992565".equals(result1.item(0).getTextContent()));
		
		NodeList result2 = xPathEvaluator.evaluateXPath("//books:book[2]/books:ISBN",
				outArgument.getDocument());
	
		assertTrue("Book not inserted or inserted in the wrong place. ISBN is "+result2.item(0).getTextContent()+".", 
				"1444712543".equals(result2.item(0).getTextContent()));
	
		XMLWriter.getInstance().write(outArgument, false);
	}
	
}
