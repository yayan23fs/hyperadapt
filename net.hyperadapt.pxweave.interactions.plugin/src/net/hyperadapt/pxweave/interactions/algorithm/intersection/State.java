package net.hyperadapt.pxweave.interactions.algorithm.intersection;

/**
 * class to represent a state in an automaton
 * 
 * @author danielkadner
 *
 */
public class State {
	
	private boolean isFinite = false;
	private boolean isInitial = false;;
	private String name;
	private int position;
	
	/**
	 * constructor for creating a state for an automaton 
	 * @param name the name of the state
	 * @param isInitial boolean to check if its the initial state
	 * @param isFinite boolean to check if its the finite state
	 * @param position the position in the chain to guarantee uniqueness
	 */
	public State(String name, boolean isInitial, boolean isFinite, int position){
		this.name = name;
		this.isFinite = isFinite;
		this.isInitial = isInitial;
		this.position = position;
	}

	/**
	 * method to check if state is finite
	 * @return if state is finite true, otherwise false
	 */
	public boolean isFinite() {
		return isFinite;
	}

	/**
	 * method to check if the state is initial
	 * @return if state is initial true, otherwise false
	 */
	public boolean isInitial() {
		return isInitial;
	}

	/**
	 * method to return the name of the state
	 * @return the name of the state
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * method to compare the state with an other state and check whether the states are equal
	 * @param other another state to check with
	 * @return if the other state is equal to the state true, otherwise false
	 */
	public boolean isEqual(State other)	{
		if (this.name.equalsIgnoreCase(other.name) 
				&& this.isFinite==other.isFinite 
				&& this.isInitial == other.isInitial 
				&& this.position == other.position) return true;
		return false;
	}

	/**
	 * method for getting the position
	 * @return the position of the state
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * method for returning the ths state as string
	 */
	public String toString(){
		return this.name;
	}
	

}
