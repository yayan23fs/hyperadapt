package net.hyperadapt.pxweave.interactions.patternmatrix;
import java.util.HashMap;
import java.util.Map;


/**
 * @author dkadner
 */
public class Primitive {
	
	/**
	 * @author dkadner
	 */
	public enum Name {
		ChangeOrder, 
		ChangeValue, 
		ChooseVariant, 
		CollapseElement,
		DeleteElement,
		EnrichContent,
		ExpandElement,
		InsertElement,
		MoveElement,
		ReduceContent;
		
		public static boolean checkForEnum(String s){
			try {
				valueOf(s);
				return true;
			} catch(IllegalArgumentException e){
				return false;
			}
		}
	}
	
	private Name pN;

	private HashMap<Primitive, HashMap<String, String>> conflictList;

	public Primitive(Name pN) {
		this.pN = pN;
		conflictList = new HashMap<Primitive, HashMap<String, String>>();
	}

	public void addErrorPattern(Primitive p, HashMap<String, String> texts) {
		conflictList.put(p, texts);
	}

	public HashMap<String, String> lookForError(Primitive p) {
		return conflictList.get(p.getPatternName());
	}

	public Name getPatternName() {
		return pN;
	}

	public HashMap<Primitive, HashMap<String, String>> getConflictList() {
		return conflictList;
	}

	public boolean hasConflictWith(String s) {
		if (Name.checkForEnum(s)) {
			for (Map.Entry<Primitive, HashMap<String, String>> e : conflictList.entrySet()) {
				if (e.getKey().getPatternName().name().equals(s))
					return true;
			}
		}
		return false;
	}
	
	public String getReasonForPattern(String s){
		if (Name.checkForEnum(s)) {
			for (Map.Entry<Primitive, HashMap<String, String>> e : conflictList.entrySet()) {
				if (e.getKey().getPatternName().name().equals(s))
					return e.getValue().get(ConflictMatrix.REASON);
			}
		}
		return null;
	}
	
	public String getSolutionForPattern(String s){
		if (Name.checkForEnum(s)) {
			for (Map.Entry<Primitive, HashMap<String, String>> e : conflictList.entrySet()) {
				if (e.getKey().getPatternName().name().equals(s))
					return e.getValue().get(ConflictMatrix.SOLUTION);
			}
		}
		return null;
	}
	
	public String getStatusForPattern(String s){
		if (Name.checkForEnum(s)) {
			for (Map.Entry<Primitive, HashMap<String, String>> e : conflictList.entrySet()) {
				if (e.getKey().getPatternName().name().equals(s))
					return e.getValue().get(ConflictMatrix.STATUS);
			}
		}
		return null;
	}

}
