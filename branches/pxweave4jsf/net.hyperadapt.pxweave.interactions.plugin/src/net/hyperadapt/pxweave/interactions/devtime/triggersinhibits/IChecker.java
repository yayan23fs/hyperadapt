package net.hyperadapt.pxweave.interactions.devtime.triggersinhibits;


import java.util.List;

import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;

/**
 * Interface for Checker classes
 * 
 * @author danielkadner
 *
 */
public interface IChecker {
	
	public List<AdviceError> getErrors();

}
