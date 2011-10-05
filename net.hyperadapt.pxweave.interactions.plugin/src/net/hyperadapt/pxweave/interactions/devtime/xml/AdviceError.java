package net.hyperadapt.pxweave.interactions.devtime.xml;

import java.util.List;
import net.hyperadapt.pxweave.aspects.ast.ProgrammaticScope;

/**
 * class to represent a conflictcombination for a ppc
 * 
 * @author danielkadner
 *
 */
public class AdviceError {
	
	public static final String TRIGGERS = "triggers";
	public static final String INHIBITS = "inhibits";
	
	private AdviceToCheck first;
	private AdviceToCheck second;
	private List<ProgrammaticScope> ppcs;
	
	private String message = "";
	
	//wenn laut Konfliktmatrix "warning" dann false
	//wenn error dann true
	private boolean isError;
	private boolean triggers = false;
	private boolean inhibits = false;
	
	private String solution;
	private String reason;
	
	public AdviceError(AdviceToCheck first, AdviceToCheck second){
		this.first = first;
		this.second = second;
	}

	public AdviceToCheck getFirstAdvice() {
		return first;
	}

	public void setFirstAdvice(AdviceToCheck first) {
		this.first = first;
	}

	public AdviceToCheck getSecondAdvice() {
		return second;
	}

	public void setSecondAdvice(AdviceToCheck second) {
		this.second = second;
	}

	public List<ProgrammaticScope> getCommonPPCs() {
		return ppcs;
	}

	public void setCommonPPCs(List<ProgrammaticScope> ppcs) {
		this.ppcs = ppcs;
	}
	
	public boolean getIsError(){
		return isError;
	}
	
	public void setIsError(boolean isError){
		this.isError = isError;
	}

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
		if (inhibits) return INHIBITS;
		if (triggers) return TRIGGERS;
		return null;
	}

	/**
	 * method to set whether this error got a triggering or inhibition behavior 
	 * @param dependention
	 */
	public void setDependent(String dependention) {
		if (dependention.equals(INHIBITS)) {
			inhibits = true;
			triggers = false;
		} else if (dependention.equals(TRIGGERS)){
			inhibits = false;
			triggers = true;
		}
		else {
			inhibits = false;
			triggers = false;
		}
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
