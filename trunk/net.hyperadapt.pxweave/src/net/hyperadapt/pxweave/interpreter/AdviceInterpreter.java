package net.hyperadapt.pxweave.interpreter;


import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.hyperadapt.pxweave.IEnvironment;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
import net.hyperadapt.pxweave.aspects.ast.ChangeOrder;
import net.hyperadapt.pxweave.aspects.ast.ChangeValue;
import net.hyperadapt.pxweave.aspects.ast.ChooseVariant;
import net.hyperadapt.pxweave.aspects.ast.CollapseElement;
import net.hyperadapt.pxweave.aspects.ast.ComplexAdvice;
import net.hyperadapt.pxweave.aspects.ast.Delete;
import net.hyperadapt.pxweave.aspects.ast.ElementTemplate;
import net.hyperadapt.pxweave.aspects.ast.EnrichContent;
import net.hyperadapt.pxweave.aspects.ast.ExpandElement;
import net.hyperadapt.pxweave.aspects.ast.FillComponentByID;
import net.hyperadapt.pxweave.aspects.ast.InsertElement;
import net.hyperadapt.pxweave.aspects.ast.MoveElement;
import net.hyperadapt.pxweave.aspects.ast.ReduceContent;
import net.hyperadapt.pxweave.aspects.ast.SimpleAdvice;
import net.hyperadapt.pxweave.aspects.ast.TextTemplate;
import net.hyperadapt.pxweave.aspects.ast.Var;
import net.hyperadapt.pxweave.aspects.ast.VarDecl;
import net.hyperadapt.pxweave.aspects.ast.XPath;
import net.hyperadapt.pxweave.evaluation.IXQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.evaluation.XQAbstractArgument;
import net.hyperadapt.pxweave.evaluation.XQEvaluator;
import net.hyperadapt.pxweave.evaluation.XQLocalArgument;
import net.hyperadapt.pxweave.interactions.runtime.AdviceToCheck;
import net.hyperadapt.pxweave.interactions.runtime.DynamicAnalysis;
import net.hyperadapt.pxweave.logger.Logable;
import net.hyperadapt.pxweave.util.DOMOperations;
import net.sf.saxon.query.XQueryExpression;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * AdviceInterpreter interprets advices, i.e. uses the {@link OperationInterpreter}  to realize them.
 */
public class AdviceInterpreter extends Logable {

	private final AdviceContext adviceContext;
	private final Document document;
	private final IXQAbstractArgument xqAbstractArgument;
	private OperationInterpreter operationInterpreter;
	private IEnvironment environment;
	private XPathEvaluator xPathEvaluator;

	private DynamicAnalysis conflictAnalyser;

	/**
	 * Constructor for {@link AdviceInterpreter}
	 * 
	 * @param adviceContext
	 *            The {@link AdviceContext} for the advice that is to be
	 *            interpreted.
	 * @param affectedCore
	 *            The {@link Document} that is the core for the aspect the
	 *            advice belongs to.
	 * @param operationInterpreter
	 *            The operationInterpreter that is used to interpret the
	 *            DOM-Operations.
	 * @param xqArgument
	 *            A {@link XQAbstractArgument} that encapsulates an
	 *            {@link XQueryExpression}, which has variables bound from the
	 *            aspect's interface
	 */
	public AdviceInterpreter(final AdviceContext adviceContext, final Document affectedCore,
			final IXQAbstractArgument xqArgument, final OperationInterpreter operationInterpreter,
			IEnvironment environment) {
		this.document = affectedCore;
		this.adviceContext = adviceContext;
		this.xqAbstractArgument = xqArgument;
		this.operationInterpreter = operationInterpreter;
		this.environment = environment;
		this.conflictAnalyser = environment.getConflictAnalyser();
		xPathEvaluator = new XPathEvaluator(environment.getNamespaceContext());
	}

