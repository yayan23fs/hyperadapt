package net.hyperadapt.pxweave.interactions.algorithm.containment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

/*
 * Created on 24.02.2005
 */

/**
 * This class represents a node in the TreePattern. a node has a label, id, a
 * list of its children, a list of the edges and a list of matchsets.
 * 
 * @author khaled
 * @author danielkadner
 */
public class Node {

	/** The label of the node **/
	private String label;

	/** The id of the node **/
	private int id;

	/** The children list of the node **/
	private ArrayList<Node> children;

	/** The edge of the node **/
	private Edge edge;
	
	private String namespace;
	
	/** The position of the node **/ 
	private int position = -1;

	/** The matchsets list of the node **/
	private ArrayList<boolean[]> matchsets;
	
	/** The list of attributes of the node **/
	private HashMap<String, String> attributes;

	private boolean debug = DebugMode.debug;

	/**
	 * Constructor with one parameter creates a child node with label
	 * 
	 * @param label
	 *            the label of the node
	 * 
	 */
	public Node(String label) {
		this.label = label;
		children = null;
		attributes = new HashMap<String, String>();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	/**
	 * Returns the label of the node
	 * 
	 * @return label the node's label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the children's list of this node
	 * 
	 * @return children of the node
	 */
	public ArrayList<Node> getChildren() {
		return children;
	}

	/**
	 * Returns the edge of the node
	 * 
	 * @return edge the edge of the node
	 */
	public Edge getEdge() {
		return edge;
	}

	/**
	 * Inserts a new node into the children's list
	 * 
	 * @param node
	 *            the node to be inserted
	 */
	public void addNode(Node node) {
		if (children == null) {
			children = new ArrayList<Node>();
		}
		node.getEdge().setFather(this);
		children.add(node);
	}

	/**
	 * Sets the edge which is connected with this node
	 * 
	 * @param edge
	 *            the edge of the node
	 * 
	 */
	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	/**
	 * Inserts a new matchset into the matchsets's list
	 * 
	 * @param ms
	 *            the matchset to be inserted
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
	 * Returns the matchsets list of this node
	 * 
	 * @return matchsets the matchsets's list
	 */
	public ArrayList<boolean[]> getMatchsets() {
		return matchsets;
	}

	/**
	 * Returns the Arity("number of children") of this node
	 * 
	 * @return arity number of children
	 */
	public int getArity() {
		if (children == null)
			return 0;
		return children.size();
	}

	/**
	 * Sets the variable id of this node
	 * 
	 * @param id
	 *            the unique id of the node
	 * 
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFullname(){
		if (namespace != null){
			if (getAttributeCount()>=1){
				String sn = "";
				for (Map.Entry<String, String> entry : getAttributes().entrySet()) {
					// System.out.println(entry.getKey() + "=" +entry.getValue());
					sn += " " + entry.getKey() + "=" + entry.getValue();
				}
				return namespace + ":" + label + " " +sn;
			}
			return namespace + ":" + label;
		} else {
			if (getAttributeCount()>=1){
				String sn = "";
				for (Map.Entry<String, String> entry : getAttributes().entrySet()) {
					// System.out.println(entry.getKey() + "=" +entry.getValue());
					sn += " " + entry.getKey() + "=" + entry.getValue();
				}
				return label + " " +sn;
			}
			return label;
		}
	}

	/**
	 * Returns the id of this node
	 * 
	 * @return id the id of this node
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns true if the matchset ms1 equals the matchset ms2 and false
	 * otherwise
	 * 
	 * @param ms1
	 *            the first matchset to be compared
	 * 
	 * @param ms2
	 *            the second matchset to be compared
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
	 * Returns true if the matchset ms is already in the matchsets list and
	 * false othewise
	 * 
	 * @param matchsets
	 *            the matchsets list
	 * 
	 * @param ms
	 *            the matchset to be tested
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

	/**
	 * Add an Attribute to the node
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public void addAttribute(String name, String value) {
		attributes.put(name, value);
	}

	/**
	 * Returns true if the given entry exists in the attribute list and false
	 * otherwise
	 * 
	 * @param entry
	 *            the entry of an HashMap containing an Attribute of a node
	 * @return true if the entry exists in attributes
	 */
	public boolean containsKeyValue(Map.Entry<String, String> entry) {

		String key = entry.getKey();
		String value = entry.getValue();
		boolean keystar = (key.equals("*")) ? true : false;
		boolean valuestar = (value.equals("*")) ? true : false;

		if (keystar && valuestar) {// @*=*
			if (debug)
				System.out.println("für node: " + this.label + " beides star " + attributes.isEmpty());
			if (attributes.isEmpty())
				return false;
			else
				return true;
		}
		if (keystar && !valuestar) {// @*=3
			if (debug){
				System.out.println("für node: " + this.label + " nur value: " + value + " solution: " + attributes.containsValue(value));
				for (Map.Entry<String, String> e1 : attributes.entrySet()) {
					System.out.println(e1.getKey() + ":" + e1.getValue());
				}
			}
			if (attributes.containsValue(value) || attributes.containsValue("*"))
				return true;
			else {
				return false;
			}
				
		}
		if (!keystar && valuestar) {// @d=*
			if (debug)
				System.out.println("für node: " + this.label + " nur key " + attributes.containsKey(key));
			if (attributes.containsKey(key) || attributes.containsKey("*"))
				return true;
			else
				return false;
		}
		if (!keystar && !valuestar) { // @d=3
			if (debug)
				System.out.println("beides keine stars");
			// does the key exists?
			if (attributes.containsKey(key)){
				if ((attributes.get(key)).equals(value)){
					return true;
				}
				return false;
			}
			
//			if (attributes.containsKey(key)) {
////				// does the values for the keys matches
//				if (attributes.get(key).equalsIgnoreCase(value)){
//					return true;
//				} else {
////					// is the value a * for the key
//					if (attributes.get(key).equalsIgnoreCase("*")) {
//						return true;
//					} else {
//						return false;
//					}
//				}
//			} else {
////				// exists es key with a star?
//				if (attributes.containsKey("*")) {
//					if (attributes.get("*").equalsIgnoreCase(value)){
//						return true;
//					} else {
//						if (attributes.get("*").equalsIgnoreCase("*")){
//							return true;
//						} else {
//							return false;
//						}
//					}
//				} else {
//					return false;
//				}
////
//			}
			
			

		}
		// should never been reached
		return false;
	}

	/**
	 * Returns the value of an attribute
	 * 
	 * @param name
	 *            the name of the attribute
	 * @return the value of the attribute otherwise null
	 */
	public String getAttribute(String name) {
		if (attributes.containsKey(name)) {
			return attributes.get(name);
		} else
			return null;
	}

	/**
	 * Returns the HashMap with the attributes of the node
	 * 
	 * @return attributes if attributes exists, otherwise null
	 */
	public HashMap<String, String> getAttributes() {
		if (attributes == null || attributes.isEmpty())
			return null;
		else
			return attributes;
	}

	/**
	 * Returns the amount of attributes of the node
	 * 
	 * @return size the number of attributes of the node
	 */
	public int getAttributeCount() {
		if (attributes == null) return 0;
		else return attributes.size();
	}
	
	public void setAttributes(HashMap<String, String> attr){
		this.attributes = attr;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}