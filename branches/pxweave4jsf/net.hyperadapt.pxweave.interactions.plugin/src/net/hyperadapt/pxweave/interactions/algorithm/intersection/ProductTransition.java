package net.hyperadapt.pxweave.interactions.algorithm.intersection;

/**
 * class represent a product transition for a pa
 * 
 * @author danielkadner
 *
 */
public class ProductTransition {
	
	private State[] in;
	private State[] out;
	private String label;

	public ProductTransition(State pIn, State qIn, String label, State pOut, State qOut){
		in = new State[2];
		in[0] = pIn;
		in[1] = qIn;
		this.label = label;
		out = new State[2];
		out[0] = pOut;
		out[1] = qOut;
	}
	
	public ProductTransition(State[] in, String label, State[] out){
		this.in = in;
		this.label = label;
		this.out = out; 
	}
	
	public String toString(){
		return "[(" + in[0] + "," + in[1] + "), '" + label+"' -> ("+ out[0] +"," + out[1] + ")]"; 
	}

	public State[] getIn() {
		return in;
	}

	public State[] getOut() {
		return out;
	}

	public String getLabel() {
		return label;
	}
}
