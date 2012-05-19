package net.hyperadapt.pxweave.interactions.algorithm.testmains;

import net.hyperadapt.pxweave.interactions.devtime.patternmatrix.ConflictMatrix;


public class ConflictMatrixTest {
	
	public static void main(String[] args){
		ConflictMatrix cm = ConflictMatrix.getInstance();
		System.out.println();
		cm.showAllErrors();
	}

}