	/**
	 * Interprets the given advices. The DOM operations are directly performed
	 * on the document given at instantiation of {@link AdviceInterpreter}. The
	 * woven {@link Document} is returned as the result of those operations.
	 * 
	 * @param advices
	 *            - the advices that are supposed to be interpreted
	 * @return the woven {@link Document}
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	public Document interpreteAdvices(final List<BasicAdvice> advices) throws XMLWeaverException {
		if (environment.reportConflicts()) {
			conflictAnalyser.setNamespaceContext(environment.getNamespaceContext());
			conflictAnalyser.setAdviceContext(adviceContext);
		}
		// da.testit();
		for (final BasicAdvice advice : advices) {
			if(adviceContext.getScope().getXpath()!=null&&!adviceContext.getScope().getXpath().isEmpty()){
				for (final XPath point : adviceContext.getScope().getXpath()) {
					interpreteAdvice(advice,point.getValue());
				}			
			}
			else{
				interpreteAdvice(advice,null);
			}
	
		}
		if (environment.reportConflicts()) {
			conflictAnalyser.addDocumentAsAfterForLastAdvice(getCopyOf(document));
		}
		// hier vergleich intersect

		return this.document;
	}
	
	private void interpreteAdvice(BasicAdvice advice, String scope) throws XMLWeaverException{
		getLogger().debug("---interpreting advice:" + advice.getClass().getSimpleName());
		final NodeList joinPoints;
		final String subpath = advice.getPointcut().getValue();
		
		String fullXPath = scope==null?subpath : (scope + "/" + subpath);
		try {
			joinPoints = xPathEvaluator.evaluateXPath(fullXPath,document);
			if (environment.reportConflicts()) {
				// füge dokument als "after dokument" dem vorherigen
				// Advice hinzu
				conflictAnalyser.addDocumentAsAfterForLastAdvice(getCopyOf(document));
				// erzeuge neuen ATC mit Kopie des derzeitigen Document
				// als Before
				AdviceToCheck atc = new AdviceToCheck(advice, joinPoints);
				atc.setXPath(fullXPath);
				atc.setBeforeDocument(getCopyOf(document));
				conflictAnalyser.addAdvice(atc);
			}

		} catch (final XMLWeaverException e) {
			throw new XMLWeaverException("Problem with pointcut. [Aspect '"
							+ adviceContext.getAspectName() + "', advice '"
							+ advice.getClass().getSimpleName() + "', path '"
					+ advice.getPointcut().getValue() + ", cause: "
					+ e.getLocalizedMessage()+"]", e);
		}
		if (joinPoints.getLength() == 0) {
			getLogger().info(
					"No nodes selected. [Aspect '"
							+ adviceContext.getAspectName() + "', advice '"
							+ advice.getClass().getSimpleName() + "', path '"
							+ fullXPath+"']");
		}
		else{
			try {
				if (advice instanceof SimpleAdvice) {
					interpreteSimpleAdvice((SimpleAdvice) advice, joinPoints,fullXPath);
				} else if (advice instanceof ComplexAdvice) {
					interpreteComplexAdvice((ComplexAdvice) advice, joinPoints,fullXPath);
				} else {
					interpreteBasicAdvice(advice, joinPoints,fullXPath);
				}
				getLogger().info("---done with " + advice.getClass().getSimpleName());
			} catch (final XMLWeaverException e) {
				throw new XMLWeaverException(e.getMessage() + " [Aspect '"
							+ adviceContext.getAspectName() + "', advice '"
							+ advice.getClass().getSimpleName() + "', path '"
							+ fullXPath+"']", e);
			}				
		}
	}

	private Document getCopyOf(Document source) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document target;

		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		target = builder.newDocument();
		
		Element n = source.getDocumentElement();
		
		target.appendChild(target.adoptNode(n.cloneNode(true)));

		return target;
	}

	/**
	 * Evaluates the String representation of an {@link ElementTemplate}.
	 * 
	 * @param varDecl
	 *            - the {@link VarDecl} of a {@link ComplexAdvice}
	 * @param joinPointXPath
	 *            A String that contains a XPath expression to select the
	 *            joinPoints.
	 * @param template
	 *            - the template as String
	 * @return an {@link ArrayList} that holds the results
	 * @throws XMLWeaverException
	 *             if the template can't be evaluated.
	 */
	protected ArrayList<Element> evaluateElementTemplate(final VarDecl varDecl,
			final String joinPointXPath, final String template) throws XMLWeaverException {
		ArrayList<Element> result = null;

		try {
			final IXQAbstractArgument xqLocal = new XQLocalArgument(xqAbstractArgument,
					environment.getNamespaceContext(), environment.getNamespaceContext());
			xqLocal.declarePointcut(joinPointXPath);
			xqLocal.setExpression(template);
			if (varDecl != null) {
				for (final Var varDeclaration : varDecl.getVar()) {
					xqLocal.addVariableAndBindEvValue(varDeclaration.getName(), varDeclaration
							.getXpath().getValue());
				}
			}
			result = XQEvaluator.evaluateToElementNodes(xqLocal);
		} catch (final Exception e) {
			throw new XMLWeaverException("Can't evaluate elementTemplate, "
					+ e.getLocalizedMessage(), e);
		}

		return result;

	}

