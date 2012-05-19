package net.hyperadapt.pxweave.interactions.algorithm.intersection;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.hyperadapt.pxweave.interactions.algorithm.containment.Node;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.Parser;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TreePattern;
import net.hyperadapt.pxweave.interactions.algorithm.testmains.IntersectionTest;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

/**
 * class for creating an automaton for an given XPath
 * 
 * @author danielkadner
 * 
 */
public class Automaton {

	private boolean debug = DebugMode.debug;

	private Parser parser = new Parser();
	private ArrayList<State> states;
	private ArrayList<Transition> transitions;
	private ArrayList<String> alphabet;

	/**
	 * the constructor for creating an automaton
	 * 
	 * @param input
	 *            the xpath
	 * @param alphabet
	 *            the alphabet, for example if there are more words than in the
	 *            xpath
	 */
	public Automaton(String input, ArrayList<String> alphabet) {
		this.states = new ArrayList<State>();
		this.transitions = new ArrayList<Transition>();
		this.alphabet = alphabet;

		try {
			Node n = parser.parseQuery(input);

			TreePattern tp = new TreePattern(n);
			Vector<Object> post = tp.postorder();

			// get all Nodes
			List<Node> nodeList = new ArrayList<Node>();
			for (int i = 0; i < post.size(); i++) {
				if (post.get(i) instanceof Node) {
					nodeList.add((Node) post.get(i));
				}
			}
			setStates(nodeList);
			setTransitions(nodeList);

		} catch (ParseQueryException e) {
			e.printStackTrace();
		} catch (ParseFunctionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// parse the xpath and create corresponding states
	private void setStates(List<Node> nodeList) {
		if (nodeList.size() >= 1) {

			// add first State either with NS or not
			State firstState;
			if (nodeList.get(0).getNamespace() != null) {
				firstState = new State(nodeList.get(0).getNamespace() + ":"
						+ nodeList.get(0).getLabel(), true, false, nodeList.get(0).getId());
			} else {
				firstState = new State(nodeList.get(0).getLabel(), true, false, nodeList.get(0)
						.getId());
			}
			addStateToList(firstState);

			// add next State accept last either with NS or not
			for (int i = 1; i < nodeList.size() - 2; i++) {
				State betweenState;
				if (nodeList.get(i).getNamespace() != null) {
					betweenState = new State(nodeList.get(i).getNamespace() + ":"
							+ nodeList.get(i).getLabel(), false, false, nodeList.get(i).getId());
				} else {
					betweenState = new State(nodeList.get(i).getLabel(), false, false, nodeList
							.get(i).getId());
				}
				addStateToList(betweenState);
			}

			// add last State either with NS or not
			State lastState;
			if (nodeList.get(nodeList.size() - 2).getNamespace() != null) {
				lastState = new State(nodeList.get(nodeList.size() - 2).getNamespace() + ":"
						+ nodeList.get(nodeList.size() - 2).getLabel(), false, true, nodeList.get(
						nodeList.size() - 2).getId());
			} else {
				lastState = new State(nodeList.get(nodeList.size() - 2).getLabel(), false, true,
						nodeList.get(nodeList.size() - 2).getId());
			}
			addStateToList(lastState);
		}
		if (debug) {
			System.out.println("NodeList " + nodeList);
			System.out.println("States " + states);
		}
	}

	// methos for creating the transitons with the states according to the xpath
	private void setTransitions(List<Node> nodeList) {
		for (int i = 0; i < nodeList.size() - 2; i++) {
			State start = getState(nodeList.get(i).getLabel(), nodeList.get(i).getId());
			Node father = nodeList.get(i + 1);
			State fatherState = getState(father.getLabel(), father.getId());
			if (debug) {
				System.out.println("\tStart " + start);
				System.out.println("\tfatherState " + fatherState + " " + father.getLabel() + " "
						+ father.getId());

			}
			if (fatherState == null)
				break;
			if (start.getName().equals("#")) {
				// zum testen
				for (String a : alphabet) {
					// such nur den einen zum weiter kommen
					if (debug) {
						System.out.println("father label " + father.getLabel() + "  Alpahbet " + a);
					}
					if (!father.getLabel().equals(a)) {
						addTransitionToList(new Transition(start, a, start));
						if (debug) {
							System.out.println("adde Self Transition " + start + " '" + a + "' -> "
									+ start);
						}
					} else {

					}
				}

			}

			// wenn // dann alle mšglichen transitions zwischen staret und end.
			// au§er einer die zum nŠchsten state
			if (nodeList.get(i).getEdge().isDescendant()) {
				for (String e : alphabet) {
					String label = e;
					if (label.equals("#") || label.equals("*"))
						continue;
					Transition t = new Transition(start, label, start);
					addTransitionToList(t);
					if (debug) {
						System.out.println("adde Self Transition " + start + " '" + label + "' -> "
								+ start);
					}
					int position = isTransitionFrom(nodeList, start, label);
					if (position != -1) {
						Transition tToNext = new Transition(start, label, getState(label, position));
						if (debug) {
							System.out.println("adde GetState Transition " + start + " '" + label
									+ "' ->  " + getState(label, position));
						}
						addTransitionToList(tToNext);
					}
				}
			}

			// Kante ist ein /
			else {
				for (String label : alphabet) {
					// String label = e;
					if (label.equals("#") || label.equals("*"))
						continue;

					int position = isTransitionFrom(nodeList, start, label);
					// System.out.println(position+ "   start "+ start.getName()
					// + "    " + father.getLabel() );
					if (position != -1) {
						// System.out.println("adde Transition " + start + " '"
						// + label + "' -> " + getState(label, position));
						Transition tToNext = new Transition(start, label, getState(label, position));
						addTransitionToList(tToNext);
						if (debug) {
							System.out.println("adde GetState Transition " + start + " '" + label
									+ "' -> " + getState(label, position));
						}
					}
				}
			}

			// Knoten ist ein STAR
			if (father.getLabel().equals("*")) {
				for (String e : alphabet) {
					String label = e;
					if (label.equals("#") || label.equals("*"))
						continue;
					Transition tToNext = new Transition(start, label, fatherState);
					if (debug) {
						System.out.println("adde Father Transition " + start + " '" + label
								+ "' -> " + fatherState);
					}

					addTransitionToList(tToNext);
				}
			}
		}
	}

	// helper method to get the position of the "state"-node in the xpath
	private int isTransitionFrom(List<Node> nodeList, State start, String end) {
		for (int i = 0; i < nodeList.size() - 1; i++) {
			Node current = nodeList.get(i);
			Node next = nodeList.get(i + 1);
			// System.out.println("Teste currentlabel " +current.getLabel() +
			// "     "+next.getLabel());
			if (current.getLabel().equals(start.getName()) && next.getLabel().equals(end)
					&& current.getId() == start.getPosition())
				return next.getId();
		}
		return -1;
	}

	// helper method to get the corresponding state by a name and the position
	// in xpath
	private State getState(String name, int position) {
		for (State s : states) {
			if (s.getName().equals(name) && s.getPosition() == position) {
				return s;
			}
		}
		return null;
	}

	// helper method to add a new state to the list. if state already exists
	// nothing happens
	private boolean addStateToList(State njew) {
		boolean isIn = false;
		for (State s : states) {
			isIn = njew.isEqual(s);
		}
		if (!isIn) {
			states.add(njew);
			return true;
		}
		return false;
	}

	// helper method to add a new transition to the list. if transition already
	// exists nothing happens
	private boolean addTransitionToList(Transition njew) {
		boolean isIn = false;
		for (Transition t : transitions) {
			isIn = njew.isEqual(t);
		}
		if (!isIn) {
			transitions.add(njew);
			return true;
		}
		return false;
	}

	// helper method to get all endstates by a given list of states and an
	// transition-label
	private ArrayList<State> getEndStates(ArrayList<State> states, String character) {
		ArrayList<State> list = new ArrayList<State>();

		for (State s : states) {
			for (int i = 0; i < transitions.size(); i++) {
				Transition t = transitions.get(i);
				if (t.getStart().isEqual(s) && t.getLabel().equals(character))
					list.add(t.getEnd());
			}
		}

		return list;
	}

	/**
	 * method to check if the automaton accepts the word or not
	 * 
	 * @param word
	 * @return true if the automaton accepts the word, otherwise false
	 */
	public boolean checkWord(String word) {
		boolean fits = false;

		String[] words = word.split("/");
		ArrayList<State> lasts = new ArrayList<State>();
		State initial = getInitialState();
		lasts.add(initial);
		for (int i = words.length - 1; i >= 0; i--) {
			if (!words[i].equals(""))
				lasts = getEndStates(lasts, words[i]);
		}
		for (State s : lasts) {
			if (s.isFinite())
				fits = true;
		}

		return fits;
	}

	/**
	 * return the endstate for a given state and a character in the context of
	 * the automaton
	 * 
	 * @param state
	 * @param character
	 * @return the list with the possible endstates
	 */
	public ArrayList<State> getEndStates(State state, String character) {
		ArrayList<State> list = new ArrayList<State>();

		for (int i = 0; i < transitions.size(); i++) {
			Transition t = transitions.get(i);
			if (t.getStart().isEqual(state) && t.getLabel().equals(character))
				list.add(t.getEnd());
		}
		return list;
	}

	/**
	 * method to get the inital state of the automaton
	 * 
	 * @return the initial state
	 */
	public State getInitialState() {
		for (State s : states) {
			if (s.isInitial())
				return s;
		}
		return null;
	}

	/**
	 * method to get the final state of teh automaton
	 * 
	 * @return the final state
	 */
	public State getFinalState() {
		for (State s : states) {
			if (s.isFinite())
				return s;
		}
		return null;
	}

	public ArrayList<State> getAllStates() {
		return states;
	}

	public ArrayList<Transition> getAllTransitions() {
		return transitions;
	}

	public ArrayList<String> getAlphabet() {
		return alphabet;
	}

	public static void main(String[] args) {
		IntersectionTest.main(args);
	}
}
