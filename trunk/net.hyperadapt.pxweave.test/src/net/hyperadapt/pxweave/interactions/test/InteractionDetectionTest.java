package net.hyperadapt.pxweave.interactions.test;

import java.io.File;
import java.net.URI;
import java.util.List;


import junit.framework.Assert;

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
import net.hyperadapt.pxweave.aspects.ast.Advice;
import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
import net.hyperadapt.pxweave.interactions.runtime.DynamicAnalysis;
import net.hyperadapt.pxweave.interactions.runtime.IDynamicAnalysis;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.validation.DOML3Parser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class InteractionDetectionTest {
	
	private URI baseURI;
	private URI matrixURI;
	private URI contextURI;
	private URI configURI;
	
	private final String in = "in:testData/interactionDetectionTest/foo.xml";
	private final String out = "out:testData/interactionDetectionTest/result.xml";
	private final String core = "coreID:core";
	private final String[] args = { in, out, core };
	
	@Before
	public void setUp() throws Exception {
		File base = new File("./testData/interactionDetectionTest");
		baseURI = base.toURI();
		matrixURI = URI.create("patternConflicts.xml");
		contextURI = URI.create("context.xml");
		configURI = URI.create("weaverConfig.xml");
	}

	@Test
	public void testConflictDetection() throws XMLWeaverException{
		IEnvironment env = Environment.create(baseURI, configURI, contextURI, matrixURI);
		IDynamicAnalysis analyzer = env.getConflictAnalyser();
		
		Assert.assertTrue("Conflict Matrix ist empty.",!(env.getConflictMatrix() == null));
		System.out.println(env.getConflictMatrix().toString());
		Assert.assertTrue("Report conflicts should be true.",env.reportConflicts());
		List<Aspect> aspects = env.getAspects();
		Assert.assertTrue("Aspect list should not be empty.",!aspects.isEmpty());
		
		AspectWeaver weaver = new AspectWeaver(env);
		final String stageName = "T_WebService";
		List<IInterpreterArgument> arguments = Main.evaluateStringArgs(args); 
			
		IProgrammaticJoinpoint jp = new PipeStageJoinpoint(stageName);
		IExecutionState state = new ExecutionState(jp,env.getExecutionState().getContextModel(),arguments);
		
		env.beforeExecutionState(state);
		env.updateExecutionState(state);
		
		IInterpreterArgument outArgument = arguments.get(0);
		
		XMLWriter.getInstance().write(outArgument, false);
	}
	

}
