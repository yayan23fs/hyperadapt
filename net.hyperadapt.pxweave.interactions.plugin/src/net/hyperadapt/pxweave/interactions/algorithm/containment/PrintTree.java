package net.hyperadapt.pxweave.interactions.algorithm.containment;
import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author danielkadner
 *
 */

public class PrintTree {

	static String nodeString;

	/**
	 * Constructor to print the tree by a given node
	 * 
	 * @param n the node by that the tree is to be drawing 
	 */
	public PrintTree(Node n) {
		System.out.println("--------- print tree for given Node --------");
		nodeString = "";
		printNode(n, 0);
		System.out.println(nodeString);

	}
	
	/**
	 * Constructor to print the tree by a given xpath-string
	 * 
	 * @param s the xpath-string by that the tree is to be drawing 
	 */
	public static void printTheTree(String s){
		System.out.println("--------- print tree for given String "+ s +" --------");
		nodeString = "";
		Parser p = new Parser();
		try {
			printNode(p.parseQuery(s), 0);
		} catch (ParseQueryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseFunctionException e) {
			e.printStackTrace();
		}
		System.out.println(nodeString);
	}

	private static void printNode(Node n, int level) {
		printBeginning(n, level);

		// childs of node
		if (n.getArity() > 0) {
			for (Node nIn : n.getChildren()) {
				if (nIn.getEdge() != null) {
					if (nIn.getEdge().isDescendant()) {
						printNode(nIn, level + 2);
					} else
						printNode(nIn, level + 1);
				} else {
					printNode(nIn, level + 1);
				}
			}
		}

		printEnd(n, level);

	}

	private static void printBeginning(Node n, int level) {
		String sn = "";

		if (n.getEdge() != null) {
			if (n.getEdge().isDescendant()) {
				for (int i = 0; i < level - 1; i++) {
					sn += "\t";
				}
				sn += "...\n";
				// level++;
			}
		}

		// Node anfang schreiben
		for (int i = 0; i < level; i++) {
			sn += "\t";
		}

		if (n.getNamespace() != null) sn += "<" + n.getNamespace() + ":" + n.getLabel();
		else sn += "<" + n.getLabel();
		if (n.getAttributeCount() >= 1) {

			for (Map.Entry<String, String> entry : n.getAttributes().entrySet()) {
				// System.out.println(entry.getKey() + "=" +entry.getValue());
				sn += " " + entry.getKey() + "=" + entry.getValue();
			}
		}
		if (n.getArity() == 0)
			sn += ">";
		else
			sn += ">\n";

		nodeString += sn;
	}

	private static void printEnd(Node n, int level) {
		// node end
		String sn = "";

		if (n.getArity() != 0) {
			for (int i = 0; i < level; i++) {
				sn += "\t";
			}
			if (n.getNamespace() != null) sn += "<" + n.getNamespace() + ":" + n.getLabel() + ">\n";
			else sn += "<" + n.getLabel() + ">\n";
		}

		else {
			nodeString = nodeString.substring(0, nodeString.length()-1);
			sn += " />\n";
		}

		if (n.getEdge() != null) {
			if (n.getEdge().isDescendant()) {
				for (int i = 0; i < level - 1; i++) {
					sn += "\t";
				}
				sn += "...\n";
			}
		}
		nodeString += sn;
	}
}
