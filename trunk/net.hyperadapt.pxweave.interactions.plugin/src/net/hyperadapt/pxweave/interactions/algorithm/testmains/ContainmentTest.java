package net.hyperadapt.pxweave.interactions.algorithm.testmains;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Containment;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Node;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Parser;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TreePattern;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;


public class ContainmentTest {

	private static TreePattern pattern_p = null;

	private static TreePattern pattern_q = null;

	private static Parser parser = new Parser();

	private static Containment containment = new Containment();

	private static Node rootP, rootQ;

	private static String result;
	
	private static boolean debug = DebugMode.debug;
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		String qQ = "/a//b[./d][.//e]";
		String qP = "a/b[@e=5][@f=7][c/@d=3]/g/h";
		String qS = "///a";
		String qA = "/a[b//c]";
		String qW = "/a/b";
		String qE = "/a/b";
		String qR = "/a//*/b";
		String qT = "/a/*//b";
		String qM = "/a[@a=7][@b=4]";
		String qU = "//b/c";
		String qG = "/a/b//c[@f=4]";
		String qH = "/a/b[.//*]/c[@f='4']";
		String qB = "/a/b";
		String qN = "/a/b[./d]";
		String qK = "/soc:a[@d=\"8\"]";
		String qL = "/soc:a[@d=8]";
		String qZ = "/a[*]";
		String qI = "/a[b]";
		String ff = "/a/@id=\"7\"";
		String fg = "/a/@id=\"7\"";
		String ft = "/a[./b]";
		String qC = "books:book[2]/books:author";
		String qD = "books:book/books:author";
		String hj = "/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source[./text]";
		String hh = "//amet:source/@id='8'";
		String hk = "/a/b";
		String hl = "/a//b/c";
		
		String gt = "/a[./@id='2']"; 
		
		String op = "//aco:AmaSetComponent[@id='j91t3sk7']/aco:SubComponents/aco:AmaAccessElement/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source[@text='images/homeL.png']";
		String oi = "//aco:AmaSetComponent[@id='j91t3sk7']/aco:SubComponents/aco:AmaAccessElement/aco:SubComponents/aco:SubComponents/aco:AmaImageComponent[./@id='images/home.png']";

		String rt = "//aco:AmaSetComponent[@id='j91t3sk7']/aco:SubComponents/aco:AmaAccessElement/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source/@id"; 


		
		String p = qB;
	    String q = qN;
		
//		Node nn = new Node("a");
//		nn.addAttribute("a", "7");
//		nn.addAttribute("b", "6");
//	
//    	PrintTree pt = new PrintTree(qQ);

		
//		char[] lll = "@".toCharArray();
//		System.out.println(Character.isLetter(lll[0]));

//		String queryP = "/a//*/b";
//		String queryQ = "/a/*//b";
		
//		String a = "\u005E";
//		System.err.println(a);
		
//	    try {
//	    	PrintTree p = new PrintTree(parser.parseQuery(qK));
//	    	
//			
//		} catch (ParseQueryException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	    PrintTree.printTheTree(p);
//	    PrintTree.printTheTree(q);
	    
	    testContainment(p, q);
	    System.out.println(result);
//        testContainment(p, q);
//        System.out.println();
//        System.out.println("p: "+p);
//        System.out.println("q: "+q);
//        System.out.println(result);
   
//        System.out.println(result);
	  	
		
	}
	
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
						result = "p == q"; //equivalenz \u2263
					}else{
						result = "p Teilmenge von q"; //p Teilmenge von q \u2286
					}
				}else{
					if(res_2){
						result = "q Teilmenge von p"; //q Teilmenge von p \u2287
					}else {
					    result = "p != q"; //keine Teilmengen \u2260
					}
				}
			}
		}

	}

}
