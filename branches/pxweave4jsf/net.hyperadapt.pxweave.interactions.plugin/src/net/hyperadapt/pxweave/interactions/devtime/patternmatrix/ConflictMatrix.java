package net.hyperadapt.pxweave.interactions.devtime.patternmatrix;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;
import net.hyperadapt.pxweave.interactions.patternmatrix.Primitive;
import net.hyperadapt.pxweave.interactions.patternmatrix.PrimitiveName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * class to represent the conflict matrix by interprete the xml file implemented
 * as singleton
 * 
 * @author danielkadner
 * 
 */
public class ConflictMatrix {

	private HashMap<PrimitiveName, Primitive> patternList;
//	private ArrayList<Primitive> primitiveList;
	private Document doc;
//	private String patternConflictsFileName;
	private XPath xpath;
	public static final String REASON = "reason";
	public static final String SOLUTION = "solution";
	public static final String STATUS = "status";

	private static ConflictMatrix instance = null;

	@SuppressWarnings("unused")
	private boolean debug = DebugMode.debug;

	private ConflictMatrix(){
		patternList = new HashMap<PrimitiveName, Primitive>();
		try {
			URL url = new URL("platform:/plugin/interaction.plugin/PatternConflicts.xml");
			InputStream inputStream = url.openConnection().getInputStream();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			
			doc = builder.parse(inputStream);
			
			xpath = XPathFactory.newInstance().newXPath();
			catchPatterns();
			readConflicts();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// auslesen aller mšglichen Primitiven aus der Enum
	private void catchPatterns() {
		for (PrimitiveName pN : PrimitiveName.values()) {
			Primitive p = new Primitive(pN);
			patternList.put(pN, p);
		}
	}

	// auslesen der Konfliktmatrix-XML-Datei
	private void readConflicts() {
		try {
			Node rootNode = doc.getDocumentElement();
			for (PrimitiveName pN : PrimitiveName.values()) {
				String path = "//" + rootNode.getNodeName() + "/" + pN.name();
				NodeList nL = (NodeList) xpath.evaluate(path, doc, XPathConstants.NODESET);
				for (int i = 0; i < nL.getLength(); i++) {
					Element el = (Element) nL.item(i);
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

	public Primitive getPatternByName(String patternName) {
		return patternList.get(PrimitiveName.valueOf(patternName));
	}

	public void showAllErrors() {
		for (Map.Entry<PrimitiveName, Primitive> e : patternList.entrySet()) {
			Primitive p = e.getValue();
			if (!p.getConflictList().isEmpty()) {
				System.out.println("Pattern " + p.getPatternName().name()
						+ " hat die folgenden Probleme mit:");
				HashMap<Primitive, HashMap<String, String>> cL = p.getConflictList();
				for (Map.Entry<Primitive, HashMap<String, String>> s : cL.entrySet()) {

					System.out.println("\t-" + s.getKey().getPatternName());
					System.out.println("\t\t Status: " + s.getValue().get(STATUS));
					System.out.println("\t\t Grund: " + s.getValue().get(REASON));
					System.out.println("\t\t Lšsung: " + s.getValue().get(SOLUTION));
				}
			}
		}
	}

	public void showPatternList() {
		for (Map.Entry<PrimitiveName, Primitive> e : patternList.entrySet()) {
			System.out.println(e.getKey().name());
		}
	}

	/**
	 * method to check whether a conflict ist defined between both advices
	 * 
	 * @param firstAdvice
	 *            the name of the first advice
	 * @param secondAdvice
	 *            the name of the second advice
	 * @return true if firstAdvice has a conflict with secondAdvice, false
	 *         otherwise
	 */
	public boolean isConflictBetween(String firstAdvice, String secondAdvice) {
		if (firstAdvice.equalsIgnoreCase("Delete")) firstAdvice = "DeleteElement";
		if (secondAdvice.equalsIgnoreCase("Delete")) secondAdvice = "DeleteElement";
		if (PrimitiveName.checkForEnum(firstAdvice) && PrimitiveName.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(PrimitiveName.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return true;
			else
				return false;
		}
		return false;
	}

	public String getReasonFor(String firstAdvice, String secondAdvice) {
		if (firstAdvice.equalsIgnoreCase("Delete")) firstAdvice = "DeleteElement";
		if (secondAdvice.equalsIgnoreCase("Delete")) secondAdvice = "DeleteElement";
		if (PrimitiveName.checkForEnum(firstAdvice) && PrimitiveName.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(PrimitiveName.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice)) {
				return p.getReasonForPattern(secondAdvice);
			} else
				return null;
		}
		return null;
	}

	public String getSolutionFor(String firstAdvice, String secondAdvice) {
		if (firstAdvice.equalsIgnoreCase("Delete")) firstAdvice = "DeleteElement";
		if (secondAdvice.equalsIgnoreCase("Delete")) secondAdvice = "DeleteElement";
		if (PrimitiveName.checkForEnum(firstAdvice) && PrimitiveName.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(PrimitiveName.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return p.getSolutionForPattern(secondAdvice);
			else
				return null;
		}
		return null;
	}

	public String getStatusFor(String firstAdvice, String secondAdvice) {
		if (firstAdvice.equalsIgnoreCase("Delete")) firstAdvice = "DeleteElement";
		if (secondAdvice.equalsIgnoreCase("Delete")) secondAdvice = "DeleteElement";
		if (PrimitiveName.checkForEnum(firstAdvice) && PrimitiveName.checkForEnum(secondAdvice)) {
			Primitive p = patternList.get(PrimitiveName.valueOf(firstAdvice));
			if (p.hasConflictWith(secondAdvice))
				return p.getStatusForPattern(secondAdvice);
			else
				return null;
		}
		return null;
	}

	public static synchronized ConflictMatrix getInstance() {
		if (instance == null) {
			instance = new ConflictMatrix();
		}
		return instance;
	}
	
	public void printXML(Document doc){
		 try
		    {
		       DOMSource domSource = new DOMSource(doc);
		       StringWriter writer = new StringWriter();
		       StreamResult result = new StreamResult(writer);
		       TransformerFactory tf = TransformerFactory.newInstance();
		       Transformer transformer = tf.newTransformer();
		       transformer.transform(domSource, result);
		       System.out.println(writer.toString());
		    }
		    catch(TransformerException ex)
		    {
		       ex.printStackTrace();
		    }
	}
}
