package net.hyperadapt.pxweave.interactions.devtime.triggersinhibits;

import java.util.ArrayList;
import java.util.List;

import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

//import interaction.algorithm.testmains.TriggersInhibitsTest;


/**
 * class to check for triggering and inhibit effects for all advices
 * 
 * @author danielkadner
 *
 */
public class TriggersAndInhibition {
	
	private List<AdviceToCheck> allAdvices;
	private List<AdviceError> errors;
	
	private boolean debug = DebugMode.debug;
//	private boolean debug = true;
	
	public TriggersAndInhibition(List<AdviceToCheck> allAdvices, List<AdviceError> errors){
		this.allAdvices = allAdvices;
		this.errors = errors;
	}
	
	/**
	 * method to find all triggering and inhibits effect advices
	 * @return a list with all advice combinations with triggering or inhibition effects 
	 */
	public List<AdviceError> getAllAfterEffects(){
		
		if (debug) { 
			System.out.println();
			System.out.println(">>>> Find trigger and inhibits");
			System.out.println();
		}
		List<AdviceError> triggersAndInhibits = new ArrayList<AdviceError>();
		
		IChecker tc = new TriggeringChecker(allAdvices, errors);
		List<AdviceError> triggeringErrors = tc.getErrors();
		if (triggeringErrors != null) triggersAndInhibits.addAll(triggeringErrors);
		
		tc = new InhibitionChecker(allAdvices, errors);
		List<AdviceError> inhibitionErrors = tc.getErrors();
		if (inhibitionErrors != null) triggersAndInhibits.addAll(inhibitionErrors);
		
		if (triggersAndInhibits.size() == 0) return null;
		return triggersAndInhibits;
		
	}
	
	public static void main(String[] args){
//		TriggersInhibitsTest.main(args);
	}

}
