package net.hyperadapt.pxweave.contextmodel;

import net.hyperadapt.pxweave.util.CallbackHelper;


public class ContextCallbackHandler {
	
	private CallbackHelper helper;
	
	public ContextCallbackHandler () {
		helper = CallbackHelper.getInstance();
	}

	public void queryCallback(final String sparqlQuery, final String result) {
		helper.handleQuery(sparqlQuery, result);		
	}
}
