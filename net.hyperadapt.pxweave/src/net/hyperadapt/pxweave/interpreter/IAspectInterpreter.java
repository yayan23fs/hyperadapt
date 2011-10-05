package net.hyperadapt.pxweave.interpreter;

import java.util.ArrayList;
import java.util.List;

import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;

/**
 * 
 * @author msteinfeldt,skarol
 */
public interface IAspectInterpreter {
	
	/**
	 * This method interprets a number of {@link InterpreterArgument}s with
	 * respective to the actual {@link DefaultWeavingContext} and the aspects that
	 * where set by the constructor {@link #AspectInterpreter(ArrayList, short)}
	 * .
	 * 
	 * @param weavingContext
	 *            - the actual context see {@link DefaultWeavingContext}
	 * @return an {@link ArrayList} that contains the result, i.e. a number of
	 *         {@link InterpreterDocumentArgument}s
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	public abstract List<IInterpreterArgument> interprete(IExecutionState executionState, AdviceLocator adviceLocator)
			throws XMLWeaverException;

}