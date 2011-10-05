package net.hyperadapt.pxweave.interpreter;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.IExecutionState;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.ContextParameter;
import net.hyperadapt.pxweave.aspects.ast.ContextVarDecl;
import net.hyperadapt.pxweave.aspects.ast.Dependency;
import net.hyperadapt.pxweave.aspects.ast.DocumentParameter;
import net.hyperadapt.pxweave.aspects.ast.Interface;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.evaluation.IXQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XQArgument;
import net.hyperadapt.pxweave.evaluation.XQEvaluator;
import net.hyperadapt.pxweave.interactions.runtime.DynamicAnalysis;
import net.hyperadapt.pxweave.logger.Logable;

import net.hyperadapt.pxweave.validation.DOMValidator;
import net.hyperadapt.pxweave.validation.IDOMFactory;
import net.hyperadapt.pxweave.validation.IDOMParser;
import net.hyperadapt.pxweave.validation.IDOMValidator;
import net.hyperadapt.pxweave.validation.SchemaValidator;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

public class AspectInterpreter extends Logable implements IAspectInterpreter {

	private final IDOMValidator validator;
	private IDOMFactory factory;

	private IEnvironment environment;

	private DynamicAnalysis da;


	/**
	 * Constructor for AspectInterpreter
	 * 
	 * @param aspectList
	 *            A list of Aspects that are supposed to be interpreted by
	 *            {@link #interprete(HashMap, IInterpreterArgument...)} with a
	 *            context.
	 * @param validationStrategy
	 *            A short that decides which instances of {@link IDOMParser} and
	 *            {@link DOMValidator} are used and if operations should be
	 *            validated
	 * @param interpreterArguments
	 *            an {@link List} of {@link IInterpreterArgument}s, that will be
	 *            interpreted by interpreter.
	 * @throws XMLWeaverException
	 */
	public AspectInterpreter(IEnvironment environment) {
		this.environment = environment;
		this.factory = environment.getValidationMode().createDOMFactory();
		this.da = environment.getConflictAnalyser();
		validator = factory.createDOMValidator();
	}

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
			final List<IInterpreterArgument> interpreterArguments, final List<Aspect> aspects)
			throws XMLWeaverException {
		final HashMap<String, IInterpreterArgument> arguments = new HashMap<String, IInterpreterArgument>();
		for (final Aspect aspect : aspects) {
			boolean coreFound = false;
			final String core = aspect.getInterface().getCore().getId();
			URI namespaceURI = URI.create(aspect.getInterface().getCore().getType());
			URI schemaURI = environment.getNamespaceContext().getDefinitionURI(namespaceURI);
			for (final IInterpreterArgument interpreterArgument : interpreterArguments) {
				if (interpreterArgument.getId().contentEquals(core)) {
					if (coreFound) {
						throw new XMLWeaverException("Duplicate argument, core \""
								+ interpreterArgument.getId()
								+ "\" already declared in another interpreterArgument.");
					}
					if (!arguments.containsKey(core)) {
						interpreterArgument.setSchemaURI(schemaURI);
						arguments.put(core, interpreterArgument);
						// core is also declared in another aspect, check if the
						// core types are the same
					} else {
						if (!arguments.get(core).getSchemaURI().equals(schemaURI)) {
							throw new XMLWeaverException("Core \""
									+ aspect.getInterface().getCore().getId()
									+ "\" is declared in another aspect with different type"
									+ "---aspect: \"" + aspect.getName() + "\"");
						}
					}
					coreFound = true;
				}
			}
			if (!coreFound) {
				throw new IllegalArgumentException("Core \""
						+ aspect.getInterface().getCore().getId()
						+ "\" not found in runtime context." + " Core was declared in aspect \""
						+ aspect.getName() + "\"");
			}
		}
		loadAndValidateArguments(arguments);
		return arguments;
	}

	private IXQAbstractArgument initialiseXQueryContext(Aspect aspect, Document coreDocument,
			IWeavingContext context) throws XMLWeaverException {

		// parse and bind documents declared in interface to global context
		// of any following xquery expressions, i.e. bind them as "external"
		// for each query
		IXQAbstractArgument xqArgument = new XQArgument(coreDocument,
				environment.getNamespaceContext(), environment.getNamespaceContext());
		if (aspect.getInterface().getDocumentParameters() != null) {
			final List<DocumentParameter> documentParameters = aspect.getInterface()
					.getDocumentParameters().getDocumentParameter();
			IDOMParser parser = factory.createDOMParser();
			for (final DocumentParameter documentParameter : documentParameters) {
				final String documentName = documentParameter.getName();
				final URI documentURI = URI.create(documentParameter.getUri());
				try {
					Document parameter = null;
					if (documentURI.isAbsolute()) {
						parameter = parser.buildDOM(documentURI);
					} else {
						parameter = parser.buildDOM(environment.getBaseURI().resolve(documentURI));
					}

					xqArgument.addVariableAndBindValue(documentName, parameter);
				} catch (final IllegalArgumentException e) {
					throw new XMLWeaverException("Document parameter not bound---aspect name: "
							+ aspect.getName() + "---reason: " + e.getLocalizedMessage(), e);
				}

			}

		}

		ContextVarDecl contextVarDecl = aspect.getInterface().getContextParameters();

		// bind contextParameter values to global context of any following
		// xquery expressions, i.e. bind them as "external" for each query
		if (contextVarDecl != null && contextVarDecl.getParameter() != null) {
			for (final ContextParameter para : contextVarDecl.getParameter()) {
				final Object value = context.getParameterValue(para.getName());

				if (value != null) {
					try {
						xqArgument.addVariableAndBindValue(para.getName(), value);
					} catch (final Exception e) {
						throw new XMLWeaverException(
								"error: aspect parameter values not bound---aspect name: "
										+ aspect.getName() + "---reason: "
										+ e.getLocalizedMessage(), e);
					}
				} else {
					throw new XMLWeaverException(
							"error: aspect parameter values not bound---aspect name: "
									+ aspect.getName() + "---parameter name:" + para.getName());
				}
			}
		}
		return xqArgument;
	}

	public void loadAndValidateArguments(HashMap<String, IInterpreterArgument> arguments)
			throws XMLWeaverException {

		final IDOMParser parser = factory.createDOMParser();
		for (final IInterpreterArgument argument : arguments.values()) {
			if (!argument.isLoaded()) {
				// throws exception if argument is not valid
				argument.loadDocument(parser, environment.getValidationMode().isValidateInput());
			} else {
				if (environment.getValidationMode().isValidateInput()) {
					// TODO setting schema for validator --> Hack!
					if (validator.needsSchema()) {
						validator.setSchema(argument.getSchemaURI());
					}
					// throws exception if document is not valid
					validator.validateDocument(argument.getDocument());
				}
			}
		}

	}

	/**
	 * Checks if all parameters of an aspect are declared in the current
	 * context.
	 * 
	 * @param aspect
	 *            - {@link Aspect}
	 * @param context
	 *            - {@link DefaultWeavingContext}
	 * @return True, if all parameters are declared.
	 */

	private boolean checkContextParameters(Aspect aspect, IWeavingContext context) {
		boolean allDeclared = true;
		if (aspect.getInterface().getContextParameters() != null) {
			for (final ContextParameter parameter : aspect.getInterface().getContextParameters()
					.getParameter()) {
				if (context.isMissingValue(parameter.getName())) {
					getLogger().info(
							"Missing parameter '" + parameter.getName() + "' for aspect '"
									+ aspect.getName() + "'");
					allDeclared = false;
				}
			}
		}
		return allDeclared;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.hyperadapt.pxweave.interpreter.IAspectInterpreter#interprete(net.
	 * hyperadapt.pxweave.interpreter.WeavingContext, java.util.ArrayList)
	 */
	public List<IInterpreterArgument> interprete(IExecutionState executionState,
			AdviceLocator adviceLocator) throws XMLWeaverException {

		// System.out.println("Executionstate "+executionState.getData().size());

		final HashMap<String, IInterpreterArgument> iArguments = initialiseArguments(
				executionState.getData(), environment.getAspects());
		if (environment.reportConflicts()) {
			da.setPPC(executionState.getCurrentJoinpoint(), adviceLocator);
		}
		for (final Aspect aspect : environment.getAspects()) {
			if (!executionState.getCurrentJoinpoint().containsApplicableAdvices(aspect,
					adviceLocator)) {
				continue;
			}
			getLogger().debug(
					"Aspect '" + aspect.getName() + "' may contain applicable advices ("
							+ adviceLocator.toString() + " '"
							+ executionState.getCurrentJoinpoint().getIdentifier() + "').");
			getLogger().info("Evaluating aspect \"" + aspect.getName() + "\"");
			// get the core document for this aspect, use it as query document
			// for the XQuery expressions

			// da.testit();
			String coreId = aspect.getInterface().getCore().getId();
			final IInterpreterArgument coreDocumentArgument = iArguments.get(coreId);
			final Document coreDocument = coreDocumentArgument.getDocument();
			if (validator.needsSchema()) {
				// TODO check tests
				validator.setSchema(coreDocumentArgument.getSchemaURI());
			}

			// get the declared parameters of this aspect and check if there are
			// values associated with them
			getLogger().debug("Checking if all required context parameters exist ...");
			if (!checkContextParameters(aspect, executionState.getContextModel())) {
				throw new XMLWeaverException("Missing context parameters for aspect '"
						+ aspect.getName() + "', see Log for details.");
			}

			getLogger().debug("Initializing context for XQuery based evaluation ...");
			IXQAbstractArgument xqContext = initialiseXQueryContext(aspect, coreDocument,
					executionState.getContextModel());

			final List<AdviceGroup> groups = aspect.getAdviceGroup();
			boolean appliedAdvice = false;
			for (final AdviceGroup adviceGroup : groups) {
				if (!executionState.getCurrentJoinpoint().applies(adviceGroup, adviceLocator)) {
					continue;
				}
				// test if advice group should be activated according to context
				// parameter values and dependencies expression
				boolean isActive = false;

				try {
					isActive = evaluateDependency(adviceGroup.getDepends(), xqContext);
				} catch (final Exception e) {
					throw new XMLWeaverException(
							"Can't evaluate dependencies [Aspect '" + aspect.getName()
									+ "', reason: " + e.getMessage()+"]", e);
				}

				if (isActive) {
					Document result = interpreteAdviceGroup(adviceGroup, aspect, xqContext,
							coreDocument);

					// TODO check if setting the result is really needed
					final IInterpreterArgument resultArgument = iArguments.get(coreId);
					resultArgument.setDocument(result);
					iArguments.put(coreId, resultArgument);
					appliedAdvice = true;
				}
			}
			if(!appliedAdvice)
				getLogger().info("Did not find any active advice applicable to content, document was not modified.");
		}
		if (environment.reportConflicts()) {
			if (adviceLocator.toString().equalsIgnoreCase("before"))
				da.setLogged(true);
			else
				da.setLogged(false);
			da.logErrors();
		}

		return outputArguments(iArguments);
	}

	private Document interpreteAdviceGroup(AdviceGroup adviceGroup, Aspect aspect,
			IXQAbstractArgument xqContext, Document coreDocument) throws XMLWeaverException {
		final AdviceContext adviceContext = new AdviceContext(adviceGroup.getScope(),
				aspect.getName());

		AdviceInterpreter adviceInterpreter;
		if (environment.getValidationMode().isValidateOperations()) {
			adviceInterpreter = new AdviceInterpreter(adviceContext, coreDocument, xqContext,
					new OperationInterpreter(validator), environment);
		} else {
			adviceInterpreter = new AdviceInterpreter(adviceContext, coreDocument, xqContext,
					new OperationInterpreter(), environment);
		}
		Document result = null;
		result = adviceInterpreter.interpreteAdvices(adviceGroup.getAdvices()
				.getChooseVariantOrChangeOrderOrChangeValue());

		return result;

	}

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
			HashMap<String, IInterpreterArgument> arguments) throws XMLWeaverException {
		ArrayList<IInterpreterArgument> arrayList = new ArrayList<IInterpreterArgument>();
		if (environment.getValidationMode().isValidateResult()) {
			final SchemaValidator schemaValidator = SchemaValidator.getInstance();
			for (final IInterpreterArgument argument : arguments.values()) {
				schemaValidator.documentIsValid(argument.getDocument(), argument.getSchemaURI());
			}
		}
		arrayList.addAll(arguments.values());
		return arrayList;
	}

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
	public static boolean evaluateDependency(final Dependency dependencies,
			final IXQAbstractArgument xqArgument) throws XMLWeaverException {
		// test if advice group should be activated according to context
		// parameter values and dependencies expression
		boolean isActive = true;
		if (dependencies != null) {
			try {
				xqArgument.setExpression(dependencies.getBoolExpr());
				final ArrayList<Text> result = XQEvaluator.evaluateToTextNodes(xqArgument);
				final String activate = result.get(0).getNodeValue();
				if (activate.equals("true")) {
					isActive = true;
				} else if (activate.equals("false")) {
					isActive = false;
				} else {
					throw new XMLWeaverException("Evaluation of expression \""
							+ dependencies.getBoolExpr() + "\" does not result in a boolean value");
				}
			} catch (final Exception e) {
				throw new XMLWeaverException(e.getMessage(),e);

			}
		}
		return isActive;
	}

}
