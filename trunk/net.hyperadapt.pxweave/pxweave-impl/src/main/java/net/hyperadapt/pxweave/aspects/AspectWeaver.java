package net.hyperadapt.pxweave.aspects;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.ExecutionState;
import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.IExecutionStateObserver;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.XMLWriter;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.interpreter.AspectInterpreter;
import net.hyperadapt.pxweave.interpreter.IAspectInterpreter;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.interpreter.InterpreterArgument;
import net.hyperadapt.pxweave.logger.Logable;

/**
 * The AspectWeaver observes the execution state of the environment and reacts
 * on state changes by weaving aspects appropriate to the joinpoint the current
 * state represents and the current data set consisting of the context model and
 * input and output parameters.
 * 
 * @author skarol,msteinfeldt
 */
public class AspectWeaver extends Logable implements IExecutionStateObserver {

	private IEnvironment environment;
	private IAspectInterpreter aspectInterpreter;

	/**
	 * Instantiate the weaver by passing a fresh environment. The Weaver will
	 * register itself to the environment as an execution state observer.
	 * 
	 * @param environment
	 *            - The environment where the weaver is executed.
	 */
	public AspectWeaver(IEnvironment environment) {
		this.environment = environment;
		this.environment.registerExecutionStateObserver(this);
		String aspectOrderString = "";
		Iterator<Aspect> it = environment.getAspects().iterator();
		while (it.hasNext()) {
			aspectOrderString += it.next().getName();
			if (it.hasNext())
				aspectOrderString += ",";
		}
		getLogger().info(
				"Aspects will be woven in the order [" + aspectOrderString
						+ "]");

		// DynamicAnalysis.getILogger().info("\n");
		// DynamicAnalysis.getILogger().info(" -- start new analysis heat  -- ");
	}

	/**
	 * Invoke the weaver on a specific execution state. The weaver will delegate
	 * the actual aspect interpretation to an AspectInterpreter.
	 * 
	 * @param executionState
	 *            - The execution state to invoke the weaver on.
	 * @return Currently the weaver flat copies the data in the execution state,
	 *         excluding input and output locations. These copies are returned.
	 * @throws IXMLWeaverException
	 */
	private List<IInterpreterArgument> weave(IExecutionState executionState,
			AdviceLocator adviceLocator) throws XMLWeaverException {

		final ArrayList<IInterpreterArgument> interpreterArguments = new ArrayList<IInterpreterArgument>();
		for (final IInterpreterArgument docArg : executionState.getData()) {
			final IInterpreterArgument interpreterArgument = new InterpreterArgument(
					docArg.getDocument(), docArg.getId());
			interpreterArguments.add(interpreterArgument);
		}
		aspectInterpreter = new AspectInterpreter(environment);
		return aspectInterpreter.interprete(executionState, adviceLocator);
	}

	public void notifyStateUpdated(IExecutionState state) throws XMLWeaverException {
		weave(environment.getExecutionState(), AdviceLocator.AFTER);
	}
 
	/**
	 * Invoke the weaver statically, e.g., from command line.
	 * 
	 * @param baseURI
	 *            - The base URI where the weaver config is located.
	 * @param args
	 *            - The arguments the weaver should be invoked on, including
	 *            input and output files(!).
	 * @throws IXMLWeaverException
	 */
	public static void weave(URI baseURI, List<IInterpreterArgument> args)
			throws XMLWeaverException {
		weave(Environment.create(baseURI), args);
	}

	/**
	 * Invoke the weaver statically, e.g., from command line using a self
	 * defined environment. The weaver will only be executed once at the current
	 * joinpoint.
	 * 
	 * @param environment
	 *            - An initialised environment
	 * @param args
	 *            - The arguments the weaver should be invoked on, including
	 *            input and output files.
	 * @throws IXMLWeaverException
	 */
	public static void weave(IEnvironment environment,
			List<IInterpreterArgument> args) throws XMLWeaverException {
		AspectWeaver weaver = new AspectWeaver(environment);
		XMLWriter writer = XMLWriter.getInstance();
		IExecutionState state = new ExecutionState(environment
				.getExecutionState().getCurrentJoinpoint(), environment
				.getExecutionState().getContextModel(), args);
		environment.beforeExecutionState(state);
		environment.updateExecutionState(state);

		for (final IInterpreterArgument argument : args) {
			argument.setOutputStream(writer.write(argument, false));
		}
		environment.deRegisterExecutionStateObserver(weaver);
	}

	public void notifyBeforeUpdate(IExecutionState nextState)
			throws XMLWeaverException {
		weave(nextState, AdviceLocator.BEFORE);
	}

}
