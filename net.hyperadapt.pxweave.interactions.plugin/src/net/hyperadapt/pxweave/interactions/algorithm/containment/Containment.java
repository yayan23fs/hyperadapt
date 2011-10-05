package net.hyperadapt.pxweave.interactions.algorithm.containment;


import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

/**
 * The class Containment.java contains the method testContainment (TreePattern1,
 * TreePattern2). It does the whole calculations to solve the containment
 * problem.
 * 
 * @author khaled
 * @author danielkadner
 */
public class Containment {

	private boolean debug = DebugMode.debug;

	private Vector<Object> postOrderP;
	private Vector<Object> postOrderQ;

	/**
	 * Standard Constructor
	 */
	public Containment() {

	}

	/**
	 * The method testContainment(TreePattern1, TreePattern2) is a complete
	 * algorithm for the containment problem. The iteration proceeds bottom up
	 * on the nodes and edges of the Tree Pattern p and computes the set of
	 * match sets for each node and edge.
	 * 
	 * @param p
	 *            TreePattern p
	 * 
	 * @param q
	 *            TreePattern q
	 * 
	 * @return true if q contains p and false otherwise.
	 */
	public boolean testContainment(TreePattern p, TreePattern q) {
		postOrderP = p.postorder();
		if (debug) {
			System.out.println();
			System.out.println("Ids des Pattern 1:");
			for (int i = 0; i < postOrderP.size(); i++) {
				if (postOrderP.get(i) instanceof Node) {
					System.out.println(((Node) postOrderP.elementAt(i)).getId() + " => " + ((Node) postOrderP.elementAt(i)).getFullname());
				} else {
					System.out.println(((Edge) postOrderP.elementAt(i)).getId() + " => " + ((Edge) postOrderP.elementAt(i)).isDescendant());
				}
			}
			System.out.println();
		}
		postOrderQ = q.postorder();
		if (debug) {
			System.out.println();
			System.out.println("Ids des Pattern 2:");
			for (int i = 0; i < postOrderQ.size(); i++) {
				if (postOrderQ.get(i) instanceof Node) {
					System.out.println(((Node) postOrderQ.elementAt(i)).getId() + " => " + ((Node) postOrderQ.elementAt(i)).getFullname());
				} else {
					System.out.println(((Edge) postOrderQ.elementAt(i)).getId() + " => " + ((Edge) postOrderQ.elementAt(i)).isDescendant());
				}
			}
			System.out.println();
		}
		int starLength = q.getStarLength();
		ArrayList<boolean[]> matchsets;
		Node node;
		Edge edge;
//		String label;
		for (int i = 0; i < postOrderP.size(); i++) {
			if (postOrderP.get(i) instanceof Node) {
				node = (Node) postOrderP.elementAt(i);
//				label = node.getLabel();
				getAllNodeMS(node, postOrderQ);
			} else {
				edge = (Edge) postOrderP.elementAt(i);
				Node n = new Node(edge.getFather().getLabel());
				// n noch ns und attribute hinzu
				if (debug){
				System.out.println("NS " + edge.getFather().getNamespace());
				System.out.println("ATTRIBUTES " + edge.getFather().getAttributes());
				}n.setNamespace(edge.getFather().getNamespace());
				n.setAttributes(edge.getFather().getAttributes());

				matchsets = edge.getChild().getMatchsets();
				if (edge.isDescendant()) {
					if (matchsets != null) {
						int idx = matchsets.size();
						for (int j = 0; j < idx; j++) {
							inflateMS(postOrderQ, matchsets, (boolean[]) matchsets.get(j), starLength);
						}
					}
				}
				getAllEdgeMS(edge, postOrderQ, matchsets, n);
			}
		}
		node = (Node) postOrderP.get(postOrderP.size() - 1);
		matchsets = node.getMatchsets();
		if (matchsets == null)
			return false;
		boolean[] ms;
		for (int i = 0; i < matchsets.size(); i++) {
			ms = (boolean[]) matchsets.get(i);
			if (!ms[postOrderQ.size() - 1])
				return false;
		}
		return true;
	}

