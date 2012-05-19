package net.hyperadapt.pxweave.interactions.devtime.triggersinhibits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import net.hyperadapt.pxweave.aspects.ast.ChangeValue;
import net.hyperadapt.pxweave.aspects.ast.ChooseVariant;
import net.hyperadapt.pxweave.aspects.ast.ElementTemplate;
import net.hyperadapt.pxweave.aspects.ast.ExpandElement;
import net.hyperadapt.pxweave.aspects.ast.InsertElement;
import net.hyperadapt.pxweave.aspects.ast.MoveElement;
import net.hyperadapt.pxweave.aspects.ast.TextTemplate;
import net.hyperadapt.pxweave.aspects.ast.XPath;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TestContainment;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.Automaton;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.AutomatonPreworker;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.ProductAutomaton;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;


/**
 * class to check for triggering effects
 * 
 * @author danielkadner
 * 
 */
public class TriggeringChecker implements IChecker {

	private List<AdviceToCheck> allAdvices;
	@SuppressWarnings("unused")
	private List<AdviceError> errors;
	private List<AdviceToCheck> triggeringAdvices;
	private List<AdviceError> triggeringErrors;
	private boolean debug = true;

	public TriggeringChecker(List<AdviceToCheck> allAdvices, List<AdviceError> errors) {
		if (debug) {
			System.out.println(">>>>>>>>Triggering");
			System.out.println();
		}
		this.allAdvices = allAdvices;
		this.errors = errors;
		if (createTriggeringList())
			check();
		if (debug) {
			System.out.println();
		}

	}

	/**
	 * filter all possible triggering advices
	 */
	private boolean createTriggeringList() {
		triggeringAdvices = new ArrayList<AdviceToCheck>();

		for (int i = 0; i < allAdvices.size(); i++) {
			AdviceToCheck atc = allAdvices.get(i);
			if (atc.getName().equalsIgnoreCase("ChangeValue")
					|| atc.getName().equalsIgnoreCase("ChooseVariant")
					|| atc.getName().equalsIgnoreCase("ExpandElement")
					|| atc.getName().equalsIgnoreCase("InsertElement")
					|| atc.getName().equalsIgnoreCase("MoveElement"))
				triggeringAdvices.add(atc);
		}

		if (debug) {
			if (triggeringAdvices.size() > 0) {
				System.out.print("found: ");
				String f = "";
				for (AdviceToCheck atc : triggeringAdvices) {
					f += atc.getName() + ", ";
				}
				if (f.length() > 0) {
					f = f.substring(0, f.length() - 2);
					System.out.print(f);
				}
				System.out.println();
			} else {
				System.out.println("no triggering found");
			}

		}

		return (triggeringAdvices.size() > 0) ? true : false;

	}

	private void check() {
		if (debug) {
			System.out.println("check on triggering errors");
		}
		triggeringErrors = new ArrayList<AdviceError>();
		for (int i = 0; i < triggeringAdvices.size(); i++) {
			AdviceToCheck atc = triggeringAdvices.get(i);
			if (atc.getAdviceType() instanceof ChangeValue)
				triggeringErrors.addAll(checkChangeValue(atc));
			if (atc.getAdviceType() instanceof ChooseVariant)
				triggeringErrors.addAll(checkChooseVariant(atc));
			if (atc.getAdviceType() instanceof ExpandElement)
				triggeringErrors.addAll(checkExpandElement(atc));
			if (atc.getAdviceType() instanceof InsertElement)
				triggeringErrors.addAll(checkInsertElement(atc));
			if (atc.getAdviceType() instanceof MoveElement)
				triggeringErrors.addAll(checkMoveElement(atc));
		}
	}

	// suche durch alle advice, ob ein Behinderung mit neuem XPath vorliegt

