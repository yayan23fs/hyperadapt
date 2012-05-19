package net.hyperadapt.pxweave.interactions.algorithm.containment;

/**
 * Thrown when an XPath function is detected.
 *
 * @author danielkadner
 */
public class ParseFunctionException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3468203230805583213L;

	/**
	 * Constructs an exception with the appropriate error message
	 *
	 * @param text the error message to be indicated
	 */
	public ParseFunctionException(String text, String chEnd) {
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