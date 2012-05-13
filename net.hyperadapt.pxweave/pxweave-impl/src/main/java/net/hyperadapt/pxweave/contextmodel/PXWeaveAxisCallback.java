package net.hyperadapt.pxweave.contextmodel;

import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;

public class PXWeaveAxisCallback implements AxisCallback {

	private boolean isComplete = false;
	private String message;
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public void onComplete() {
		isComplete = true;		
	}

	public void onError(Exception exception) {
		System.out.println(exception.getMessage());		
	}

	public void onFault(MessageContext messagecontext) {
		System.out.println(messagecontext.getEnvelope().getBody().getText());
	}

	public void onMessage(MessageContext messagecontext) {
		message = messagecontext.getEnvelope().getBody().getText();
	}
	
	public String getResult() {
		return message;
	}

}
