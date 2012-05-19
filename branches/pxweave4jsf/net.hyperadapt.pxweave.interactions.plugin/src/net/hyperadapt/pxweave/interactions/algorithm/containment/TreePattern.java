package net.hyperadapt.pxweave.interactions.algorithm.containment;


import java.util.Vector;

/*
 * Created on 24.02.2005
 */

/**
 * This class represents a Tree Pattern.
 * a Tree Pattern consists of a root node and a Vector for Post order representation
 *
 * @author khaled
 */
public class TreePattern {

	/** The root node of the TreePattern * */
	private Node root;

    /** The longest number of *-nodes forming a path consisting exclusively of child edges **/
	private int starLength;

	/**
	 * default Constructor creates a TreePattern with root = null
	 *
	 */
	public TreePattern() {
		root = null;
	}

	/**
	 * Constructor creates a TreePattern and initialize its root
	 *
	 * @param root the root of the TreePattern
	 */
	public TreePattern(Node root) {
		this.root = root;
	}

	/**
	 * Constructor with two parameters creates a TreePattern and initialize its root and the starLength variable
	 *
	 * @param root the root of the TreePattern
	 * @param starLength the longest star sequence
	 */
	public TreePattern(Node root , int starLength) {
		this.root = root;
		this.starLength = starLength;
	}

	/**
	 * Returns the longest number of *-nodes forming a path consisting exclusively of child edges
	 *
	 * @return starLength the longest star sequence
	 */
	public int getStarLength() {
		return starLength;
	}

	/**
	 * This methode transforms a TreePattern nodes and edges into a post ordered list
	 * and assignes all nodes and edges a unique identifier
	 *
	 * @param node the node to be transformed
	 * @param result Vector for post order representation of nodes and edges
	 */
	private void post(Node node , Vector<Object> result) {
		if (node != null) {
			if (node.getChildren() != null) {
				for (int i = 0; i < node.getChildren().size(); i++) {
					post((Node) node.getChildren().get(i) , result);
				}
				for (int i = 0; i < node.getChildren().size(); i++) {
					Edge temp = (Edge)((Node)node.getChildren().get(i)).getEdge();
					temp.setId(result.size());
					result.addElement(temp);

				}
			}
			node.setId(result.size());
			result.addElement(node);

		}

	}

	/**
	 * This methode invokes the methode post with the parameter root of the TreePattern
	 * and returns a Vector with all nodes and edges in post order representation
	 *
	 * @return result the post-ordered Vector of nodes and edges
	 */
	public Vector<Object> postorder() {
		Vector<Object> result = new Vector<Object>();
		post(root , result);
		return result;
	}
}