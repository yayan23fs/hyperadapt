package net.hyperadapt.pxweave.interactions.algorithm.containment;


import java.util.ArrayList;

/*
 * Created on 28.02.2005
 *
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 *  The class Edge.java represents an Edge in the Tree Pattern.
 *  An edge has an ID, a list of Matchsets, a father node and a child node.
 *
 *  @author khaled
 */
public class Edge {

	/** The id of the edge **/
	private int id;

    /** the father node of the edge **/
	private Node father;

    /** the child node of the edge **/
	private Node child;

    /** whether the edge is decendant edge or child edge **/
	private boolean descendant;

    /** The matchsets list of the edge **/
	private ArrayList<boolean[]> matchsets;

	/**
	 * Constructor with two parameters creates an edge with child node and whether it is a decsendant
	 * edge or a child edge
	 *
	 * @param child the child node of the edge
	 *
	 * @param descendant whether the edge is decsendant or child edge
	 *
	 */
	public Edge(Node child, boolean descendant) {
		this.descendant = descendant;
		this.child = child;
	}

	/**
	 * Sets the variable id of this edge
	 *
	 * @param id the unique id of the edge
	 *
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the variable father of this edge, the starting point of the edge
	 *
	 * @param father the father node of the edge
	 *
	 */
	public void setFather(Node father) {
		this.father = father;
	}

	/**
	 * Returns the father node of this edge, the starting point of the edge
	 *
	 * @return father the father node of the edge
	 */
	public Node getFather() {
		return father;
	}
	/**
	 * Returns the child node of this edge, the destination point of the edge
	 *
	 * @return child the child node of the edge
	 */
	public Node getChild() {
		return child;
	}

	/**
	 * Returns true if the edge is decsendant and false otherwise
	 *
	 * @return true if decsendant false otherwise
	 */
	public boolean isDescendant() {
		return descendant;
	}

	/**
	 * Inserts a new matchset into the matchsets's list
	 *
	 * @param ms the matchset to be inserted
	 *
	 */
	public void addMatchset(boolean[] ms) {
		if (matchsets == null) {
			matchsets = new ArrayList<boolean[]>();
			matchsets.add(ms);
		} else {
			if (!isInMatchsets(matchsets, ms))
				matchsets.add(ms);
		}
	}

	/**
	 * Returns the matchsets list of this edge
	 *
	 * @return matchsets the matchsets's list
	 */
	public ArrayList<boolean[]> getMatchsets() {
		return matchsets;
	}

	/**
	 * Returns the id of this edge
	 *
	 * @return id the id of this edge
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns true if the matchset ms1 equals the matchset ms2 and false otherwise
	 *
	 * @param ms1 the first matchset to be compared
	 *
	 * @param ms2 the second matchset to be compared
	 *
	 * @return true if ms1 equals ms2, false otherwise
	 */
	private boolean isEqual(boolean[] ms1, boolean[] ms2) {
		for (int i = 0; i < ms1.length; i++) {
			if (ms1[i] != ms2[i])
				return false;
		}
		return true;
	}

	/**
	 * Returns true if the matchset ms is already in the matchsets list and false othewise
	 *
	 * @param matchsets the matchsets list
	 *
	 * @param ms the matchset to be tested
	 *
	 * @return true if ms is in the matchsets list, false otherwise
	 */
	private boolean isInMatchsets(ArrayList<boolean[]> matchsets, boolean[] ms) {
		if (matchsets == null)
			return false;
		for (int i = 0; i < matchsets.size(); i++) {
			if (isEqual(ms, (boolean[]) matchsets.get(i)))
				return true;
		}
		return false;
	}
}