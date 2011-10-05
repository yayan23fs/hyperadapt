package net.hyperadapt.pxweave.interactions.devtime.xml;



import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
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


import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.interactions.devtime.triggersinhibits.TriggersAndInhibition;
import net.hyperadapt.pxweave.util.JAXBHelper;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MainStartAnalysisOffline {

	public static void main(String[] args) {
		String data = "testdata/weaverConfig.xml";
		String sitemap = "testdata/sitemap.xmap";
		boolean performAutomatic = true;
		boolean debug = true;
		PipelineStations.setFile(sitemap);

		if (debug) {
			System.out.println("InteractionBuilder building");
		}

		if (PipelineStations.getStations().getLength() == 0) {
			System.out.println("Please set Sitemap-File in Properties or no transformer set yet");
		}

		// zerlege den Speicherortstring
		String base = null;
		String configFile = null;
		if (performAutomatic) {
			if (data != null) {
				configFile = data.substring(data.lastIndexOf("/") + 1, data.length());
				base = data.substring(0, data.lastIndexOf("/") + 1);
				// } else
				// throw new CoreException(null);

				// hole alle aspectdateien aus der configdatei
				List<String> codeFiles = catchAspectFiles(base + configFile);

				// falls keine aspekte definiert waren
				if (codeFiles == null)
					codeFiles = new ArrayList<String>();
				// füge configfile in Liste der zu überwachenden Dateien hinzu
				codeFiles.add(configFile);

				// überprüfe ob eine der zu überwachenden Dateien verändert
				// wurde
				// und
				// gib alle zu untersuchenden zurück
				ArrayList<File> aspectFiles = new ArrayList<File>();
				for (String s : codeFiles) {
					aspectFiles.add(new File(s));
				}

				// es müssen mindestens 2 dateien vorhanden sein. configfile
				// + min ein aspektfile
				if (aspectFiles != null) {
					if (aspectFiles.size() >= 2) {

						// teste Containment
						// todo: INTERFERENCE auch einbinden!!!
						List<String> nameOfAspectFiles = catchAspectFiles(base + configFile);
						for (int i = 0; i < nameOfAspectFiles.size(); i++) {
							nameOfAspectFiles.set(i, base + nameOfAspectFiles.get(i));
						}
						AdviceTracking container = new AdviceTracking(nameOfAspectFiles);

						if (container.containsAdviceWithFunctions())
							System.out.println("Info: Found advices with functions");

						// Liste aller advices die eine Wechselwirkung haben
						List<AdviceError> adviceInteractions = container.getErrors();

						// hole alle hemmenden und auslösenden AdviceErrors
						TriggersAndInhibition tai = new TriggersAndInhibition(
								container.getAllAdvices(), adviceInteractions);
						List<AdviceError> dependentErrors = tai.getAllAfterEffects();

						List<AdviceError> allErrors = new ArrayList<AdviceError>();
						if (adviceInteractions != null)
							allErrors.addAll(adviceInteractions);
						// if (dependentErrors != null)
						// allErrors.addAll(dependentErrors);

						if (allErrors != null) {
							// gehe alle gefunden Konfliktpaare durch
							for (AdviceError ae : allErrors) {
								// Marker für eine Richtung
								createMarkers(ae, aspectFiles);
								if (ae.getSecondAdvice() != null) {
									AdviceToCheck tmp = ae.getFirstAdvice();
									ae.setFirstAdvice(ae.getSecondAdvice());
									ae.setSecondAdvice(tmp);
									// Marker für andere Richtung
									createMarkers(ae, aspectFiles);
								}
							}

						}
						if (dependentErrors != null) {
							// gehe alle trigger&inhibit Konfliktpaare durch
							for (AdviceError ae : dependentErrors) {
								// Marker für eine Richtung
								createMarkers(ae, aspectFiles);
								// if (ae.getSecondAdvice() != null) {
								// AdviceToCheck tmp = ae.getFirstAdvice();
								// ae.setFirstAdvice(ae.getSecondAdvice());
								// ae.setSecondAdvice(tmp);
								// // Marker für andere Richtung
								// createMarkers(ae, aspectFiles);
								// }
							}
						}
					}
					// wenn nur eine datei vorhanden ist hinweis geben,
					// dass entweder config fatei falsch, oder noch "leer"
					else {
						System.out.println("Wrong Weaver-Configdata or no Aspects set");
					}
				}
//			} else {
//				System.out.println("Please set Weaver-Configdata in Properties");
			}
		} else {
			System.out.println("Automatic detection while saving turned off");
		}

	}

	/**
	 * methode to generate xpath on advice element for aspectfile
	 * 
	 * @param atc
	 *            the advice
	 * @return the xpath
	 */
	private static String buildXPath(AdviceToCheck atc) {

		String orgXPath = atc.getPointcutXpath();
		orgXPath = orgXPath.replace("\'", "\"");

		// System.out.println("ORG "+orgXPath);

		String xpath = "";
		xpath += "/aspect/advicegroup/advices/";

		if (atc.getName().equalsIgnoreCase("deleteElement"))
			xpath += "delete";
		else
			xpath += atc.getName().toLowerCase();

		xpath += "[self::node()/pointcut[contains(text(),'" + orgXPath + "')]]";
		return xpath;
	}

	/**
	 * apply xpath on xml and return corresponding linenumber of the element
	 * (necessary to set markers on the correct position)
	 * 
	 * @param the
	 *            xml file as file
	 * @param the
	 *            xpath as string
	 * @return the linenumber for the result of the xpath as integer
	 */
	private static int evaluateXPathandReturnLinenumber(File newFile, String xp) {
		int nr = -1;
		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			XMLLinenumberRecorder handler = new XMLLinenumberRecorder(d);
			sp.parse(newFile, handler);

			// printXML(d);

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xpath.evaluate(xp, d, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				int line = (Integer) n.getUserData(XMLLinenumberRecorder.KEY_LINE_NO);
				// int col = (Integer)
				// n.getUserData(LocationRecordingHandler.KEY_COLUMN_NO);
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

	/**
	 * help method to print xml document on console
	 * 
	 * @param doc
	 *            the document to be written on console
	 */
	public void printXML(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			System.out.println(writer.toString());
		} catch (TransformerException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * read all given aspectfiles from configfile
	 * 
	 * @param filename
	 *            the configfile
	 * @return a list with all used aspect files
	 */
	private static List<String> catchAspectFiles(String filename) {
		File f = new File(filename);
		List<String> aspectFiles = null;
		try {
			aspectFiles = (JAXBHelper.unmarshallConfig(f.toURI().toURL())).getAspectFiles().getAspectFile();
		} catch (XMLWeaverException e) {
			// e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aspectFiles;
	}

	/**
	 * help method to create markers with nice messages
	 * 
	 * @param ae
	 *            the founded advice error
	 * @param aspectFiles
	 *            alls aspectfiles
	 */
	private static void createMarkers(AdviceError ae, ArrayList<File> aspectFiles) {
		AdviceToCheck a = ae.getFirstAdvice();
		AdviceToCheck b = ae.getSecondAdvice();
		String firstAspectFilename = a.getPathName();
		String firstXpath = buildXPath(a);
		boolean isError = ae.getIsError();

		File file = null;
		for (File f : aspectFiles) {
			if (firstAspectFilename.contains(f.getName()))
				file = f;
		}

		String message = "";
		// nette message generieren

		int lineNumber = evaluateXPathandReturnLinenumber(new File(firstAspectFilename), firstXpath);

		if (b != null) {
			String secondAspectFilename = b.getPathName();
			String secondXPath = buildXPath(b);

			// hole Liniennummer für marker
			// könnten noch mehrere Rückgabenodes sein!!!

			int lineNumberSecond = evaluateXPathandReturnLinenumber(new File(secondAspectFilename),
					secondXPath);

			if (ae.isDependent() == null) {
				message = "Conflict with advice \"" + b.getName() + "\" in Aspectfile "
						+ b.getFilename();
			} else {
				if (ae.isDependent().equals(AdviceError.INHIBITS)) {
					// if (ae.getSecondAdvice() == null)
					// message = "potential inhibited conflict";
					// else
					message = "inhibited conflict on advice \"" + b.getName() + "\" @line "
							+ lineNumberSecond + " from aspectdata " + b.getFilename();
				}
				if (ae.isDependent().equals(AdviceError.TRIGGERS)) {
					// if (ae.getSecondAdvice() == null)
					// message = "potential triggered conflict";
					// else
					message = "triggered conflict on advice \"" + b.getName() + "\" @line "
							+ lineNumberSecond + " from aspectdata " + b.getFilename();
				}
				message += ae.getMessage();
			}
		} else {
			message += ae.getMessage();
		}

		// marker in file an entsprechender Stelle setzen (abhängig von Status
		// des Konflikts)
		if (isError)
			System.out.println("Error @file:" + file.getName() + " @advice:" + a.getName()
					+ " @line:" + lineNumber + " with message: " + message);
		else
			System.out.println("Warning @file:" + file.getName() + " @advice:" + a.getName()
					+ " @line:" + lineNumber + " with message: " + message);
	}
}