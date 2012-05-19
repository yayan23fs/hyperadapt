package net.hyperadapt.pxweave;

/**
 * Basic interface for execution state notifications.
 * 
 * @author skarol
 *
 */
public interface IExecutionStateProvider {
	
	/**
	 * Gives access to the current execution state.
	 * 
	 * @return An IExectionState object representing the current state.
	 */
	public IExecutionState getExecutionState();
	
	
	/**
	 * Passes the coming state change to the IExecutionStateProvider which is responsible to notify 
	 * its listeners about it. 
	 * 
	 * @param state - The forthcoming state.
	 * @throws XMLWeaverException
	 */
	public void beforeExecutionState(IExecutionState state) throws XMLWeaverException;
	
	
	/**
	 * Passes a state change to the IExecutionStateProvider which is responsible to notify 
	 * its listeners about the change. 
	 * 
	 * @param state - The new state.
	 * @throws XMLWeaverException
	 */
	public void updateExecutionState(IExecutionState state) throws XMLWeaverException;
	
	/**
	 * Register a state observer.
	 * 
	 * @param observer - The observer to be registered.
	 */
	public boolean registerExecutionStateObserver(IExecutionStateObserver observer);
	
	/**
	 * Unregister a state observer.
	 * 
	 * @param observer - The observer to be removed.
	 */

	public boolean deRegisterExecutionStateObserver(IExecutionStateObserver observer);
	
}
