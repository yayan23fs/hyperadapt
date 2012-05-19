package net.hyperadapt.pxweave.interactions.patternmatrix;

/**
 * Interface for the dissolving of the conflict matrix.
 * 
 * @author Martin Lehmann
 *
 */
public interface IConflictMatrix {

	boolean isConflictBetween(String firstAdvice, String secondAdvice);
	
}