	/**
	 * Evaluates a {@link TextTemplate} of a {@link SimpleAdvice}.
	 * 
	 * @param varDecl
	 *            - the {@link VarDecl} of the {@link SimpleAdvice}
	 * @param joinPointXPath
	 *            - an XPath expression that select the joinPoints
	 * @param template
	 *            - the {@link TextTemplate}
	 * @return an {@link ArrayList} that contains the result
	 * @throws XMLWeaverException
	 *             if any error occurred during evaluation.
	 */
	protected ArrayList<Text> evaluateTextTemplate(final VarDecl varDecl,
			final String joinPointXPath, final String template) throws XMLWeaverException {
		ArrayList<Text> result = null;
		try {
			final IXQAbstractArgument xqLocal = new XQLocalArgument(xqAbstractArgument,
					environment.getNamespaceContext(), environment.getNamespaceContext());
			xqLocal.declarePointcut(joinPointXPath);
			xqLocal.setExpression(template);
			if (varDecl != null) {
				for (final Var varDeclaration : varDecl.getVar()) {
					xqLocal.addVariableAndBindEvValue(varDeclaration.getName(), varDeclaration
							.getXpath().getValue());
				}
			}
			result = XQEvaluator.evaluateToTextNodes(xqLocal);
		} catch (final Exception e) {
			throw new XMLWeaverException("Can't evaluate textTemplate, " + e.getLocalizedMessage(),
					e);
		}
		return result;
	}

	/**
	 * Interprets a {@link BasicAdvice}
	 * 
	 * @param advice
	 *            - the {@link BasicAdvice}
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the join points for this
	 *            advice
	 * @param joinPointXPath
	 *            - the XPath expression that points to the joinPoints
	 * @throws XMLWeaverException
	 *             if any error occurred during evaluation.
	 */
	private void interpreteBasicAdvice(final BasicAdvice advice, final NodeList joinPoints,
			final String joinPointXPath) throws XMLWeaverException {
		if (advice instanceof Delete) {
			interpreteDelete(joinPoints);
		} else if (advice instanceof ChangeOrder) {
			interpreteChangeOrder((ChangeOrder) advice, joinPoints);
		} else if (advice instanceof MoveElement) {
			interpreteMoveElement((MoveElement) advice, joinPoints);
		} else if (advice instanceof ChooseVariant) {
			interpreteChooseVariant(joinPoints);
		} else if (advice instanceof ReduceContent) {
			interpreteReduceContent((ReduceContent) advice, joinPoints);
		} else if (advice instanceof FillComponentByID) {
			interpreteFillComponentByID((FillComponentByID) advice, joinPoints, joinPointXPath);
		}
	}

	/**
	 * Interprets a {@link SimpleAdvice}
	 * 
	 * @param advice
	 *            - the {@link SimpleAdvice}
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the join points for this
	 *            advice
	 * @param joinPointXPath
	 *            - the XPath expression that points to the joinPoints
	 * @throws XMLWeaverException
	 *             if any error occurred during evaluation.
	 */
	private void interpreteSimpleAdvice(final SimpleAdvice advice, final NodeList joinPoints,
			final String joinPointXPath) throws XMLWeaverException {
		final VarDecl varDecl = (advice).getValue().getVarDecl();
		final String template = (advice).getValue().getTextTemplate().getValue();
		final ArrayList<Text> evaluatedTemplate = evaluateTextTemplate(varDecl, joinPointXPath,
				template);
		AdviceInterpreter.checkTemplateSize(evaluatedTemplate);
		if (advice instanceof ChangeValue) {
			interpreteChangeValue(joinPoints, evaluatedTemplate);
		}
		if (advice instanceof CollapseElement) {
			interpreteCollapseElement(joinPoints, evaluatedTemplate);
		}
		if (advice instanceof EnrichContent) {
			interpreteEnrichContent((EnrichContent) advice, joinPoints, evaluatedTemplate);
		}

	}

