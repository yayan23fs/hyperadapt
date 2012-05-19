package net.hyperadapt.pxweave.interactions.algorithm.testmains;

import java.util.List;

import net.hyperadapt.pxweave.interactions.algorithm.testmains.ContainmentPreworker;
import net.hyperadapt.pxweave.interactions.devtime.triggersinhibits.TriggersAndInhibition;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;


public class TriggerInhibitTest {
	
	private static String configfile = "ConfigChangeOrder.xml";
	
	public static void main(String[] args){
		ContainmentPreworker cp = new ContainmentPreworker(configfile);
		List<AdviceToCheck> allAdvices = cp.getAllAdvices();
		List<AdviceError> errors = cp.getErrors();
		
		TriggersAndInhibition tai = new TriggersAndInhibition(allAdvices, errors);
		
		List<AdviceError> dependentErrors = tai.getAllAfterEffects();
		
		System.out.println(dependentErrors);
		
		
	}

}