	/*
	 * 1. advice change value ändert einen value bei einem pc 2. advice greift
	 * auf den neu geänderten pc vom 1. advice zu (mit den neuen value)
	 */
	private List<AdviceError> checkChangeValue(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on ChangeValue errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		// wenn value $(variable) enthält, dann einfach so zurück geben mit
		// second = null
		String value = ((TextTemplate) atc.getExtraContentIfExisting()).getValue();
		String linearizedXPath = linearize(atc.getXPath());
		String lastPartOfLinearizedXPath = linearizedXPath.substring(
				linearizedXPath.lastIndexOf("/") + 1, linearizedXPath.length());
		// System.out.println("VVVV"+value);

		if (lastPartOfLinearizedXPath.contains("@id")
				|| lastPartOfLinearizedXPath.contains("attribute::id")) {
			//wenn eine id geändert werden soll
			for (AdviceToCheck aa : allAdvices) {
				if (!equalsAdvice(atc, aa)) {
					if (aa.getXPath().contains("@id="+value) || aa.getXPath().contains("attribute::id="+value)){
//						System.out.println("einen gefunden mit gleicher id:"+value);
						AdviceError ae_new = new AdviceError(atc, aa);
						ae_new.setDependent(AdviceError.TRIGGERS);
						ae_new.setMessage(" Uses the changed id!");
						checked.add(ae_new);
					}
				}
			}
		} else {

			String var = checkForVariable(value);
			if (!var.equals("")) {
				// System.out.println("J matched");
				AdviceError ae = new AdviceError(atc, null);
				ae.setDependent(AdviceError.TRIGGERS);
				ae.setMessage("Possible triggering effect. Can not be intercepted, because value contains variable: "+var);
				checked.add(ae);
				return checked;
			}

			// erzeuge XPath mit neuem Value
			// String valueChanged = ((TextTemplate)
			// atc.getExtraContentIfExisting()).getValue();

			String attr = null;
			if (lastPartOfLinearizedXPath.matches("(attribute)::[a-z]*")
					|| lastPartOfLinearizedXPath.matches("(@)[a-z]*")) {
				// wenn das letzte Teilstück entweder attribute::?? oder @?? ist
				attr = lastPartOfLinearizedXPath + "=" + value;
			} else {
				// ansonsten ist es ein Element und somit wird auf text()
				// zugegriffen
				// Wechselwirkung bei Funktionen nicht erkennbar mit bisherigen
				// Algorithmen
				AdviceError ae_new = new AdviceError(atc, null);
				ae_new.setDependent(AdviceError.TRIGGERS);
				ae_new.setMessage("Possible triggering effect. Can not be intercepted, because changed value is a text element (no Funktions allowed).");
				checked.add(ae_new);
				return checked;
			}

			// String p =
			// "/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source[./text()='dhdj']/@id[./text()='222']";
			// System.out.println("p "+p);
			// String linearizedXPath = TriggeringChecker.linearize(p);
			// String lastPartOfLinearizedXPath =
			// linearizedXPath.substring(linearizedXPath.lastIndexOf("/")+1,
			// linearizedXPath.length());

			// // String attr ="nichts";
			// if (lastPartOfLinearizedXPath.matches("(attribute)::[a-z]*") ||
			// lastPartOfLinearizedXPath.matches("(@)[a-z]*")){
			// // wenn das letzte Teilstück entweder attribute::?? oder @?? ist
			// attr = lastPartOfLinearizedXPath+"='HalloAttribute'";
			// }
			// System.out.println("letzte stück "+lastPartOfLinearizedXPath);
			// System.out.println(" wird ersetzt "+attr);

			String[] sa = atc.getXPath().split(lastPartOfLinearizedXPath);

			String newXPath = "";
			for (int i = 0; i < sa.length; i++) {
				newXPath += sa[i];
				newXPath += lastPartOfLinearizedXPath;
			}
			newXPath = newXPath.substring(0, newXPath.lastIndexOf(lastPartOfLinearizedXPath));
			newXPath += attr;
			// newXPath += sa[sa.length-1];

			// überprüfe ob der neue XPath mit irgend einen der anderen Advices
			// matched

			// List<AdviceToCheck> possibleErrors = new
			// ArrayList<AdviceToCheck>();

			for (AdviceToCheck aa : allAdvices) {
				if (!equalsAdvice(atc, aa)) {
					boolean[] result = TestContainment.testContainment(aa.getXPath(), newXPath);
					// System.out.println("Vergleiche:");
					// System.out.println(aa.getXPath());
					// System.out.println(newXPath);
					// System.out.println(result[0] + "    " + result[1]);
					if (result[0] == true || result[1] == true) {
						AdviceError ae_new = new AdviceError(atc, aa);
						ae_new.setDependent(AdviceError.TRIGGERS);
						checked.add(ae_new);
					}
				}
			}

			// for (int i = 0; i < allAdvices.size(); i++) {
			// AdviceToCheck atc1 = errors.get(i).getFirstAdvice();
			// if (equalsAdvice(atc, atc1))
			// possibleErrors.add(atc1);
			// }
			// String attribute = "";
			// if (atc.getXPath().contains("attribute:")) {
			// attribute =
			// atc.getXPath().substring(atc.getXPath().lastIndexOf("attribute::")
			// +
			// 11, atc.getXPath().length());
			// }
			// if (atc.getXPath().contains("@")) {
			// attribute =
			// atc.getXPath().substring(atc.getXPath().lastIndexOf("@")
			// + 1, atc.getXPath().length());
			// }

			// for (AdviceToCheck atc1 : possibleErrors) {
			// // schauen ob dieses Attribut im zweiten Xpath vorkommt
			// if (atc1.getXPath().contains("attribute::" + valueChanged) ||
			// atc1.getXPath().contains("@" + valueChanged) ) {
			// AdviceError addit = new AdviceError(atc, atc1);
			// addit.setDependent(AdviceError.TRIGGERS);
			// checked.add(addit);
			// }
			// }
		}
		return checked;
	}