	/**
	 * Interprets a {@link ComplexAdvice}
	 * 
	 * @param advice
	 *            - the {@link ComplexAdvice}
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the join points for this
	 *            advice
	 * @param joinPointXPath
	 *            - the XPath expression that points to the joinPoints
	 * @throws XMLWeaverException
	 *             if any error occurred during evaluation.
	 */
	private void interpreteComplexAdvice(final ComplexAdvice advice, final NodeList joinPoints,
			final String joinPointXPath) throws XMLWeaverException {
		final VarDecl varDecl = (advice).getValue().getVarDecl();
		final List<Object> templObjects = advice.getValue().getElementTemplate().getContent();

		if (templObjects == null || templObjects.isEmpty()) {
			throw new XMLWeaverException(
					"ElementTemplate must contain element or string declarations");
		}

		String expression = "";
		for (Object o : templObjects) {
			if (o instanceof String) {
				expression = expression + o.toString();
			} else if (o instanceof Element) {
				Node adoptedNode = document.importNode((Element) o, true);
				expression = expression + DOMOperations.convertNodeToString(adoptedNode);
			} else {
				throw new XMLWeaverException(
						"Unknown XML content in template---ElementTemplate may only contain Elements or String nodes.");
			}
		}
		//getLogger().info(expression);

		final ArrayList<Element> evaluatedTemplate = this.evaluateElementTemplate(varDecl,
				joinPointXPath, expression);
		AdviceInterpreter.checkTemplateSize(evaluatedTemplate);
		if (advice instanceof InsertElement) {
			interpreteInsertElement((InsertElement) advice, joinPoints, evaluatedTemplate);
		}
		if (advice instanceof ExpandElement) {
			interpreteExpandElement(joinPoints, evaluatedTemplate);
		}
	}