	/**
	 * This methode computes the union of all edge subpatterns for a given
	 * matchset ms and node label. It is used for computing edgeMS(ms,label).
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of the Tree Pattern q
	 * 
	 * @param ms
	 *            the match set of the child node of the edge
	 * 
	 * @param label
	 *            the label of the father node of the edge
	 * 
	 * @return ms the matchset of the union of all edge subpatterns
	 */
	private boolean[] unionAllEdgeMs(Vector<Object> nodeQ, boolean[] ms, Node n) {
		boolean[] result = new boolean[nodeQ.size()];
		Edge edge;
		for (int i = 0; i < nodeQ.size(); i++) {
			if (nodeQ.get(i) instanceof Edge) {
				edge = (Edge) nodeQ.get(i);
				if (!edge.isDescendant()) {
					if (ms[edge.getChild().getId()] && (edge.getFather().getLabel().equals(n.getLabel()) || edge.getFather().getLabel().equals("*"))) {
						result[edge.getId()] = true;
					}
				} else {
					if (ms[edge.getChild().getId()] || ms[edge.getId()]) {
						result[edge.getId()] = true;
					}
				}
			}
		}
		return result;

	}

	/**
	 * This methode computes the matchsets for an edge. It invokes the methode
	 * edgeMS(ms, label) for all matchsets of the child node of the edge to be
	 * processed.
	 * 
	 * @param edge
	 *            the edge to be processed
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of Tree Pattern q
	 * 
	 * @param matchsets
	 *            the matchsets list of the child node of this edge
	 * 
	 * @param label
	 *            the label of the father node of this edge
	 * 
	 */
	private void getAllEdgeMS(Edge edge, Vector<Object> nodeQ, ArrayList<boolean[]> matchsets, Node n) {
		boolean[] ms;
		if (matchsets != null) {
			for (int i = 0; i < matchsets.size(); i++) {
				if (debug){System.out.println("getAllEdgeMS AUfruf edgeMS");}
				ms = edgeMS(nodeQ, (boolean[]) matchsets.get(i), n);
				edge.addMatchset(ms);
			}
		}

	}

	/**
	 * The methode inflateMS(nodeQ,matchsets,ms,starLength) calls the methode
	 * edgeMS(ms,*) repeatedly ("starLength+1")-times if the edge is descendant.
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of Tree Pattern q
	 * 
	 * @param matchsets
	 *            the matchsets list of the child node of this edge
	 * 
	 * @param ms
	 *            the matchset of the child node
	 * 
	 * @param starLength
	 *            The longest number of *-nodes forming a path consisting
	 *            exclusively of child edges
	 * 
	 */
	private void inflateMS(Vector<Object> nodeQ, ArrayList<boolean[]> matchsets, boolean[] ms, int starLength) {
		boolean[] tmp = ms;
		boolean[] tmp2;
		for (int i = 0; i < starLength + 1; i++) {
			tmp2 = tmp;
			Node n = new Node("*");
			if (debug){System.out.println("aus inflateMS Aufruf edgeMS");}
			tmp = edgeMS(nodeQ, tmp, n);
			if (isEqual(tmp2, tmp))
				break;
			matchsets.add(tmp);
		}
	}

	/**
	 * This methode edgeMS(nodeQ,ms,label) computes a matchset for an edge. It
	 * invokes the methode closeMS(nodeQ,ms,label) and
	 * unionAllEdgeMs(nodeq,ms,label).
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of Tree Pattern q
	 * 
	 * @param ms
	 *            the matchset of the child node of the edge to be processed
	 * 
	 * @param label
	 *            the label of the father node of the edge to be processed
	 * 
	 * @return matchset the computed matchset of the edge
	 */
	private boolean[] edgeMS(Vector<Object> nodeQ, boolean[] ms, Node n) {
		if (debug){System.out.println("aus edgeMS");}
		return closeMS(nodeQ, unionAllEdgeMs(nodeQ, ms, n), n);
	}

