package net.hyperadapt.pxweave.interactions.algorithm.testmains;


import java.io.IOException;

import net.hyperadapt.pxweave.interactions.algorithm.containment.Containment;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Node;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Parser;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TreePattern;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.Automaton;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.AutomatonPreworker;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.ProductAutomaton;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;


//import Autow.Automaton;
//import automaton.Automaton;


public class IntersectionTest {
	
	private static TreePattern pattern_p = null;

	private static TreePattern pattern_q = null;

	private static Parser parser = new Parser();

	private static Containment containment = new Containment();

	private static Node rootP, rootQ;

	@SuppressWarnings("unused")
	private static String result;
	
	private static boolean debug = DebugMode.debug;

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		String pP = "/a/*//c//d";
//		String qq = "/a/soc:b[@e=5][@f=7][c/@d=3]/g[c/@d=3]/h";
		String qP = "/a/b/d";
		
		String qR = "/a//*/b";
		String qT = "/a/*//b";
		
		String hk = "/a/b";
		String hl = "/a//b/c";
		
		
		String p = qR;
		String q = qT;
		
		AutomatonPreworker ap = new AutomatonPreworker();
//		System.out.println(ap.linearize(qq));
		try {
			ap.createAutomatons(p, q);
		} catch (ParseQueryException e) {
			e.printStackTrace();
		} catch (ParseFunctionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Automaton aP = ap.getAutomatonP();
		Automaton aQ = ap.getAutomatonQ();
		
		String word = "/a/b/b/c/c/d";
		System.out.println();
		
		ProductAutomaton pa = new ProductAutomaton(aP, aQ);
		
		System.out.println("P trans "+ aP.getAllTransitions());
		System.out.println("Q trans "+ aQ.getAllTransitions());
		
		System.out.println("PA all state " +pa.getAllStates());
		System.out.println("PA all trans " +pa.getAllTransitions());
		System.out.println("Intersection? "+pa.isIntersection());
		
//		testContainment(p, q);
//		System.out.println("Containment? "+result);
//		
//		Automaton a = new Automaton(p);
//		System.out.println(aP.states);
//		System.out.println(aP.transitions);
//		System.out.println(aP.transitions.size());
//		System.out.println();
//		System.out.println(aQ.states);
//		System.out.println(aQ.transitions);
//		System.out.println(aQ.transitions.size());
//		
//		System.out.println("check Word '"+ word +"' in aP: "+ aP.checkWord(word));
		

	}
	
	@SuppressWarnings("unused")
	private static void testContainment(String p , String q) {
		rootP = null;
		rootQ = null;
		//parse ersten String
		try {
			rootP = parser.parseQuery(p);
		} catch (Exception e) {
			result = e.toString();
			e.printStackTrace();
		}
		//parse zweiten String
		if (rootP != null) {
			try {
				rootQ = parser.parseQuery(q);
			} catch (Exception ex) {
				result = ex.toString();
				ex.printStackTrace();
			}
			if (rootQ != null) {
				//erzeuge treepattern aus geparsten xpath anfragen
				pattern_p = new TreePattern(rootP);
				pattern_q = new TreePattern(rootQ , parser.getStarLength(q));
				if (debug) {
					System.out.println();
					System.out.println(">>>>>>>>>>>>>>Pattern P");
				}
				boolean res_1 = containment.testContainment(pattern_p, pattern_q);
				if (debug) {
					System.out.println();
					System.out.println(">>>>>>>>>>>>>>Pattern Q");
				}
				boolean res_2 = containment.testContainment(pattern_q, pattern_p);
				if (res_1) {
					if(res_2){
						result = "p \u2263 q"; //equivalenz
					}else{
						result = "p \u2286 q"; //p Teilmenge von q
					}
				}else{
					if(res_2){
						result = "p \u2287 q"; //q Teilmenge von p
					}else {
					    result = "p \u2260 q"; //keine Teilmengen
					}
				}
			}
		}

	}

}
