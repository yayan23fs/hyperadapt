package net.hyperadapt.pxweave.interactions.runtime;


import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.hyperadapt.pxweave.IPXWeaveNamespaceContext;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.IProgrammaticJoinpoint;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.aspects.ast.XPath;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.interactions.patternmatrix.ConflictMatrix;
import net.hyperadapt.pxweave.interpreter.AdviceContext;
import net.hyperadapt.pxweave.logger.Logable;

/**
 * class for the dynamical analysis to find interference and affectRelation conflicts
 * 
 * @author dkadner
 */
public class DynamicAnalysis extends Logable  {

	private List<AdviceToCheck> advicesWithJP;
//	private List<AffectedAdvicePair> affectRelation;
	private IProgrammaticJoinpoint ppc;
	private IPXWeaveNamespaceContext nsContext = null;
	private XPathEvaluator xPathEvaluator;

	private AdviceContext adviceContext = null;
	private AdviceLocator adviceLocator = null;

	private boolean logged = false;

	private ConflictMatrix conflictMatrix = null;

	public DynamicAnalysis(ConflictMatrix conflictMatrix) {
		advicesWithJP = new ArrayList<AdviceToCheck>();
		nsContext = null;
		adviceContext = null;
		this.conflictMatrix = conflictMatrix;
		getLogger().debug("Initialized dynamic analysis component ...");
	}



	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	public void setPPC(IProgrammaticJoinpoint ppc, AdviceLocator al) {
		// setze alles auf neu
		this.ppc = ppc;
		this.adviceLocator = al;
		if (logged) {
			advicesWithJP = new ArrayList<AdviceToCheck>();
			nsContext = null;
			adviceContext = null;
		}
		getLogger().debug("Current PPC: (" + this.adviceLocator.toString() + ") " + this.ppc.getIdentifier());
		// System.out.println("current PPC: " + this.ppc.toString());
	}

	public List<AffectedAdvicePair> computeAffectRelation(){
		xPathEvaluator = new XPathEvaluator(nsContext);
		List<AffectedAdvicePair> affectRelation = new ArrayList<AffectedAdvicePair>();

		for (int i = 0; i < advicesWithJP.size() - 1; i++) {
			AdviceToCheck first = advicesWithJP.get(i);
			// NodeList nlFirst = (NodeList) first[1];
			for (int j = i + 1; j < advicesWithJP.size(); j++) {
				AdviceToCheck second = advicesWithJP.get(j);

				// wenn in Konfliktamtrix, dann untersuchen ob wirklich
				// Konflikt
				if (conflictMatrix.isConflictBetween(first.getBasicAdvice().getClass().getSimpleName(),
						second.getBasicAdvice().getClass().getSimpleName())) {

					// NodeList nlSecond = (NodeList) second[1];
					// generiere schon mal vorsorglich eine Message
					String message = "-- Advice "
							+ first.getBasicAdvice().getClass().getSimpleName()
							+ " (Pointcut: " + first.getBasicAdvice().getPointcut().getValue()
							+ ")" + " INFLUENCE Advice "
							+ second.getBasicAdvice().getClass().getSimpleName()
							+ " (Pointcut: " + second.getBasicAdvice().getPointcut().getValue()
							+ ")";

					// Überprüfe ob Gemeinsamkeiten bei den beiden Nodelists
					// (welche Nodes sind betroffen beim Einweben des
					// Advices in das Dokument) existieren die beide ATCs
					// besitzen
					List<Node> indirectNodes;
							indirectNodes = catchIndirectConflict(first.getNodesBefore(),
								second.getNodesBefore());
					if (indirectNodes.size() != 0) {
						// wenn Konflikte gefunden wurden, dann loggen
						getLogger().debug(message);
						getLogger().debug("    @ Joinpoint(s): ");
						for (Node n : indirectNodes) {
							getLogger().debug("   " + n.getNodeName());
						}
						getLogger().debug("----");
					}
				}

				// ab hier Überprüfung auf beeinflussendes Verhalten. Siehe
				// Formel 3.1. @ Diplomarbeit
				// 1. Bedingung: cpc(a,doc)!=ø
				if (first.getNodesBefore().getLength() != 0) {
					// 2. Bedingung: cpc(b,doc)!=cpc(b,a*doc)
					final String subpath = second.getBasicAdvice().getPointcut().getValue();
				
					///Just to handle xpath expressions if no scope is present
					List<String> fullXPathList = new LinkedList<String>();
					if(adviceContext.getScope().getXpath()!=null&&!adviceContext.getScope().getXpath().isEmpty()){
						for (final XPath point : adviceContext.getScope().getXpath()) {
							fullXPathList.add(point.getValue() + "/" + subpath);
						}
					}
					else{
						fullXPathList.add(subpath);
					}
					
					for (final String fullXPath : fullXPathList) {
						// anwenden des XPath des 2. Advices auf das before
						// Document des 1. Advices
						try{
							NodeList cpcBOnDoc = xPathEvaluator.evaluateXPath(fullXPath,
									first.getBeforeDocument());
							// anwenden des XPath des 2. Advices auf das after
							// Document des 1. Advices
							NodeList cpcBOnDocWithA = xPathEvaluator.evaluateXPath(fullXPath,
									first.getAfterDocument());
							final NodeList joinPoints;
							joinPoints = xPathEvaluator.evaluateXPath(fullXPath, first.getBeforeDocument());

							second.setNodesBeforeOtherAdvice(joinPoints);
							
							if (!sameNodeList(cpcBOnDoc, cpcBOnDocWithA)) {
								AffectedAdvicePair pair = new AffectedAdvicePair(first.getCopy(), second.getCopy());
								affectRelation.add(pair);
							}
					
						}catch(XMLWeaverException e){
							getLogger().warn("Exception during critical pair comparison occured.",e);
						}
						
		

					}

				}

			}

		}

		return affectRelation;
	}
	
