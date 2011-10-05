package net.hyperadapt.pxweave.interactions.devtime.triggersinhibits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.hyperadapt.pxweave.aspects.ast.ChangeValue;
import net.hyperadapt.pxweave.aspects.ast.CollapseElement;
import net.hyperadapt.pxweave.aspects.ast.Delete;
import net.hyperadapt.pxweave.aspects.ast.MoveElement;
import net.hyperadapt.pxweave.aspects.ast.TextTemplate;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TestContainment;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.Automaton;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.AutomatonPreworker;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.ProductAutomaton;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;


/**
 * class to check for inhibition effect
 * 
 * @author danielkadner
 * 
 */
public class InhibitionChecker implements IChecker {

	private List<AdviceToCheck> allAdvices;
	private List<AdviceError> errors;
	private List<AdviceToCheck> inhibitionAdvices;
	private List<AdviceError> inhibitionErrors;
	private boolean debug = true;

	public InhibitionChecker(List<AdviceToCheck> allAdvices, List<AdviceError> errors) {
		if (debug) {
			System.out.println(">>>>>>>>Inhibition");
			System.out.println();
		}
		this.allAdvices = allAdvices;
		this.errors = errors;
		if (createInhibitionList())
			check();
		if (debug) {
			System.out.println();
		}
	}

	/**
	 * filter all posible inhibition advices
	 */
	private boolean createInhibitionList() {
		inhibitionAdvices = new ArrayList<AdviceToCheck>();

		for (int i = 0; i < allAdvices.size(); i++) {
			AdviceToCheck atc = allAdvices.get(i);
			if (atc.getName().equalsIgnoreCase("ChangeValue")
					|| atc.getName().equalsIgnoreCase("CollapseElement")
					|| atc.getName().equalsIgnoreCase("DeleteElement")
					|| atc.getName().equalsIgnoreCase("Delete")
					|| atc.getName().equalsIgnoreCase("MoveElement"))
				inhibitionAdvices.add(atc);
		}
		if (debug) {
			if (inhibitionAdvices.size() > 0) {
				System.out.print("found: ");
				String f = "";
				for (AdviceToCheck atc : inhibitionAdvices) {
					f += atc.getName() + ", ";
				}
				if (f.length() > 0) {
					f = f.substring(0, f.length() - 2);
					System.out.print(f);
				}
				System.out.println();
			} else {
				System.out.println("no possible inhibitions found");
			}

		}

		return (inhibitionAdvices.size() > 0) ? true : false;
	}

	private void check() {
		if (debug) {
			System.out.println("check on inhibition errors");
		}
		inhibitionErrors = new ArrayList<AdviceError>();
		for (int i = 0; i < inhibitionAdvices.size(); i++) {
			AdviceToCheck atc = inhibitionAdvices.get(i);
			if (atc.getAdviceType() instanceof ChangeValue)
				inhibitionErrors.addAll(checkChangeValue(atc));
			if (atc.getAdviceType() instanceof CollapseElement)
				inhibitionErrors.addAll(checkCollapseElement(atc));
			if (atc.getAdviceType() instanceof Delete)
				inhibitionErrors.addAll(checkDeleteElement(atc));
			if (atc.getAdviceType() instanceof MoveElement)
				inhibitionErrors.addAll(checkMoveElement(atc));
		}
	}