	// erzeuge XPath aus der Basis, suche XPath mit gleicher Basis
	private List<AdviceError> checkChooseVariant(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on ChooseVariant errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		// erzeuge Xpath aus Basis
		// suche Xpath mit gleicher Basis
		String atcXP = atc.getXPath();

		for (AdviceToCheck atc1 : allAdvices) {
			if (!equalsAdvice(atc, atc1)) {

				String atc1XP = atc1.getXPath();

				if (checkForInterference(atcXP, atc1XP)) {
					AdviceError ae = new AdviceError(atc, atc1);
					ae.setDependent(AdviceError.TRIGGERS);
					checked.add(ae);
				}
			}
		}
		return checked;
	}

	// Wenn Advice auf Unterelemente des Elements zugreifen
	// Hole XPath des expandElement und füge ersten Knoten des elementTemplat an
	// wenn XPath des zweiten Elements Spezialisierung des ersten =>
	// Intersection
	private List<AdviceError> checkExpandElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on ExpandElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		String newXPath = atc.getXPath();
		ElementTemplate value = ((ElementTemplate) atc.getExtraContentIfExisting());

		List<Object> l = value.getContent();
		String nodeName = "";
		for (Object object : l) {
			if (object instanceof Node) {
				Node n = (Node) object;
				nodeName = n.getNodeName();
				// System.out.println("Knoten gefunden" + n);
//				System.out.println(n.getNodeName());
			}
		}
		if (!nodeName.equals(""))
			newXPath += "/" + nodeName;
//		System.out.println(newXPath);

		// Xpath des neuen Elements, sucher Verschneidung
		for (AdviceToCheck atc1 : allAdvices) {
			if (!equalsAdvice(atc, atc1)) {
				String atc1XP = atc1.getXPath();

				if (checkForInterference(newXPath, atc1XP)) {
					AdviceError ae = new AdviceError(atc, atc1);
					ae.setDependent(AdviceError.TRIGGERS);
					checked.add(ae);
				}
			}
		}
		return checked;
	}

	// erzeuge XPath des neuen Elements
	// checke Verschneidung mit allen anderen Advices
	private List<AdviceError> checkInsertElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on InsertElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		ElementTemplate value = ((ElementTemplate) atc.getExtraContentIfExisting());

		String newXPath = atc.getXPath();
		List<Object> l = value.getContent();
		String nodeName = "";
		for (Object object : l) {
			if (object instanceof Node) {
				Node n = (Node) object;
				nodeName = n.getNodeName();
				// System.out.println("Knoten gefunden" + n);
//				System.out.println(n.getNodeName());
			}
		}
		newXPath += "/" + nodeName;
