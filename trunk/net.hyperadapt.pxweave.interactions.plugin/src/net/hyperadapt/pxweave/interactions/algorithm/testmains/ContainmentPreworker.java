package net.hyperadapt.pxweave.interactions.algorithm.testmains;



import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.config.ast.WeaverConfiguration;
import net.hyperadapt.pxweave.interactions.devtime.patternmatrix.ConflictMatrix;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceTracking;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;
import net.hyperadapt.pxweave.interactions.devtime.xml.XMLLinenumberRecorder;
import net.hyperadapt.pxweave.util.JAXBHelper;

public class ContainmentPreworker {

	public static boolean debug = DebugMode.debug;
	private WeaverConfiguration wc;
	private List<String> nameofaspectfiles;
//	private HashMap<String, Aspect> aspects;
//	private List<Aspect> allAspects;
	private ConflictMatrix cm;
	private AdviceTracking container;

	public ContainmentPreworker(String filename) {
		try {
			cm = ConflictMatrix.getInstance();
			//TODO check file loading
			File configFile = new File(filename);
//			aspects = new HashMap<String, Aspect>();
			try {
				wc = JAXBHelper.unmarshallConfig(configFile.toURI().toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			nameofaspectfiles = wc.getAspectFiles().getAspectFile();
			container = new AdviceTracking(nameofaspectfiles);

			List<AdviceError> adviceInteractions = container.getErrors();

			for (AdviceError atcs : adviceInteractions) {

				AdviceToCheck atc1 = atcs.getFirstAdvice();
				AdviceToCheck atc2 = atcs.getSecondAdvice();
				
					
				String aspectFilename = atc1.getPathName();
				String xpath = buildXPath(atc1);
					
//				hole Liniennummer für marker	
				//könnten noch mehrere Rückgabenodes sein
				int nr = evaluateXPathandReturnLinenumber(new File(aspectFilename), xpath);
				
				System.out.println(aspectFilename + "    " + xpath + "  " + nr + "   !");
				
				aspectFilename = atc2.getPathName();
				xpath = buildXPath(atc2);
				
//					hole Liniennummer für marker	
				//könnten noch mehrere Rückgabenodes sein
				nr = evaluateXPathandReturnLinenumber(new File(aspectFilename), xpath);
				
				System.out.println(aspectFilename + "    " + xpath + "  " + nr + "   !");


			}

			// setze marker

		} catch (XMLWeaverException e) {
			e.printStackTrace();
		}
	}

	private String buildXPath(AdviceToCheck atc) {
		String xpath = "";
		xpath += "/aspect/advicegroup/advices/";

		if (atc.getName().equalsIgnoreCase("deleteElement")) xpath += "delete";
		else xpath += atc.getName().toLowerCase();
		
		xpath += "/pointcut[contains(text(),'"+ atc.getPointcutXpath() +"')]";
		
		return xpath;
	}

	public int evaluateXPathandReturnLinenumber(File newFile, String xp) {
		int nr = -1;

		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			XMLLinenumberRecorder handler = new XMLLinenumberRecorder(d);
			sp.parse(newFile, handler);
			
			printXML(d);

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xpath.evaluate(xp, d, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				int line = (Integer) n.getUserData(XMLLinenumberRecorder.KEY_LINE_NO);
//				int col = (Integer) n.getUserData(LocationRecordingHandler.KEY_COLUMN_NO);
				nr = line;
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return nr;
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
	
	public void printIt() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		List<AdviceToCheck> allA = container.getAllAdvices();
		if (debug) {
			for (AdviceToCheck a : allA) {
				System.out.println("Advice " + a.getName() + " aus Aspect " + a.getAspect().getName() + " (" + a.getPathName() + ")" + " XPath: " + a.getXPath());
			}
		}

		List<AdviceError> allE = container.getErrors();
		if (debug) {
			for (AdviceError a : allE) {
				String p = a.getFirstAdvice().getName();
				String q = a.getSecondAdvice().getName();
				System.out.println("Problem zwischen " + p + " und " + q + ": ");
				if (cm.isConflictBetween(p, q)) {
					System.out.println("\t\t Ursache: " + cm.getReasonFor(p, q));
					System.out.println("\t\t Lösung: " + cm.getSolutionFor(p, q));
				} else {
					System.out.println("\t\t aber das ist kein problem, die behindern sich nicht");
				}
			}
		}
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println();
	}
	
	public List<AdviceError> getErrors(){
		return container.getErrors();
	}

	public List<AdviceToCheck> getAllAdvices(){
		return container.getAllAdvices();
	}
	
	public static void main(String[] args){
		TriggerInhibitTest.main(args);
	}
}
