package net.hyperadapt.pxweave;

import java.net.URI;
import java.util.List;

import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.config.ast.WeaverConfiguration;
import net.hyperadapt.pxweave.interactions.patternmatrix.IConflictMatrix;
import net.hyperadapt.pxweave.interactions.runtime.IDynamicAnalysis;
import net.hyperadapt.pxweave.validation.IValidationMode;

/**
 * The basic interface for configuration access of the environment.
 * 
 * @author skarol
 *
 */
public interface IEnvironment extends IExecutionStateProvider{
	
	List<Aspect> getAspects();
	
	void setAspects(List<Aspect> aspects);
	
	WeaverConfiguration getConfiguration();
	
	IPXWeaveNamespaceContext getNamespaceContext();
	 
	URI getBaseURI();
	
	IValidationMode getValidationMode();
	
	boolean isValid();
	
	IConflictMatrix getConflictMatrix();
	 
	IDynamicAnalysis getConflictAnalyser();
	
	boolean reportConflicts();
}
