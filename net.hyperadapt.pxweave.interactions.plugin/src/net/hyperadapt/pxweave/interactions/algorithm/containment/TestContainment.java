package net.hyperadapt.pxweave.interactions.algorithm.containment;

public class TestContainment {
	
	public static boolean[] testContainment(String p, String q) {
		boolean[] result = new boolean[2];
		
		p = p.replace(" ", "");
		q = q.replace(" ", "");
		
		if (p.matches("(\\S)*(\\()(\\S)*(\\))(\\S)*") || q.matches("(\\S)*(\\()(\\S)*(\\))(\\S)*")){
//			System.out.println("HALT FUNKTIONEN ENTDECKT");
			result[0] = false;
			result[1] = false;
			return result;
		}
		

		Parser parser = new Parser();
		Containment containment = new Containment();
		Node rootP, rootQ;
		rootP = null;
		rootQ = null;
		

		// parse ersten String
		try {
			rootP = parser.parseQuery(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// parse zweiten String
		if (rootP != null) {
			try {
				rootQ = parser.parseQuery(q);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (rootQ != null) {
				result[0] = containment.testContainment(new TreePattern(rootP), new TreePattern(rootQ, parser.getStarLength(q)));
				result[1] = containment.testContainment(new TreePattern(rootQ, parser.getStarLength(q)), new TreePattern(rootP));
			}
		}
		return result;

	}

}
