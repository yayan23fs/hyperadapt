package net.hyperadapt.pxweave.integration.cocoon;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

public class AspectWeaverSetupAction extends org.apache.cocoon.acting.AbstractAction{
	@SuppressWarnings("unchecked")
	public Map act(Redirector arg0,SourceResolver resolver, Map objectModel, String source,
			Parameters parameters) throws Exception {
		AspectWeaverSetup.getInstance().initialise(resolver);
		return null; 
	}
}