	/**
	 * Interprets a {@link FillComponentByID}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link FillComponentByID} that is to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param joinPointXPath
	 *            - an XPath expression that points to the selected joinPoints.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteFillComponentByID(final FillComponentByID advice,
			final NodeList joinPoints, final String joinPointXPath) throws XMLWeaverException {
		final FillComponentByID fillComponentByID = advice;
		final String nameOfTargetID = fillComponentByID.getIdentifyingAttribute();
		final String nameOfSourceID = fillComponentByID.getSource().getIdentifyingAttribute();
		final String nameOfSourceDoc = fillComponentByID.getSource().getDocument();
		final String xPathToSourceContainer = fillComponentByID.getSource().getSourceComponents()
				.getValue();
		final IXQAbstractArgument xqLocal = new XQLocalArgument(xqAbstractArgument,
				environment.getNamespaceContext(), environment.getNamespaceContext());
		xqLocal.declarePointcut(joinPointXPath);
		String expression;
		if (nameOfSourceDoc == null) {
			expression = xPathToSourceContainer + "[" + "./attribute::" + nameOfSourceID + "="
					+ " $joinPoint/attribute::" + nameOfTargetID + "]";
		} else {
			expression = "$" + nameOfSourceDoc + xPathToSourceContainer + "[" + "./attribute::"
					+ nameOfSourceID + "=" + " $joinPoint/attribute::" + nameOfTargetID + "]";
		}
		xqLocal.setExpression(expression);
		final List<Element> sourceComponents = XQEvaluator.evaluateToElementNodes(xqLocal);
		final NodeList targetComponents = xPathEvaluator.evaluateXPath(joinPointXPath, document);
		operationInterpreter.interpreteFillComponentByID(nameOfSourceID, nameOfTargetID,
				targetComponents, sourceComponents);
	}

	/**
	 * Interprets a {@link ReduceContent}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link ReduceContent} that is to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteReduceContent(final ReduceContent advice, final NodeList joinPoints)
			throws XMLWeaverException {
		operationInterpreter.interpreteReduceContent(joinPoints, (advice).getDeletePart());
	}

	/**
	 * Interprets a {@link ChooseVariant}-advice.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteChooseVariant(final NodeList joinPoints) throws XMLWeaverException {
		operationInterpreter.interpreteChooseVariant(joinPoints);
	}

	/**
	 * Interprets a {@link MoveElement}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link MoveElement} that is to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteMoveElement(final MoveElement advice, final NodeList joinPoints)
			throws XMLWeaverException {
		if (joinPoints.getLength() > 1) {
			throw new XMLWeaverException("Can't move more than one element at once");
		}
		final String target = (advice).getTo().getValue();
		final NodeList targetPoints = xPathEvaluator.evaluateXPath(target, document);
		if (targetPoints.getLength() != 1) {
			throw new XMLWeaverException("Target has to be exactly one element");
		}
		final Node source = joinPoints.item(0);
		final Node targetNode = targetPoints.item(0);
		final int position = (advice).getPosition().intValue();
		operationInterpreter.interpreteMoveElement(source, targetNode, position);
	}

	/**
	 * Interprets a {@link ChangeOrder}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link ChangeOrder} that is to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteChangeOrder(final ChangeOrder advice, final NodeList joinPoints)
			throws XMLWeaverException {
		if ((advice).getPermutation() != null) {
			operationInterpreter.interpretChangeOrder(joinPoints, (advice).getPermutation());
		} else {
			getLogger().warn("Warning: Permutation is empty");
		}
	}

	/**
	 * Interprets a {@link Delete}-advice.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteDelete(final NodeList joinPoints) throws XMLWeaverException {
		operationInterpreter.interpreteDelete(joinPoints);
	}

	/**
	 * Interprets an {@link EnrichContent}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link EnrichContent} that is to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param evaluatedTemplate
	 *            - a {@link NodeList} that contains an evaluated
	 *            {@link TextTemplate} for each joinPoint
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteEnrichContent(final EnrichContent advice, final NodeList joinPoints,
			final ArrayList<Text> evaluatedTemplate) throws XMLWeaverException {
		int position;
		if ((advice).getPosition() != null) {
			position = (advice).getPosition().intValue();
		} else {
			position = -1;
		}
		operationInterpreter.interpreteEnrichContent(joinPoints, position, evaluatedTemplate);
	}

	/**
	 * Interprets an {@link CollapseElement}-advice.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param evaluatedTemplate
	 *            - a {@link NodeList} that contains an evaluated
	 *            {@link TextTemplate} for each joinPoint
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteCollapseElement(final NodeList joinPoints,
			final ArrayList<Text> evaluatedTemplate) throws XMLWeaverException {
		operationInterpreter.interpreteCollapseElement(joinPoints, evaluatedTemplate);
	}

	/**
	 * Interprets an {@link ChangeValue}-advice.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param evaluatedTemplate
	 *            - a {@link NodeList} that contains an evaluated
	 *            {@link TextTemplate} for each joinPoint
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteChangeValue(final NodeList joinPoints,
			final ArrayList<Text> evaluatedTemplate) throws XMLWeaverException {
		operationInterpreter.interpreteChangeValue(joinPoints, evaluatedTemplate);
	}

	/**
	 * Interprets an {@link ExpandElement}-advice.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param evaluatedTemplate
	 *            - a {@link NodeList} that contains an evaluated
	 *            {@link ElementTemplate} for each joinPoint
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteExpandElement(final NodeList joinPoints,
			final ArrayList<Element> evaluatedTemplate) throws XMLWeaverException {
		operationInterpreter.interpreteExpandElement(joinPoints, evaluatedTemplate);
	}

	/**
	 * Interprets an {@link ExpandElement}-advice.
	 * 
	 * @param advice
	 *            - the instance of {@link InsertElement} that is supposed to be
	 *            interpreted
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the selected joinPoints for
	 *            this advice.
	 * @param evaluatedTemplate
	 *            - a {@link NodeList} that contains an evaluated
	 *            {@link ElementTemplate} for each joinPoint
	 * @throws XMLWeaverException
	 *             if any error occurred during weaving.
	 */
	private void interpreteInsertElement(final InsertElement advice, final NodeList joinPoints,
			final ArrayList<Element> evaluatedTemplate) throws XMLWeaverException {
		operationInterpreter.interpreteInsertElement(joinPoints, evaluatedTemplate, (advice)
				.getPosition().intValue());
	}

	/**
	 * Checks if an {@link ArrayList} is not empty. If the list doesn't contain
	 * any elements, the method throws a {@link XMLWeaverException}
	 * 
	 * @param <E>
	 * 
	 * @param template
	 *            - {@link ArrayList} that is to be checked
	 * @throws XMLWeaverException
	 *             - if the {@link ArrayList} is empty.
	 */

	public static <E> void checkTemplateSize(final ArrayList<E> template) throws XMLWeaverException {
		if (template == null || template.size() == 0) {
			throw new XMLWeaverException("Template evaluation did not result in any nodes");
		}
	}
}
