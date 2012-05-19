package net.hyperadapt.pxweave.aspects;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.ProgrammaticScope;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;

/**
 * Default joinpoint implementation for programmatic joinpoints in 
 * XML pipelines.
 * 
 * @author skarol
 */

public class PipeStageJoinpoint implements IProgrammaticJoinpoint {
	
	private String stageName = null;
	
	public PipeStageJoinpoint(String stageName){
		this.stageName = stageName;
	}
	
	@Override
	public boolean applies(AdviceGroup advices, AdviceLocator adviceLocator) {
		
		if((advices.getScope().getJoinpoint()==null||
				advices.getScope().getJoinpoint().isEmpty())){
			if(adviceLocator==AdviceLocator.AFTER)
				return true;
			return false;
		}
		
		for(ProgrammaticScope scope:advices.getScope().getJoinpoint()){
			if(this.getIdentifier().equals(scope.getName())){
				if(adviceLocator == scope.getLocator())
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsApplicableAdvices(Aspect aspect, AdviceLocator adviceLocator) {
		for(AdviceGroup group:aspect.getAdviceGroup()){
			if(applies(group,adviceLocator))
				return true;
		}
		return false;
	}

	@Override
	public String getIdentifier() {
		return stageName;
	}

}
