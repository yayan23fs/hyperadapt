package net.hyperadapt.pxweave.context;

import java.util.List;


/**
 * A basic implementation for a chain of weaving contexts. The idea is 
 * to have a kind of (static) default context providing a set default parameters which can 
 * be shadowed or augmented by additional contexts at runtime, or a set of context
 * of other contexts. 
 * 
 * @author skarol
 */
public abstract class ScopedWeavingContext implements IWeavingContext {

	private IWeavingContext nextContext = null;
	
	
	public ScopedWeavingContext(IWeavingContext nextContext){
		this.nextContext = nextContext;
	}
	
	protected abstract Object getParameterValueCurrentScope(String parameterName);
	
	@Override
	public Object getParameterValue(String parameterName) {
		Object currentScopeValue = getParameterValueCurrentScope(parameterName);
		if(currentScopeValue==null&&nextContext!=null){
			return nextContext.getParameterValue(parameterName);
		}
		return currentScopeValue;
	}
	
	@Override
	public boolean isMissingValue(String... parameterNames) {
		List<String> missingValueParameters = getMissingValueParameters(parameterNames);
		return missingValueParameters!=null&&!missingValueParameters.isEmpty();
	}
	
	public List<String> getMissingValueParameters(final String... parameterNames){
		List<String> missingParameterValues = getMissingValueParametersCurrentScope(parameterNames);
		if(!missingParameterValues.isEmpty()&&nextContext!=null){
			return nextContext.getMissingValueParameters((String[])missingParameterValues.toArray());
		}
		return missingParameterValues;
	}
	
	protected abstract List<String> getMissingValueParametersCurrentScope(String... parameterNames);

}
