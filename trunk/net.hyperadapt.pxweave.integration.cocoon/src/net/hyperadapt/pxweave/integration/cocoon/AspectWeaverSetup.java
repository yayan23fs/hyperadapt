package net.hyperadapt.pxweave.integration.cocoon;

import java.net.URI;

import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import net.hyperadapt.pxweave.aspects.AspectWeaver;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.IExecutionStateProvider;
import net.hyperadapt.pxweave.Environment;

public class AspectWeaverSetup extends AbstractLogEnabled {

	public static final String BASE_PATH = "resource/internal/generic_adaptation";

	private static AspectWeaverSetup INSTANCE = null;

	private IEnvironment environment;
	private AspectWeaver weaver;
	
	private AspectWeaverSetup(){
		this.setLogger(	LogFactory.getLog(this.getClass()));	
	}
	 
	public static AspectWeaverSetup getInstance(){
		if(INSTANCE==null)
			INSTANCE = new AspectWeaverSetup();
		return INSTANCE;
	}
	
	
	public AspectWeaver getWeaver(){
		return weaver;
	}
	
	public IEnvironment getEnvironment(){
		return environment;
	}
	
	public IExecutionStateProvider getExcutionStateProvider(){
		return environment;
	}
	
	protected void initialise(SourceResolver resolver) {
		getLogger().info("Initializing HyperAdapt Generic Weaver...");
		
		try {
			Source baseSource = resolver.resolveURI(BASE_PATH);
			environment = Environment.create(URI.create(baseSource.getURI()));
			weaver = new AspectWeaver(environment);
		
		} catch (Exception e1) {		
			getLogger().error("Could not initialize PX-Weave.",e1);
		} 

	}
	
	
	public boolean isInitialised(){
		return weaver!=null&&environment!=null;
	}

}