//		System.out.println(newXPath);

		// Xpath des neuen Elements, sucher Verschneidung
		for (AdviceToCheck atc1 : allAdvices) {
			if (!equalsAdvice(atc, atc1)) {
				String atc1XP = atc1.getXPath();

				if (checkForInterference(newXPath, atc1XP)) {
					AdviceError ae = new AdviceError(atc, atc1);
					ae.setDependent(AdviceError.TRIGGERS);
					checked.add(ae);
				}
			}
		}
		return checked;
	}

	// erzeuge XPath aus <to> Element + Namen des Elementes
	// checke Verschneidung mit allen anderen Advices
	private List<AdviceError> checkMoveElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on MoveElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		// erzeuge xpath aus to element, checke Verschneidung
		// String valueChanged = ((TextTemplate)
		// atc.getExtraContentIfExisting()).getValue();
		String s = ((XPath) atc.getExtraContentIfExisting()).getValue();
		if (s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		String linearizedXPath = linearize(atc.getXPath());
		// if (linearizedXPath.endsWith("/")) linearizedXPath =
		// linearizedXPath.substring(0, linearizedXPath.length()-1);
		String lastPartOfLinearizedXPath = linearizedXPath.substring(
				linearizedXPath.lastIndexOf("/") + 1, linearizedXPath.length());

		// System.out.println("XPATH "+s);
		// System.out.println("LAST "+lastPartOfLinearizedXPath);
		String newXPath = s + "/" + lastPartOfLinearizedXPath;
		// System.out.println(newXPath);

		// Xpath des neuen Elements, sucher Verschneidung
		for (AdviceToCheck atc1 : allAdvices) {
			String atc1XP = atc1.getXPath();

			if (checkForInterference(newXPath, atc1XP)) {
				AdviceError ae = new AdviceError(atc, atc1);
				ae.setDependent(AdviceError.TRIGGERS);
				checked.add(ae);
			}
		}
		return checked;
	}

	private boolean equalsAdvice(AdviceToCheck atc1, AdviceToCheck atc2) {
		if (atc1.getFilename().equalsIgnoreCase(atc2.getFilename())
				&& atc1.getName().equalsIgnoreCase(atc2.getName())
				&& atc1.getFilename().equalsIgnoreCase(atc2.getFilename())
				&& atc1.getXPath().equalsIgnoreCase(atc2.getXPath())
				&& atc1.getAdviceType().equals(atc2.getAdviceType())
				&& atc1.getAdviceGroup().equals(atc2.getAdviceGroup())
				&& atc1.getAspect().equals(atc2.getAspect()))
			return true;
		return false;

	}

	public List<AdviceError> getErrors() {
		if (triggeringErrors == null || triggeringErrors.size() == 0)
			return null;
		return triggeringErrors;
	}

	public static String linearize(String in) {
		// in = in.replaceAll("/\\w*:", "/");
		return in.replaceAll("\\[.*?\\]", "");
	}
	
	private String checkForVariable(String value) {
		String variable = "";
		Pattern pattern = Pattern.compile("(\\$\\w+)");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			variable += matcher.group() + ", ";
		}

		if (variable.length() == 0) return "";
		return variable.substring(0, variable.length()-2);
	}
	
	private boolean checkForInterference(String firstXPath, String secondXPath) {
		boolean[] containmentResult;
		boolean interferenceResult;

		containmentResult = TestContainment.testContainment(firstXPath, secondXPath);

		AutomatonPreworker ap = new AutomatonPreworker();
		try {
			ap.createAutomatons(firstXPath, secondXPath);
		} catch (ParseQueryException e) {
			e.printStackTrace();
		} catch (ParseFunctionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Automaton aP = ap.getAutomatonP();
		Automaton aQ = ap.getAutomatonQ();
		ProductAutomaton pa = new ProductAutomaton(aP, aQ);
		interferenceResult = pa.isIntersection();

		if (containmentResult[0] == true || containmentResult[1] == true || interferenceResult)
			return true;
		else
			return false;
	}
}
