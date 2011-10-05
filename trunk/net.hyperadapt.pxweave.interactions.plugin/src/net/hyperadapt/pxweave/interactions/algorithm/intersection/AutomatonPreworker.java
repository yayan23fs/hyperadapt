package net.hyperadapt.pxweave.interactions.algorithm.intersection;


import java.io.IOException;
import java.util.ArrayList;

import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.testmains.IntersectionTest;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

/**
 * class for preparatory work for creating the automatons
 * 
 * @author danielkadner
 * 
 */
public class AutomatonPreworker {

	private boolean debug = DebugMode.debug;
	private ArrayList<String> alphabet;
	private Automaton aP;
	private Automaton aQ;

	/**
	 * method for linearize a string to create an automaton
	 * 
	 * @param in
	 *            the string to linearize
	 * @return the linearized string
	 */
	public String linearize(String in) {
		in = in.replaceAll("/\\w*:", "/");
		return in.replaceAll("\\[.*?\\]", "");
	}

	/**
	 * method to catch the complete alphabet as union of both automaton strings
	 * 
	 * @param p
	 *            first automaton string
	 * @param q
	 *            second automaton string
	 * @return a list with complete alphabet of both automaton strings
	 */
	public ArrayList<String> catchCompleteAlphabet(String p, String q) {
		alphabet = new ArrayList<String>();
		String[] splittet = p.split("/");
		for (String s : splittet) {
			if (!s.equals("") && s != null && !s.equals("*")) {
				if (!isInAlphabet(s))
					alphabet.add(s);
			}
		}
		splittet = q.split("/");
		for (String s : splittet) {
			if (!s.equals("") && s != null && !s.equals("*")) {
				if (!isInAlphabet(s))
					alphabet.add(s);
			}
		}
		return alphabet;
	}

	private boolean isInAlphabet(String s) {
		for (String string : alphabet) {
			if (string.equals(s))
				return true;
		}
		return false;
	}

	/**
	 * method to create both automatons
	 * 
	 * @param p
	 *            first automaton string
	 * @param q
	 *            second automaton string
	 * @throws ParseQueryException
	 * @throws ParseFunctionException
	 * @throws IOException
	 */
	public void createAutomatons(String p, String q) throws ParseQueryException,
			ParseFunctionException, IOException {
		p = p.replace(" ", "");
		q = q.replace(" ", "");
		
		p = linearize(p);
		q = linearize(q);
		// System.out.println(linearize(p));
		ArrayList<String> completeAlphabet = catchCompleteAlphabet(p, q);
		if (debug) {
			System.out.println(linearize(p));
			System.out.println(completeAlphabet);
		}
		aP = new Automaton(p, completeAlphabet);
		aQ = new Automaton(q, completeAlphabet);
	}

	public static void main(String[] args) {
		IntersectionTest.main(args);
	}

	public Automaton getAutomatonP() {
		return aP;
	}

	public Automaton getAutomatonQ() {
		return aQ;
	}
}
