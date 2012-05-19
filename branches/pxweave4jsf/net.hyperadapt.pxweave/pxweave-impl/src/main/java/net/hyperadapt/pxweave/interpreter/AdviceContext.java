package net.hyperadapt.pxweave.interpreter;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.Scope;


/**
 * This Class represents the context of an {@link AdviceGroup}, i.e. the
 * aspect's name and the advice's scope
 */
public class AdviceContext implements IAdviceContext {

	private final Scope scope;
	private final String aspectName;

	/**
	 * Constructor for advice context.
	 * 
	 * @param scope
	 *            An XPath expression that selects the context item for any
	 *            XPath expression in the {@link AdviceGroup it belongs to}
	 * @param aspectName
	 *            The name of the {@link Aspect} this advice context belongs to
	 * @param interf
	 *            The {@link Interface} of the aspect this advice context
	 *            belongs to
	 * @param core
	 *            A String that identifies the associated core document
	 */
	public AdviceContext(final Scope scope, final String aspectName) {
		this.scope = scope;
		this.aspectName = aspectName;
	}

	/**
	 * Get the name of the aspect this advice context belongs to, i.e. the
	 * aspect that also contains the advice group
	 * 
	 * @return
	 */
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * Get the scope of the advice group.
	 * 
	 * @return
	 */
	public Scope getScope() {
		return scope;
	}
}
