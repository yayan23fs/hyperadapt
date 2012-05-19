package net.hyperadapt.pxweave.aspects;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;

/**
 * A simple, named, programmatic joinpoint.
 * 
 * @author skarol,Martin Lehmann
 *
 */
public interface IProgrammaticJoinpoint {
	
	/**
	 * @return  The unique joinpoint name.
	 */
	public String getIdentifier();
	
	/**
	 * Checks if the advices in the given AdviceGroup should be applied at this joinpoint.
	 * 
	 * @param advices - The advice group containing a joinpoint reference.
	 * @return - True, if the group's advices have to be applied at this joinpoint.
	 */
	public boolean applies(AdviceGroup advices, AdviceLocator adviceLocator);
	
	/**
	 * Extended check of applies. 
	 * @param aspect - The advice group containing a joinpoint reference.
	 * @return - True, if the group's advices have to be applied at this joinpoint.
	 */
	public boolean containsApplicableAdvices(Aspect aspect, AdviceLocator adviceLocator);
	
}
