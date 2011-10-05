package net.hyperadapt.pxweave.interactions.algorithm.intersection;

/**
 * class to represent a transition in an automaton
 * 
 * @author danielkadner
 *
 */
public class Transition {
	
	private State start;
	private State end;
	private String label;
	
	public Transition(State start, String label, State end){
		this.start = start;
		this.end = end;
		this.label = label;
	}
	
	public String toString(){
		return "("+start.getName()+":'"+label+"') -> "+end.getName();
	}
	
	public boolean isEqual(Transition t){
		if (this.start.equals(t.getStart())
				&& this.label.equals(t.getLabel())
				&& this.end.equals(t.getEnd())) return true;
		return false;
	}

	public State getStart() {
		return start;
	}

	public void setStart(State start) {
		this.start = start;
	}

	public State getEnd() {
		return end;
	}

	public void setEnd(State end) {
		this.end = end;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
