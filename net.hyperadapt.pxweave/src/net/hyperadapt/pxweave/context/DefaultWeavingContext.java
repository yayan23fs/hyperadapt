
package net.hyperadapt.pxweave.context;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.hyperadapt.pxweave.context.ast.Context;
import net.hyperadapt.pxweave.context.ast.Parameter;

/**
 * PXWeave's default weaving context implementation which wraps weaving context XML files.
 * 
 * @author skarol
 *
 */
public class DefaultWeavingContext extends ScopedWeavingContext implements IWeavingContext{		
	private Context context = null;		

	public DefaultWeavingContext(Context context){
		super(null);
		this.context = context;
	}
	
	public DefaultWeavingContext(Context context, IWeavingContext nextContext){
		super(nextContext);
		this.context = context;
	}

	
	@Override
	protected Object getParameterValueCurrentScope(String parameterName) {
		for(Parameter param:context.getStringParameterOrIntegerParameterOrBooleanParameter()){
			if(param.getName().equals(parameterName)){
				try{
					Method getValueMethod = param.getClass().getMethod("getValue");
					return getValueMethod.invoke(param);
				}
				catch(Exception e){
					e.printStackTrace();
					//TODO Should not happen
				}
			}
		}
		return null;
	}
	
	public Context getBaseContext(){
		return context;
	}

	@Override
	protected List<String> getMissingValueParametersCurrentScope(String... parameterNames) {
		List<String> missingValueParameters = new LinkedList<String>();
		for(String parameterName:parameterNames){
			if(getParameterValue(parameterName)==null){
				missingValueParameters.add(parameterName);
			}
		}
		return missingValueParameters;
	}


	
}