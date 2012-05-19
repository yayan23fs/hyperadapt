package net.hyperadapt.pxweave;

/**
 * Clients observing the execution state need to implement this interface.
 * 
 * @author skarol
 * 
 */
public interface IExecutionStateObserver {
	
	
	/**
	 * Notifies an observer about the next state.
	 * 
	 * @param nextState
	 * @throws XMLWeaverException
	 */
	public void notifyBeforeUpdate(IExecutionState nextState) throws XMLWeaverException;
	
	
	/**
	 * Notifies the observer about state change. 
	 * 
	 * @param previousState - The previous state.
	 * @throws XMLWeaverException
	 * 
	 */
	public void notifyStateUpdated(IExecutionState previousState) throws XMLWeaverException;
	
}
