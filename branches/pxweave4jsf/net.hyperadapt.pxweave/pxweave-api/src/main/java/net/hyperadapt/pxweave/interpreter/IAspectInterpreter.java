package net.hyperadapt.pxweave.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.Dependency;
import net.hyperadapt.pxweave.aspects.ast.Interface;
import net.hyperadapt.pxweave.evaluation.IXQAbstractArgument;

/**
 * 
 * @author msteinfeldt,skarol
 */
public interface IAspectInterpreter {

	/**
	 * This method interprets a number of {@link InterpreterArgument}s with
	 * respective to the actual {@link DefaultWeavingContext} and the aspects
	 * that where set by the constructor
	 * {@link #AspectInterpreter(ArrayList, short)} .
	 * 
	 * @param weavingContext
	 *            - the actual context see {@link DefaultWeavingContext}
	 * @return an {@link ArrayList} that contains the result, i.e. a number of
	 *         {@link InterpreterDocumentArgument}s
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	public abstract List<IInterpreterArgument> interprete(
			IExecutionState executionState, AdviceLocator adviceLocator)
			throws XMLWeaverException;

	/**
	 * Returns the given {@link HashMap} as {@link ArrayList} and validates the
	 * contained {@link InterpreterDocumentArgument}s if {@link #validateResult}
	 * is true.
	 * 
	 * @param arguments
	 *            - contains the weaving result
	 * @return
	 * @throws XMLWeaverException
	 *             if any error occurred during validation, i.e. if the document
	 *             is invalid or the validation could not take place.
	 */
	public List<IInterpreterArgument> outputArguments(
			HashMap<String, IInterpreterArgument> arguments)
			throws XMLWeaverException;

	/**
	 * This method initializes the {@link InterpreterArgument}s. It sets the
	 * path to the schema associated with the
	 * {@link InterpreterArgument#getId()}, which is the identifier of the core
	 * document as {@link InterpreterArgument#setSchemaPath(String)}. The schema
	 * path is read from the aspect which declares this schema path as the type
	 * for its core document. See{@link Interface#getCore()} It also checks if
	 * there is a core document provided in one of the
	 * {@link InterpreterArgument}s for each core in the aspects and throws an
	 * {@link XMLWeaverException} if not.
	 * 
	 * @param interpreterArguments
	 * @param aspects
	 * @return
	 * @throws XMLWeaverException
	 *             if a core was not found or if more than one of the
	 *             {@link InterpreterArgument}s use the same core and if the
	 *             same core is declared in multiple aspects with different
	 *             type.
	 */
	public HashMap<String, IInterpreterArgument> initialiseArguments(
			final List<IInterpreterArgument> interpreterArguments,
			final List<Aspect> aspects) throws XMLWeaverException;

	/**
	 * Evaluates the {@link Dependency}- declaration of an {@link AdviceGroup},
	 * i.e. states whether an {@link AdviceGroup} is activated or not.
	 * 
	 * @param dependencies
	 *            - the {@link Dependency} that is to be evaluated
	 * @param xqArgument
	 *            - an {@link XQAbstractArgument}, which has values bound for
	 *            {@link Interface#getContextParameters()} and is used as an
	 *            argument for the {@link XQEvaluator} to evaluate the boolean
	 *            expression of {@link AdviceGroup#getDepends()}
	 * @return a boolean to decide whether this {@link AdviceGroup} should
	 *         become active.
	 * @throws XMLWeaverException
	 *             if any error occurred during the evaluation.
	 */
	public boolean evaluateDependency(final String boolExpr,
			final IXQAbstractArgument xqArgument) throws XMLWeaverException;
}