	// suche ob vorher schon eine Wechselwirkung da war mit attribut = oldValue
	private List<AdviceError> checkChangeValue(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on ChangeValue errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		String valueChanged = ((TextTemplate) atc.getExtraContentIfExisting()).getValue();
		String linearizedXPath = linearize(atc.getXPath());
		String lastPartOfLinearizedXPath = linearizedXPath.substring(
				linearizedXPath.lastIndexOf("/") + 1, linearizedXPath.length());

		if (lastPartOfLinearizedXPath.contains("@id")
				|| lastPartOfLinearizedXPath.contains("attribute::id")) {
			// wenn eine id geändert werden soll
			for (AdviceToCheck aa : allAdvices) {
				if (!equalsAdvice(atc, aa)) {
					if (aa.getXPath().contains("@id=" + valueChanged)
							|| aa.getXPath().contains("attribute::id=" + valueChanged)) {
						// System.out.println("einen gefunden mit gleicher id:"+valueChanged);
						AdviceError ae_new = new AdviceError(atc, aa);
						ae_new.setDependent(AdviceError.INHIBITS);
						ae_new.setMessage(" Used the old/changed id!");
						checked.add(ae_new);
					}
				}
			}
		} else {
			// wenn value $(variable) enthält, dann einfach so zurück geben mit
			// second = null
			String value = ((TextTemplate) atc.getExtraContentIfExisting()).getValue();

			String var = checkForVariable(value);
			if (!var.equals("")) {
				AdviceError ae = new AdviceError(atc, null);
				ae.setDependent(AdviceError.INHIBITS);
				ae.setMessage("Possible inhibit effect. Can not be intercepted, because value contains variable: "
						+ var);
				checked.add(ae);
				return checked;
			}

			// erzeuge XPath mit neuem Value

			// System.out.println(linearizedXPath);
			// System.out.println(lastPartOfLinearizedXPath);
			// System.out.println();
			String secondLastOfLinearizedXPath = linearizedXPath.substring(0,
					linearizedXPath.lastIndexOf(lastPartOfLinearizedXPath) - 1);
			secondLastOfLinearizedXPath = secondLastOfLinearizedXPath.substring(
					secondLastOfLinearizedXPath.lastIndexOf("/") + 1,
					secondLastOfLinearizedXPath.length());
			// System.out.println("SECONDLAST "+secondLastOfLinearizedXPath);

			String attr = null;
			String attrWO = null;
			if (lastPartOfLinearizedXPath.matches("(attribute)::[a-z]*")
					|| lastPartOfLinearizedXPath.matches("(@)[a-z]*")) {
				// wenn das letzte Teilstück entweder attribute::?? oder @?? ist
				attr = secondLastOfLinearizedXPath + "\\[" + lastPartOfLinearizedXPath + "="
						+ valueChanged + "\\]";
				attrWO = secondLastOfLinearizedXPath + "\\[" + lastPartOfLinearizedXPath
						+ "=')(\\S)*('\\]";
			}
			if (lastPartOfLinearizedXPath.matches("[A-Za-z0-9]*:[A-Za-z]*")) {
				// wenn das letzte Teilstück entweder attribute::?? oder @?? ist
				attr = lastPartOfLinearizedXPath + "\\[./text()=" + valueChanged + "\\]";
				attrWO = lastPartOfLinearizedXPath + "\\[./text()=')(\\S)*('\\]";
			}

			// System.out.println("ATTR "+attr);
			// System.out.println("ATTRWO "+attrWO);

			/*
			 * 1. advice change value ändert einen value bei einem pc 2. advice
			 * greift auf den pc vom 1. advice zu mit den alten value
			 */

			// => checke ob schon unter den gefundenen adviceerrors schon ein
			// problem mit 1. advice existierte?
			// List<AdviceToCheck> possibleErrors = new
			// ArrayList<AdviceToCheck>();
			for (AdviceError aes : errors) {
				AdviceToCheck inhibitionATC = aes.getFirstAdvice();
				if (equalsAdvice(atc, inhibitionATC)) {
					AdviceError ae_new = new AdviceError(aes.getFirstAdvice(),
							aes.getSecondAdvice());
					ae_new.setDependent(AdviceError.INHIBITS);
					// possibleErrors.add(inhibitionATC);
					checked.add(ae_new);
				}

			}

			for (AdviceToCheck atcs : allAdvices) {

				String xp = atcs.getXPath();
				// System.out.println(xp);
				String matchesWithNewString = "(\\S)*(" + attr + ")(\\S)*";
				String matchesWithOutNewString = "(\\S)*(" + attrWO + ")(\\S)*";
				// System.out.println(matchesWithNewString);
				// System.out.println(matchesWithOutNewString);

				boolean matchesWithNew = xp.matches(matchesWithNewString);
				boolean matchesWithoutNew = xp.matches(matchesWithOutNewString);
				// System.out.println(matchesWithNew + "   "
				// +matchesWithoutNew);
				if (!matchesWithNew && matchesWithoutNew) {
					AdviceError ae = new AdviceError(atc, atcs);
					ae.setDependent(AdviceError.INHIBITS);
					ae.setMessage(" Found an XPath for the Element not based on the new value.");
					checked.add(ae);
				}
			}

			// attribut vorhanden? wenn ja nimm letztes
			// aber nur wenns nicht zwischen [ und ] steht
			// String attribute = "";
			// if (atc.getXPath().contains("attribute:")) {
			// if ()
			// attribute =
			// atc.getXPath().substring(atc.getXPath().lastIndexOf("attribute::")
			// +
			// 11,
			// atc.getXPath().length());
			// }
			// if (atc.getXPath().contains("@")) {
			// attribute =
			// atc.getXPath().substring(atc.getXPath().lastIndexOf("@")
			// + 1,
			// atc.getXPath().length());
			// }
			// String text = "";
			// if (atc.getXPath().contains(s))
			//
			// for (AdviceToCheck atcSecond : possibleErrors) {
			// // schauen ob dieses Attribut im zweiten Xpath vorkommt
			// if (atcSecond.getXPath().contains("attribute::" + attribute)
			// || atcSecond.getXPath().contains("@" + attribute)) {
			// AdviceError addit = new AdviceError(atc, atcSecond);
			// addit.setDependent(AdviceError.INHIBITS);
			// checked.add(addit);
			// }
			// }

		}

		return checked;
	}

