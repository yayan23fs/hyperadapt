package net.hyperadapt.pxweave.integration.cocoon;

import org.apache.cocoon.transformation.AbstractDOMTransformer;
import org.apache.avalon.framework.parameters.Parameters;

public abstract class AdaptedDOMTransformer extends AbstractDOMTransformer {

	public Parameters getParameters(){
		return super.parameters;
	}
}
