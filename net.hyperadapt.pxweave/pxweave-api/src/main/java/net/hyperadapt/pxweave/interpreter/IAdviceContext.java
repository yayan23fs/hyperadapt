package net.hyperadapt.pxweave.interpreter;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Scope;

/**
 * This Class represents the context of an {@link AdviceGroup}, i.e. the
 * aspect's name and the advice's scope
 */
public interface IAdviceContext {

	/**
	 * Get the name of the aspect this advice context belongs to, i.e. the
	 * aspect that also contains the advice group
	 * 
	 * @return
	 */
	String getAspectName();

	/**
	 * Get the scope of the advice group.
	 * 
	 * @return
	 */
	Scope getScope();
}
