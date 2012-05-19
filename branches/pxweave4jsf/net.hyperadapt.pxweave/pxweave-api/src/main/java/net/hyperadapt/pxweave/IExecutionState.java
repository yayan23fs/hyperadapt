package net.hyperadapt.pxweave;

import java.util.List;

import net.hyperadapt.pxweave.aspects.IProgrammaticJoinpoint;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;


/**
 * Execution states provide information about the envionment's state.
 * 
 * @author skarol
 *
 */
public interface IExecutionState {
	
	/**
	 * Gives access to the current context model.
	 * 
	 * @return - An IWeavingContext representing the current context.
	 */
	public IWeavingContext getContextModel();
	
	/**
	 * Gives access to the data (or core) aspects are to be applied on.
	 * 
	 * @return - A list arguments for the aspect interpreter.
	 */
	public List<IInterpreterArgument> getData();
	
	
	/**
	 * Returns the current joinpoint object.
	 * 
	 * @return - An IProgrammaticJoinpoint object.
	 */
	public IProgrammaticJoinpoint getCurrentJoinpoint();
	
	/**
	 * Set the current joinpoint object.
	 */
	public void setCurrentJoinpoint(IProgrammaticJoinpoint joinpoint);
	
}