	/**
	 * method to start finding direct and affectRelation conflicts
	 */
	public void logErrors() {
		List<AffectedAdvicePair> affectRelation = computeAffectRelation();
		
		if (affectRelation.size() != 0) {
			for (AffectedAdvicePair pair : affectRelation) {
				checkIfTriggersOrInhibits(pair);
			}
			logAffected(affectRelation);
		}
	}

	/**
	 * method to decide whether the founded affectRelation advice error has an inhibit or triggering behavior
	 *  
	 * @param affectedPair the advice error to decide for, whether the affectRelation behavior is a triggering or inhibition
	 */
	private void checkIfTriggersOrInhibits(AffectedAdvicePair affectedPair) {
		NodeList justDoc = affectedPair.getB().getNodesBeforeOtherAdvice();
		NodeList docWithA = affectedPair.getB().getNodesBefore();

		List<Node> triggersNodes = new ArrayList<Node>();
		List<Node> inhibitsNodes = new ArrayList<Node>();

		// Unterscheide gefunde affectRelation nach auslösender und hemmender Wirkung:
		// justDoc == cpc(b, doc)
		// docWithA == cpc(b, a*doc)
		// inhibit: n € cpc(b, doc) && n !€ cpc(b, a*doc)
		for (int i = 0; i < justDoc.getLength(); i++) {
			Node nodeWithoutA = justDoc.item(i);
			boolean found = false;
			for (int j = 0; j < docWithA.getLength(); j++) {
				Node nodeWithA = docWithA.item(j);
				if (sameNodesWithoutDeep(nodeWithoutA, nodeWithA))
					found = true;
			}
			if (!found)
				inhibitsNodes.add(nodeWithoutA);
		}
		// triggers: n !€ cpc(b, doc) && n € cpc(b, a*doc)
		for (int i = 0; i < docWithA.getLength(); i++) {
			Node nodeWithA = docWithA.item(i);
			boolean found = false;
			for (int j = 0; j < justDoc.getLength(); j++) {
				Node nodeWithoutA = justDoc.item(j);
				if (sameNodesWithoutDeep(nodeWithA, nodeWithoutA))
					found = true;
			}
			if (!found)
				triggersNodes.add(nodeWithA);
		}

		affectedPair.setDependent("");
		if (triggersNodes.size() == 0) {
			if (inhibitsNodes.size() == 0) {
				// do nothing
			} else {
				affectedPair.setDependent(AffectedAdvicePair.INHIBITS);
				affectedPair.setDifferentNodes(inhibitsNodes);
			}
		} else {
			if (inhibitsNodes.size() == 0) {
				affectedPair.setDependent(AffectedAdvicePair.TRIGGERS);
				affectedPair.setDifferentNodes(triggersNodes);
			} else {
				//do nothing
				//System.out.println("inhibts und triggers gefunden, aber das sollte nicht passieren");
			}
		}
	}

