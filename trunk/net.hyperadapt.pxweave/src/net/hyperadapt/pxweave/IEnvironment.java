package net.hyperadapt.pxweave;

import java.net.URI;
import java.util.List;

import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.config.ast.WeaverConfiguration;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.interactions.patternmatrix.ConflictMatrix;
import net.hyperadapt.pxweave.interactions.runtime.DynamicAnalysis;
import net.hyperadapt.pxweave.validation.ValidationMode;

/**
 * The basic interface for configuration access of the environment.
 * 
 * @author skarol
 *
 */
public interface IEnvironment extends IExecutionStateProvider{
	
	public List<Aspect> getAspects();
	
	public WeaverConfiguration getConfiguration();
	
	public IWeavingContext getContextModel();
	
	public IPXWeaveNamespaceContext getNamespaceContext();
	
	public URI getBaseURI();
	
	public ValidationMode getValidationMode();
	
	public boolean isValid();
	
	public ConflictMatrix getConflictMatrix();
	
	public DynamicAnalysis getConflictAnalyser();
	
	public boolean reportConflicts();
}
