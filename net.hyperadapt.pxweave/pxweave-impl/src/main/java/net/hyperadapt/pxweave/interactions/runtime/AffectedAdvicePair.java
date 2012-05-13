package net.hyperadapt.pxweave.interactions.runtime;

import java.util.List;

import org.w3c.dom.Node;

/**
 * class representing an advice error. this is either an direct or an affected
 * conflict between to AdviceToChecks (definiert eine Konfliktkombination mit
 * den zutreffenden PPCs)
 * 
 * @author dkadner
 */
public class AffectedAdvicePair {

	public static final String TRIGGERS = "triggers";
	public static final String INHIBITS = "inhibits";

	private IAdviceToCheck a;
	private IAdviceToCheck b;
	// private List<ProgrammaticScope> ppcs;
	// wenn triggers: a fügt diese joinpoints ins dokument hinzu
	// wenn inhibits: a löscht diese joinpoints aus dem dokument
	private List<Node> differentNodes;

	// wenn laut Konfliktmatrix "warning" dann false
	// wenn error dann true
	private boolean triggers = false;
	private boolean inhibits = false;

	private String solution;
	private String reason;

	public AffectedAdvicePair(IAdviceToCheck a, IAdviceToCheck b) {
		this.a = a;
		this.b = b;
	}

	public IAdviceToCheck getA() {
		return a;
	}

	public void setA(IAdviceToCheck a) {
		this.a = a;
	}

	public IAdviceToCheck getB() {
		return b;
	}

	public void setB(IAdviceToCheck b) {
		this.b = b;
	}

	// public List<ProgrammaticScope> getCommonPPCs() {
	// return ppcs;
	// }
	//
	// public void setCommonPPCs(List<ProgrammaticScope> ppcs) {
	// this.ppcs = ppcs;
	// }

	// public boolean getIsError(){
	// return isError;
	// }
	//
	// public void setIsError(boolean isError){
	// this.isError = isError;
	// }

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String isDependent() {
		if (inhibits)
			return INHIBITS;
		if (triggers)
			return TRIGGERS;
		return null;
	}

	public void setDependent(String dependention) {
		if (dependention.equals(INHIBITS)) {
			inhibits = true;
			triggers = false;
		} else if (dependention.equals(TRIGGERS)) {
			inhibits = false;
			triggers = true;
		} else {
			inhibits = false;
			triggers = false;
		}
	}

	public List<Node> getDifferentNodes() {
		return differentNodes;
	}

	public void setDifferentNodes(List<Node> differentNodes) {
		this.differentNodes = differentNodes;
	}

}