	/**
	 * method to log all affectRelation advice errors
	 */
	private void logAffected(List<AffectedAdvicePair> affectRelation) {
		if (affectRelation.size() >= 1)
			getLogger().warn("=> Found trigger/inhibit Events ---");
		for (AffectedAdvicePair pair : affectRelation) {
			String message = "irgendwas lief beim Erkennen ob \"triggers oder inhibits\" schief";
			if (pair.isDependent().equals(AffectedAdvicePair.TRIGGERS)) {
				message = "Advice " + pair.getA().getBasicAdvice().getClass().getSimpleName()
						+ " (Pointcut: " + pair.getA().getBasicAdvice().getPointcut().getValue()
						+ ", File: " + adviceContext.getClass().getSimpleName() + ")" + " TRIGGERS Advice "
						+ pair.getB().getBasicAdvice().getClass().getSimpleName() + "(Pointcut: "
						+ pair.getB().getBasicAdvice().getPointcut().getValue() + ")";
			}

			if (pair.isDependent().equals(AffectedAdvicePair.INHIBITS)) {
				message = "Advice " + pair.getA().getBasicAdvice().getClass().getSimpleName()
						+ " (Pointcut: " + pair.getA().getBasicAdvice().getPointcut().getValue()
						+ ", File: " + adviceContext.getClass().getSimpleName() + ")" + " INHIBITS Advice "
						+ pair.getB().getBasicAdvice().getClass().getSimpleName() + "(Pointcut: "
						+ pair.getB().getBasicAdvice().getPointcut().getValue() + ")";
			}
			getLogger().warn(message);
		}
	}

