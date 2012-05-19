package net.hyperadapt.pxweave.context;

import java.util.List;

/**
 * A very basic interface for context models based on simple parameter - value relations.
 * 
 * @author skarol
 */

public interface IWeavingContext {
	
	public Object getParameterValue(String parameterName);
	
	public boolean isMissingValue(final String... parameterNames);
	
	public List<String> getMissingValueParameters(final String... parameterNames);
	
}
