package net.hyperadapt.pxweave.interactions.algorithm.intersection;


import java.util.ArrayList;

import net.hyperadapt.pxweave.interactions.algorithm.testmains.IntersectionTest;

/**
 * class to create a product automaton based on two automatons
 * 
 * @author danielkadner
 * 
 */
public class ProductAutomaton {

	private Automaton p;
	private Automaton q;
	private ArrayList<State[]> states;
	private ArrayList<String> alphabet;
	private ArrayList<ProductTransition> transitions;
	private boolean intersect;

	/**
	 * contructor to create the product automaton by zwo given automatons
	 * 
	 * @param p
	 *            first automaton
	 * @param q
	 *            second automaton
	 */
	public ProductAutomaton(Automaton p, Automaton q) {
		this.p = p;
		this.q = q;
		alphabet = p.getAlphabet();
		states = new ArrayList<State[]>();
		transitions = new ArrayList<ProductTransition>();
		createStates();
		createTransitions();
		intersect = calculateTheIntersection();
	}

	// create states for the pa
	private void createStates() {
		ArrayList<State> pStates = p.getAllStates();
		ArrayList<State> qStates = q.getAllStates();

		for (State sP : pStates) {
			for (State sQ : qStates) {
				State[] statePair = { sP, sQ };
				states.add(statePair);
			}
		}
	}

	// create transitions for the pa
	private void createTransitions() {
		ArrayList<State> pOutStates;
		ArrayList<State> qOutStates;
		for (State[] statePair : states) {
			for (String label : alphabet) {
				pOutStates = p.getEndStates(statePair[0], label);
				qOutStates = q.getEndStates(statePair[1], label);

				if (!pOutStates.isEmpty() && !qOutStates.isEmpty()) {
					for (State pOutState : pOutStates) {
						for (State qOuState : qOutStates) {
							State[] outPair = { pOutState, qOuState };
							transitions.add(new ProductTransition(statePair, label, outPair));
						}
					}
				}
			}
		}
	}

	/**
	 * helpmethod to get the reachable states for the product automaton
	 * 
	 * @param start
	 *            the start state
	 * @param character
	 *            the given character
	 * @return an arraylist with all states reachable from start with character
	 */
	public ArrayList<State[]> getEndStates(State[] start, String character) {
		ArrayList<State[]> endStateList = new ArrayList<State[]>();
		for (ProductTransition pd : transitions) {
			if (compareStates(pd.getIn(), start) && pd.getLabel().equals(character)) {
				endStateList.add(pd.getOut());
			}
		}
		return endStateList;
	}

	/**
	 * method to get the initial pair for the pa
	 * 
	 * @return the initial state pair
	 */
	public State[] getInitialStatePair() {
		for (State[] statePair : states) {
			if (statePair[0].isInitial() && statePair[1].isInitial())
				return statePair;
		}
		return null;
	}

	/**
	 * method to get the finite pair for the pa
	 * 
	 * @return the finite state pair
	 */
	public State[] getFiniteStatePair() {
		for (State[] statePair : states) {
			if (statePair[0].isFinite() && statePair[1].isFinite())
				return statePair;
		}
		return null;
	}

	/**
	 * help method to decide whether two pa states are equal or not
	 * 
	 * @param first
	 *            the first state pair
	 * @param second
	 *            the second state pair
	 * @return true if state pairs are the same, false otherwise
	 */
	public boolean compareStates(State[] first, State[] second) {
		boolean match = false;
		if (first[0].isEqual(second[0]) && first[1].isEqual(second[1]))
			match = true;
		return match;
	}

	private boolean isIn(State[] singleStatePair, ArrayList<State[]> listOfStatesPairs) {
		boolean match = false;
		for (State[] sP : listOfStatesPairs) {
			if (compareStates(sP, singleStatePair))
				match = true;
		}
		return match;
	}

	private ArrayList<State[]> getTransitiveClosure(ArrayList<State[]> completeList,
			ArrayList<State[]> states) {
		if (states.isEmpty())
			return completeList;
		ArrayList<State[]> newStates = new ArrayList<State[]>();
		for (State[] s : states) {
			for (String character : alphabet) {
				ArrayList<State[]> endStates = getEndStates(s, character);
				for (State[] e : endStates) {
					if (e != null && !isIn(e, completeList)) {
						newStates.add(e);
					} else {
					}
				}
			}
		}
		addAllNew(completeList, newStates);
		return getTransitiveClosure(completeList, newStates);
	}

	// tries the find a route from initial state to finite state
	// return true if exists, false otherwise
	private boolean calculateTheIntersection() {
		ArrayList<State[]> list = new ArrayList<State[]>();
		ArrayList<State[]> empty = new ArrayList<State[]>();
		list.add(getInitialStatePair());
		return isIn(getFiniteStatePair(), getTransitiveClosure(empty, list));
	}

	private void addAllNew(ArrayList<State[]> old, ArrayList<State[]> newStuff) {
		for (State[] st : newStuff) {
			if (!isIn(st, old))
				old.add(st);
		}
	}

	public ArrayList<State[]> getAllStates() {
		return states;
	}

	public ArrayList<String> getCompleteAlphabet() {
		return alphabet;
	}

	public ArrayList<ProductTransition> getAllTransitions() {
		return transitions;
	}

	public boolean isIntersection() {
		return intersect;
	}

	public static void main(String[] args) {
		IntersectionTest.main(args);
	}

}