	private boolean sameNodeList(NodeList nl1, NodeList nl2) {

		if (nl1.getLength() == 0) {
			if (nl2.getLength() == 0)
				return true;
			else
				return false;
		} else {
			if (nl2.getLength() == 0)
				return false;
		}

		// if (nl1.getLength() == 0 || nl2.getLength() == 0) return false;

		for (int i = 0; i < nl1.getLength(); i++) {
			boolean found = false;
			for (int j = 0; j < nl2.getLength(); j++) {
				if (sameNodesWithoutDeep(nl1.item(i), nl2.item(j))) {
					found = true;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	private boolean sameNodesWithoutDeep(Node n1, Node n2) {
		if (n1.cloneNode(false).isEqualNode(n2.cloneNode(false)))
			return true;
		return false;
	}

	public static boolean isSameCPC(NodeList nl_one, NodeList nl_two) {
		if (nl_one.getLength() > 0 && nl_two.getLength() > 0) {
			for (int i = 0; i < nl_one.getLength(); i++) {
				Node n_one = nl_one.item(i);
				for (int j = 0; j < nl_two.getLength(); j++) {
					Node n_two = nl_two.item(j);
					if (!n_one.isEqualNode(n_two))
						return false;
				}
			}
		}
		return true;
	}

	public List<Node> catchIndirectConflict(NodeList nl_one, NodeList nl_two) {
		List<Node> indirectNodeConflicts = new ArrayList<Node>();
		if (nl_one.getLength() > 0 && nl_two.getLength() > 0) {
			for (int i = 0; i < nl_one.getLength(); i++) {
				Node n_one = nl_one.item(i);
				for (int j = 0; j < nl_two.getLength(); j++) {
					NodeList nl_two_childs = nl_two.item(j).getChildNodes();
					if (isIn(n_one, nl_two_childs) != null)
						indirectNodeConflicts.add(n_one);
				}
			}
			for (int i = 0; i < nl_two.getLength(); i++) {
				Node n_two = nl_two.item(i);
				for (int j = 0; j < nl_one.getLength(); j++) {
					NodeList nl_one_childs = nl_one.item(j).getChildNodes();
					if (isIn(n_two, nl_one_childs) != null)
						indirectNodeConflicts.add(n_two);
				}
			}
		}
		return indirectNodeConflicts;
	}

	private Node isIn(Node n, NodeList nl) {
		Node t = null;

		for (int i = 0; i < nl.getLength(); i++) {
			Node a = nl.item(i);
			if (a.isEqualNode(n)) {
				t = n;
				return t;
			} else {
				NodeList newNL = nl.item(i).getChildNodes();
				t = isIn(n, newNL);
				if (t != null)
					return t;
			}
		}
		return t;

	}

	public AdviceToCheck getLastAdvice() {
		if (advicesWithJP.size() == 0)
			return null;
		else
			return advicesWithJP.get(advicesWithJP.size() - 1);
	}

	public void addDocumentAsAfterForLastAdvice(Document afterDocument) {
		if (advicesWithJP.size() > 0) {
			getLastAdvice().setAfterDocument(afterDocument);
		}
	}

	public void addAdvice(AdviceToCheck advicetocheck) {
		if (advicetocheck != null)
			advicesWithJP.add(advicetocheck);
	}

	public void setNamespaceContext(IPXWeaveNamespaceContext nsc) {
		this.nsContext = nsc;
	}

	public void setAdviceContext(AdviceContext ac) {
		this.adviceContext = ac;
	}

	/**
	 * Print the type and value of the evaluation result.
	 */
	public static void printResult(Object result) throws Exception {
		if (result instanceof Double) {
			System.out.println("Result type: double");
			System.out.println("Value: " + result);
		} else if (result instanceof Boolean) {
			System.out.println("Result type: boolean");
			System.out.println("Value: " + ((Boolean) result).booleanValue());
		} else if (result instanceof String) {
			System.out.println("Result type: String");
			System.out.println("Value: " + result);
		} else if (result instanceof Node) {
			Node node = (Node) result;
			System.out.println("Result type: Node");
			System.out.println("<output>");
			printNode(node);
			System.out.println("</output>");
		} else if (result instanceof NodeList) {
			NodeList nodelist = (NodeList) result;
			System.out.println("Result type: NodeList");
			System.out.println("<output>");
			printNodeList(nodelist);
			System.out.println("</output>");
		} else {
			System.out.println("An error occured");
		}
	}

	/** Decide if the node is text, and so must be handled specially */
	public static boolean isTextNode(Node n) {
		if (n == null)
			return false;
		short nodeType = n.getNodeType();
		return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE;
	}

	public static void printNode(Node node) throws Exception {
		if (isTextNode(node)) {
			System.out.println(node.getNodeValue());
		} else {
			// Set up an identity transformer to use as serializer.
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(
					System.out)));
		}

	}

	public static void printNodeList(NodeList nodelist) throws Exception {
		Node n;

		// Set up an identity transformer to use as serializer.
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		for (int i = 0; i < nodelist.getLength(); i++) {
			n = nodelist.item(i);
			if (isTextNode(n)) {
				// DOM may have more than one node corresponding to a
				// single XPath text node. Coalesce all contiguous text nodes
				// at this level
				StringBuffer sb = new StringBuffer(n.getNodeValue());
				for (Node nn = n.getNextSibling(); isTextNode(nn); nn = nn.getNextSibling()) {
					sb.append(nn.getNodeValue());
				}
				System.out.print(sb);
			} else {
				serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(
						System.out)));
			}
			System.out.println();
		}
	}

	public void printXML(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			System.out.println(writer.toString());
		} catch (TransformerException ex) {
			ex.printStackTrace();
		}
	}
	
	public ConflictMatrix getConflictMatrix(){
		return conflictMatrix;
	}
}