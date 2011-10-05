package net.hyperadapt.pxweave.integration.cocoon;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.environment.SourceResolver;

import net.hyperadapt.pxweave.aspects.IProgrammaticJoinpoint;
import net.hyperadapt.pxweave.aspects.PipeStageJoinpoint;
import net.hyperadapt.pxweave.ExecutionState;
import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.interpreter.InterpreterArgument;
import org.w3c.dom.Document;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.transformation.AbstractDOMTransformer;
import org.apache.cocoon.sitemap.SitemapParameters;

public privileged aspect AspectWeaverAdapter  {
		
	private IProgrammaticJoinpoint currentJoinPoint = null;
	 
	pointcut domTrafoHook(Document doc):
		execution(* (AdaptedDOMTransformer+&&!AdaptedDOMTransformer).transform(Document))
		&& args(doc);
	 
	@SuppressWarnings("unchecked")
	pointcut domSetupHook(SourceResolver resolver, Map objectModel, String source,
			Parameters parameters):
		execution(* AbstractDOMTransformer.setup(SourceResolver,Map,String,Parameters))
		&& args(resolver,objectModel,source,parameters);
	 
	
	before(Document doc, AdaptedDOMTransformer trafo) : domTrafoHook(doc)&&this(trafo){
		currentJoinPoint = deriveCurrentJoinPointName(trafo.getParameters());	
		if(AspectWeaverSetup.getInstance().isInitialised()){
			IExecutionState state = initializeStateDescriptor(doc);
			try {
				AspectWeaverSetup.getInstance().getExcutionStateProvider().beforeExecutionState(state);
			} catch (XMLWeaverException e) {
				AspectWeaverSetup.getInstance().getLogger().error(e.getMessage());
			}
		}
	}
	
	after(Document doc, AdaptedDOMTransformer trafo) returning (Document domTrafoResult): domTrafoHook(doc)&&this(trafo){	
		if(AspectWeaverSetup.getInstance().isInitialised()){
			IExecutionState state = initializeStateDescriptor(domTrafoResult);
			try {
				AspectWeaverSetup.getInstance().getExcutionStateProvider().updateExecutionState(state);
			} catch (XMLWeaverException e) {
				AspectWeaverSetup.getInstance().getLogger().error(e.getMessage());
			}
		}
		currentJoinPoint = null;
	}
	
	declare parents : AbstractDOMTransformer+&&!AbstractDOMTransformer extends AdaptedDOMTransformer;
	
	private IProgrammaticJoinpoint extractJoinpointName(String description){
		Pattern descriptionPattern = Pattern.compile("type[\\p{javaWhitespace}]?=[\\p{javaWhitespace}]?\"[\\w&&[^\"]]+\"");
		Pattern subPattern = Pattern.compile("\"[\\w&&[^\"]]+\"");
			
		Matcher matcher = descriptionPattern.matcher(description);
		if(matcher.find()){
			String typeString = matcher.group();
			matcher = subPattern.matcher(typeString);
			matcher.find();
			String joinpointName = matcher.group();
			return new PipeStageJoinpoint(joinpointName.substring(1,joinpointName.length()-1));
		}
		return null;
	}
	
	
	private IProgrammaticJoinpoint deriveCurrentJoinPointName(Parameters parameters){
		AspectWeaverSetup setup = AspectWeaverSetup.getInstance();
		if(parameters instanceof SitemapParameters){
			IProgrammaticJoinpoint _currentJoinPoint = null;
			SitemapParameters sitemapParameters = (SitemapParameters)parameters;
			String description = sitemapParameters.getLocation().getDescription();
			_currentJoinPoint = extractJoinpointName(description);
			if(_currentJoinPoint==null){
				setup.getLogger().info("Unable to extract joinpoint name from description '"+description+"'.");
			}
			else{
				setup.getLogger().info("Current Joinpoint is "+_currentJoinPoint.getIdentifier());
			}
			return _currentJoinPoint;		
		}
		else{
			setup.getLogger().info("Unable to extract joinpoint since no Sitemap information available.");
			return null;
		}	
	}
	

	
	private IExecutionState initializeStateDescriptor(Document doc){
		AspectWeaverSetup setup = AspectWeaverSetup.getInstance();
		if(setup.isInitialised()){
			InterpreterArgument docArg = new InterpreterArgument(doc, "core");
			final List<IInterpreterArgument> arguments = new LinkedList<IInterpreterArgument>();
			arguments.add(docArg);
			IExecutionState state = new ExecutionState(currentJoinPoint,setup.getEnvironment().getContextModel(),arguments);
			return state;
		}
		else{
			setup.getLogger().warn("Weaver not initialized.");
		}
		return null;
	}
	
	
	
	
	

}
