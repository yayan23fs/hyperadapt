package net.hyperadapt.pxweave.interactions.patternmatrix;


import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.hyperadapt.pxweave.logger.Logable;
import net.hyperadapt.pxweave.logger.LoggerConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/** 
 * @author Daniel Kadner
 */
public class ConflictMatrix extends Logable implements IConflictMatrix{

	private HashMap<Primitive.Name, Primitive> patternList;
	private Document doc;
	private XPath xpath;
	public static final String REASON = "reason";
	public static final String SOLUTION = "solution";
	public static final String STATUS = "status";

	/**
	 * Dummy constructor for empty pattern matrix. 
	 * 
	 */
	public ConflictMatrix(){
		patternList = new HashMap<Primitive.Name, Primitive>();
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	
	private ConflictMatrix(URI conflictMatrixURL) {
		try {
			this.patternList = new HashMap<Primitive.Name, Primitive>();
			this.xpath = XPathFactory.newInstance().newXPath();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(conflictMatrixURL.toString());
			xpath = XPathFactory.newInstance().newXPath();
			catchPatterns();
			readConflicts();
			
		} catch (ParserConfigurationException e) {
			getLogger().error("Error in conflict matrix configuration.", e);
		} catch (SAXException e) {
			getLogger().error("Error in conflict matrix configuration.", e);
		} catch (IOException e) {
			getLogger().error("Error in conflict matrix configuration.", e);
		}
	}
	
	public static synchronized ConflictMatrix createMatrix(URI matrixURI){
		ConflictMatrix matrix = null;
		if(matrixURI==null||!matrixURI.isAbsolute()){
			matrix = new ConflictMatrix();
			LoggerConfiguration.instance().getLogger(ConflictMatrix.class).warn("Conflict matrix URI was null or not absolute, using empty instance instead.");
		}
		else{
			matrix = new ConflictMatrix(matrixURI);
		}
		
		return matrix;
	}
	
	private void catchPatterns() {
		for (Primitive.Name pN : Primitive.Name.values()) {
			Primitive p = new Primitive(pN);
			patternList.put(pN, p);
		}
	}

	private void readConflicts() {
		try {
			Node rootNode = doc.getDocumentElement();
			for (Primitive.Name primitiveName : Primitive.Name.values()) {
				String path = "//" + rootNode.getNodeName() + "/" + primitiveName.name();
				NodeList nL = (NodeList) xpath.evaluate(path, doc, XPathConstants.NODESET);
				for (int i = 0; i < nL.getLength(); i++) {
					Element el = (Element) nL.item(i);
					// System.out.println(el.getNodeName());
					Primitive firstPattern = getPatternByName(el.getNodeName());
					NodeList elChilds = el.getChildNodes();
					for (int j = 0; j < elChilds.getLength(); j++) {
						if (elChilds.item(j) instanceof Element) {
							Element elsecond = (Element) elChilds.item(j);
							Primitive secondPattern = getPatternByName(elsecond.getNodeName());
							String reason = (String) xpath.evaluate("reason/text()", elsecond,
									XPathConstants.STRING);
							String solution = (String) xpath.evaluate("solution/text()", elsecond,
									XPathConstants.STRING);
							String status = (String) xpath.evaluate("status/text()", elsecond,
									XPathConstants.STRING);// elsecond.getFirstChild().getNodeValue();
							HashMap<String, String> texts = new HashMap<String, String>();
							texts.put(REASON, reason);
							texts.put(SOLUTION, solution);
							texts.put(STATUS, status);
							firstPattern.addErrorPattern(secondPattern, texts);
						}
					}
				}
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	private Primitive getPatternByName(String patternName) {
		return patternList.get(Primitive.Name.valueOf(patternName));
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		for (Map.Entry<Primitive.Name, Primitive> e : patternList.entrySet()) {
			Primitive p = e.getValue();
			if (!p.getConflictList().isEmpty()) {
				out.append("Advice pattern " + p.getPatternName().name()
						+ " may cause the following problems:");
				out.append("\n");
				HashMap<Primitive, HashMap<String, String>> cL = p.getConflictList();
				for (Map.Entry<Primitive, HashMap<String, String>> s : cL.entrySet()) {
					
					out.append("\t-" + s.getKey().getPatternName());
					out.append("\n");
					out.append("\t\t Status: " + s.getValue().get(STATUS));
					out.append("\n");
					out.append("\t\t Reason: " + s.getValue().get(REASON));
					out.append("\n");
					out.append("\t\t Solution: " + s.getValue().get(SOLUTION));
					out.append("\n");
				}
			}
		}
		return out.toString();
	}

	public boolean isConflictBetween(String firstAdvice, String secondAdvice) {
		if (Primitive.Name.checkForEnum(firstAdvice) && Primitive.Name.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(Primitive.Name.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return true;
			else
				return false;
		}
		return false;
	}

	public String getReasonFor(String firstAdvice, String secondAdvice) {
		if (Primitive.Name.checkForEnum(firstAdvice) && Primitive.Name.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(Primitive.Name.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice)) {
				return p.getReasonForPattern(secondAdvice);
			} else
				return null;
		}
		return null;
	}

	public String getSolutionFor(String firstAdvice, String secondAdvice) {
		if (Primitive.Name.checkForEnum(firstAdvice) && Primitive.Name.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(Primitive.Name.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return p.getSolutionForPattern(secondAdvice);
			else
				return null;
		}
		return null;
	}

	

	public String getStatusFor(String firstAdvice, String secondAdvice) {
		if (Primitive.Name.checkForEnum(firstAdvice) && Primitive.Name.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(Primitive.Name.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return p.getStatusForPattern(secondAdvice);
			else
				return null;
		}
		return null;
	}
	
	public boolean isEmpty(){
		return patternList.isEmpty();
	}
	
	
}
