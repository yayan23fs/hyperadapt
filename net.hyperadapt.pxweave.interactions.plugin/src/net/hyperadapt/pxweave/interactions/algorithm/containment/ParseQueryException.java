package net.hyperadapt.pxweave.interactions.algorithm.containment;

/*
 * Created on 31.05.2005
 */

/**
 * Thrown when an XPath query syntax error is detected.
 *
 * @author khaled
 */
public class ParseQueryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2051793267084766453L;

	/**
	 * Constructs an exception with the appropriate error message
	 *
	 * @param text the error message to be indicated
	 */
	public ParseQueryException(String text, String chEnd) {
		
		

		super(text);
		String s = "Current position: "+chEnd;
		String a = "";
		for (int i = 0; i < s.length()-1; i++) {
			a = a + " ";
		}
		a = a + "^";
		System.err.println(s+"\n"+a);

	}

}