	/**
	 * The methode closeMS(nodeQ,ms,label) computes a node matchset for a given
	 * matchset ms and a label.
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of Tree Pattern q
	 * 
	 * @param ms
	 *            the union of all edge matchsets of the node to be processed
	 * 
	 * @param label
	 *            the label of the node to be processed
	 * 
	 * @return matchset the computed matchset of the node
	 */
	private boolean[] closeMS(Vector<Object> nodeQ, boolean[] ms, Node n) {
		Node temp;
		boolean a, b, c, d;
		String label = n.getLabel();
		String namespace = n.getNamespace();
		for (int i = 0; i < nodeQ.size(); i++) {
			if (nodeQ.get(i) instanceof Node) {
				temp = (Node) nodeQ.get(i);
				d = true;
				String s = temp.getLabel();
				if (label.equals("#")) {
					a = s.equals(label);
				} else {
					a = (s.equals(label) || s.equals("*"));
				}
				b = verifyAllEdges(temp, ms);

				if (debug) {
					System.out.println();
					System.out.print("Vergleiche: ");
					if (namespace != null) {
						System.out.print(namespace + ":" + label);
						if (n.getAttributeCount() >= 1) {
							String sn = "";
							for (Map.Entry<String, String> entry : n.getAttributes().entrySet()) {
								// System.out.println(entry.getKey() + "="
								// +entry.getValue());
								sn += " " + entry.getKey() + "=" + entry.getValue();
							}
							System.out.print("(" + sn + ")");
						}
					} else {
						System.out.print(label);
						if (n.getAttributeCount() >= 1) {
							String sn = "";
							for (Map.Entry<String, String> entry : n.getAttributes().entrySet()) {
								// System.out.println(entry.getKey() + "="
								// +entry.getValue());
								sn += " " + entry.getKey() + "=" + entry.getValue();
							}
							System.out.print("(" + sn + ")");
						}
					}
					System.out.print(" mit ");
					if (temp.getNamespace() != null) {
						System.out.print(temp.getNamespace() + ":" + temp.getLabel());
						if (temp.getAttributeCount() >= 1) {
							String sn = "";
							for (Map.Entry<String, String> entry : temp.getAttributes().entrySet()) {
								// System.out.println(entry.getKey() + "="
								// +entry.getValue());
								sn += " " + entry.getKey() + "=" + entry.getValue();
							}
							System.out.print("(" + sn + ")");
						}
					} else {
						System.out.print(temp.getLabel());
						if (temp.getAttributeCount() >= 1) {
							String sn = "";
							for (Map.Entry<String, String> entry : temp.getAttributes().entrySet()) {
								// System.out.println(entry.getKey() + "="
								// +entry.getValue());
								sn += " " + entry.getKey() + "=" + entry.getValue();
							}
							System.out.print("(" + sn + ")");
						}
					}

					System.out.println();
				}

				if (namespace == null) {
					if (temp.getNamespace() == null) {
						d = true;
					} else {
						d = false;
					}
				} else {
					if (temp.getNamespace() == null) {
						d = true;
					} else {
						if (namespace.equals(temp.getNamespace()))
							d = true;
						else
							d = false;
					}
				}

				// vergleiche ob attribute auch passen
				c = false;
				if (n.getAttributeCount() != 0) {
					if (temp.getAttributeCount() != 0) {
//						System.out.println("Attribute N "+n.getAttributes());
//						System.out.println("Attribute TEMP "+temp.getAttributes());
						for (Map.Entry<String, String> entryP : temp.getAttributes().entrySet()) {
							if (debug)System.out.println(entryP);
							if (n.containsKeyValue(entryP)) {
								c = true;
//								break;
							} else {
								c = false;
							}
						}
					}
					else {
						c = false;
					}
				}
				else {
					if (temp.getAttributeCount() != 0){
						c = false;
					}
					else {
						c = true;
					}
				}

				if (debug) {
//					System.out.println("label == TempLabel oder * ? " + a);
//					System.out.println("passen alle Kantensubpattern ? " + b);
//					System.out.println("Attribute ? " + c); 
//					System.out.println("Namespace ? " + d);
				}

				if ((a && b) && (c && d)) {
					ms[temp.getId()] = true;
				}
//				 if (!c) {
//				 ms[temp.getId()] = false;
//				 }
				// if (!d) {
				// ms[temp.getId()] = false;
				// }

//				if (debug) {
//					System.out.println("Matchset " + temp.getId() + " => " + ms[temp.getId()]);
//					System.out.print("Alle Matchsets = [");
//					for (int j = 0; j < ms.length; j++) {
//						System.out.print(ms[j] + " ");
//					}
//					System.out.print("]");
//					System.out.println();
//				}
			}
		}
		return ms;
	}

	/**
	 * Auxiliary methode for closeMS(nodeQ,ms,label) Hilfsmethode fŸr closeMS,
	 * it checks if all edge subPatterns are in the matchset ms.
	 * 
	 * @param q
	 *            the node to be processed
	 * 
	 * @param ms
	 *            the union matchset of all edge matchsets of the node to be
	 *            processed
	 * 
	 * @return true if all edge subPatterns are in ms and false otherwise
	 */
	private boolean verifyAllEdges(Node q, boolean[] ms) {
		Edge temp;
		if (q.getArity() == 0)
			return true;
		for (int i = 0; i < q.getArity(); i++) {
			// temp = (Edge) q.getEdges().get(i);
			temp = (Edge) ((Node) q.getChildren().get(i)).getEdge();
			if (!ms[temp.getId()])
				return false;
		}
		return true;
	}

