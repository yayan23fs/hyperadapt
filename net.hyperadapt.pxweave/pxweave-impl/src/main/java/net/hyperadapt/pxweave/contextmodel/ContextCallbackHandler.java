package net.hyperadapt.pxweave.contextmodel;

import net.hyperadapt.pxweave.util.CallbackHelper;

/**
 * Handler to proceed changed context parameters from a extern ontologie.
 * @author Martin Lehmann
 *
 */
public class ContextCallbackHandler {
	
	private CallbackHelper helper;
	
	public ContextCallbackHandler () {
		helper = CallbackHelper.getInstance();
	}

	public void queryCallback(final String sparqlQuery, final String result) {
		helper.handleQuery(sparqlQuery, result);		
	}
}