	private List<AdviceError> checkCollapseElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on CollapseElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();

		// wenn value $(variable) enthält, dann einfach so zurück geben mit
		// second = null
		String value = ((TextTemplate) atc.getExtraContentIfExisting()).getValue();
		String var = checkForVariable(value);
		if (!var.equals("")) {
			AdviceError ae = new AdviceError(atc, null);
			ae.setDependent(AdviceError.INHIBITS);
			ae.setMessage("Possible inhibition effect. Can not be intercepted, because value contains variable: "
					+ var);
			checked.add(ae);
			return checked;
		}

		// wenn Advice auf Unterelemente des XPath zugreifen wollen
		String atcXP = atc.getXPath();

		for (AdviceToCheck atcSecond : allAdvices) {
			String atcSecondXP = atcSecond.getXPath();

			if (checkForInterference(atcXP, atcSecondXP)) {
				AdviceError ae = new AdviceError(atc, atcSecond);
				ae.setDependent(AdviceError.INHIBITS);
				checked.add(ae);
			}
		}
		return checked;
	}

	private List<AdviceError> checkDeleteElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on DeleteElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();
		// bei Zugriff auf Element
		for (AdviceError atc1 : errors) {
			if (equalsAdvice(atc, atc1.getFirstAdvice())) {
				AdviceError ae = new AdviceError(atc, atc1.getSecondAdvice());
				ae.setDependent(AdviceError.INHIBITS);
				checked.add(ae);
			}
		}
		return checked;
	}

	private List<AdviceError> checkMoveElement(AdviceToCheck atc) {
		if (debug) {
			System.out.println("\tcheck on MoveElement errors");
		}
		List<AdviceError> checked = new ArrayList<AdviceError>();
		// wenn Analyse vorher Fehler gezeigt hat und jetzt wird zu neuer
		// Position verschoben
		for (AdviceError atc1 : errors) {
			if (equalsAdvice(atc, atc1.getFirstAdvice())) {
				AdviceError ae = new AdviceError(atc, atc1.getSecondAdvice());
				ae.setDependent(AdviceError.INHIBITS);
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
		if (inhibitionErrors == null || inhibitionErrors.size() == 0)
			return null;
		return inhibitionErrors;
	}

	private static String linearize(String in) {
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
		return variable.substring(0, variable.length() - 2);
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