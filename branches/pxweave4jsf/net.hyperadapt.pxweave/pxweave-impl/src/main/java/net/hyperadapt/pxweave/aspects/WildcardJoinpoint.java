package net.hyperadapt.pxweave.aspects;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.Aspect;

/**
 * 
 * A joinpoint where always all advices are to be applied.
 * 
 * @author skarol
 *
 */
public class WildcardJoinpoint implements IProgrammaticJoinpoint{

	public boolean applies(AdviceGroup advices, AdviceLocator adviceLocator) {
		if(adviceLocator==AdviceLocator.AFTER)
			return true;
		return false;
	}

	public boolean containsApplicableAdvices(Aspect aspect, AdviceLocator adviceLocator) {
		if(adviceLocator==AdviceLocator.AFTER)
			return true;
		return false;
	}

	public String getIdentifier() {
		return "*";
	}
	
}