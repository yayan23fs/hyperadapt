package net.hyperadapt.pxweave.context;

import java.util.List;

import net.hyperadapt.pxweave.context.ast.Parameter;

/**
 * A very basic interface for context models based on simple parameter - value relations.
 * 
 * @author skarol
 */

public interface IWeavingContext {
	
	Object getParameterValue(String parameterName);
	
	void addParameter(Parameter param);
	
	boolean isMissingValue(final String... parameterNames);

	List<String> getMissingValueParameters(String[] array);
	
	Parameter getParameter(String parameterName);
	
}