	/**
	 * This methode computes the union of two matchsets ms1 and ms2.
	 * 
	 * @param ms1
	 *            the first matchset
	 * 
	 * @param ms2
	 *            the second matchset
	 * 
	 * @return matchset the union matchset of ms1 and ms2
	 */
	private boolean[] union(boolean[] ms1, boolean[] ms2) {
		for (int i = 0; i < ms1.length; i++) {
			ms1[i] = ms1[i] || ms2[i];
		}
		return ms1;
	}

	/**
	 * This methode computes the matchsets for a given node. It invokes the
	 * methode getAllMS(edges,msLength) and closeMS(nodeQ,ms,label).
	 * 
	 * @param node
	 *            the node to be processed
	 * 
	 * @param nodeQ
	 *            the list of nodes and edges of Tree Pattern q
	 * 
	 * @param label
	 *            the label of the node to be processed
	 * 
	 */
	private void getAllNodeMS(Node node, Vector<Object> nodeQ) {
		int msLength = nodeQ.size();
		// ArrayList edges = node.getEdges();
		ArrayList<Node> children = node.getChildren();
		if (children != null) {
			Vector<Object> v = getAllMS(children, msLength);
			for (int i = 0; i < v.size(); i++) {
				if (debug){System.out.println("aus getAllNodeMS if");}
				node.addMatchset(closeMS(nodeQ, (boolean[]) v.elementAt(i), node));
			}
		} else {
			boolean[] ms = new boolean[msLength];
			if (debug){System.out.println("aus getAllNodeMS else");}
			node.addMatchset(closeMS(nodeQ, ms, node));
		}
	}

	/**
	 * Auxiliary methode for getAllNodeMS, it computes the union of all
	 * combinations of edge matchsets of the node to be processed.
	 * 
	 * @param edges
	 *            the edges list of the node to be processed
	 * 
	 * @param msLength
	 *            the length of the matchset
	 * 
	 * @return Vector the list of matchsets
	 */
	private Vector<Object> getAllMS(ArrayList<Node> children, int msLength) {
		Vector<Object> result = new Vector<Object>();
		Vector<Object> v = getAllKomb(children);
		int[] a;
		for (int i = 0; i < v.size(); i++) {
			a = (int[]) v.get(i);
			boolean[] ms = new boolean[msLength];
			for (int j = 0; j < a.length; j++) {
				if (a[j] != -1) {
					ms = union(ms, (boolean[]) ((Edge) ((Node) children.get(j)).getEdge()).getMatchsets().get(a[j]));
				}
			}
			result.add(ms);
		}
		return result;
	}

	/**
	 * Auxiliary methode for getAllMS(edges,msLength). It returns a list of the
	 * matchsets combinations of all edges of a given node
	 * 
	 * @param edges
	 *            the list of all edges of a given node
	 * 
	 * @return Vector the list of all combinations
	 */
	private Vector<Object> getAllKomb(ArrayList<Node> children) {
		Edge edge;
		ArrayList<boolean[]> matchsets;
		Vector<Object> result = new Vector<Object>();
		int[] comb = new int[children.size()];
		for (int i = 0; i < comb.length; i++) {
			edge = (Edge) ((Node) children.get(i)).getEdge();
			matchsets = edge.getMatchsets();
			if (matchsets != null) {
				comb[i] = matchsets.size() - 1;
			} else {
				comb[i] = -1;
			}
		}
		result.add(comb);
		for (int i = 0; i < comb.length; i++) {
			int x = result.size();
			for (int j = 0; j < x; j++) {
				int[] tmp = (int[]) result.elementAt(j);
				if (tmp[i] > 0) {
					for (int k = 0; k < tmp[i]; k++) {
						int[] tmp2 = copy(tmp);
						tmp2[i] = k;
						result.add(tmp2);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns a copy of a given int[]
	 * 
	 * @param comb
	 *            the int[] to be copied
	 * 
	 * @return int[] a copy of a given int[]
	 */
	private int[] copy(int[] comb) {
		int[] tmp = new int[comb.length];
		for (int i = 0; i < comb.length; i++) {
			tmp[i] = comb[i];
		}
		return tmp;
	}

	/**
	 * This methode tests whether two given matchsets are identical.
	 * 
	 * @param ms1
	 *            the first matchset to be tested
	 * 
	 * @param ms2
	 *            the second matchset to be tested
	 * 
	 * @return true if ms1 = ms2 and false otherwise
	 */
	private boolean isEqual(boolean[] ms1, boolean[] ms2) {
		for (int i = 0; i < ms1.length; i++) {
			if (ms1[i] != ms2[i])
				return false;
		}
		return true;
	}
}
