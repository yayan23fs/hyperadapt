package net.hyperadapt.pxweave.interactions.plugin;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
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
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceError;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceToCheck;
import net.hyperadapt.pxweave.interactions.devtime.xml.AdviceTracking;
import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;
import net.hyperadapt.pxweave.interactions.devtime.xml.PipelineStations;
import net.hyperadapt.pxweave.interactions.devtime.xml.XMLLinenumberRecorder;
import net.hyperadapt.pxweave.util.JAXBHelper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * action for the manual start button in the popup menu
 * 
 * @author danielkadner
 *
 */
public class InteractionActionManual implements IObjectActionDelegate {
	
	private ISelection selection;
	private IProject project;
	private String base;

	
	public static final String BUILDER_ID = "interaction.plugin.interactionbuilder";
	public static final String ASPECT_INTERFERENCE_ERROR = "interaction.plugin.AspectInterferenceErrorMarker";
	public static final String ASPECT_INTERFERENCE_WARNING = "interaction.plugin.AspectInterferenceWarningMarker";
	public static final String WRONG_OR_EMPTY_CONFIGFILE = "interaction.plugin.WrongOrEmptyConfigFile";
	public static final boolean debug = DebugMode.debug;

	public InteractionActionManual() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					this.project = project;
					runAnalysis();
				}
			}
		}
	}
	
	private void runAnalysis(){
		// TODO Auto-generated method stub
		if (debug) {
			System.out.println("Manual InteractionBuilder Analyzation");
		}
		
		deleteMarkers();

		// hole configdateispeicherort aus den property pages
		String data = null;
		String sitemap = null;
		try {
			data = project.getPersistentProperty(new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_DATA));
			
			// sitemap speicherort
			sitemap = project.getPersistentProperty(new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_SITEMAP));
			PipelineStations.setFile(sitemap);
			
		} catch (CoreException e) {
		}

		// zerlege den Speicherortstring
		base = null;
		String configFile = null;

		if (data != null) {
			String fs = File.separator;
			configFile = data.substring(data.lastIndexOf(fs) + 1, data.length());
			base = data.substring(0, data.lastIndexOf(fs) + 1);
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
			ArrayList<IFile> aspectFiles = getAspectFiles(codeFiles);

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
						addMarker(
								WRONG_OR_EMPTY_CONFIGFILE,
								null,
								"Found XPath containing function(s). Static Analysis is not able to perform test on these advice(s).",
								0, 0, 0, IMarker.SEVERITY_INFO);

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
							AdviceToCheck tmp = ae.getFirstAdvice();
							ae.setFirstAdvice(ae.getSecondAdvice());
							ae.setSecondAdvice(tmp);
							// Marker für andere Richtung
							createMarkers(ae, aspectFiles);
						}
					}

					if (dependentErrors != null) {
						// gehe alle trigger&inhibit Konfliktpaare durch
						for (AdviceError ae : dependentErrors) {
							// Marker für eine Richtung
							createMarkers(ae, aspectFiles);
							// AdviceToCheck tmp = ae.getFirstAdvice();
							// ae.setFirstAdvice(ae.getSecondAdvice());
							// ae.setSecondAdvice(tmp);
							// // Marker für andere Richtung
							// createMarkers(ae, aspectFiles);
						}
					}
				}
				// wenn nur eine datei vorhanden ist hinweis geben,
				// dass entweder config fatei falsch, oder noch "leer"
				else {
					addMarker(WRONG_OR_EMPTY_CONFIGFILE, aspectFiles.get(0),
							"Wrong Weaver-Configdata or no Aspects set", 0, 0, 0,
							IMarker.SEVERITY_INFO);
				}
			}
		} else {
			addMarker(WRONG_OR_EMPTY_CONFIGFILE, null,
					"Please set Weaver-Configdata in Properties", 0, 0, 0,
					IMarker.SEVERITY_INFO);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;		
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}
	
	// generiere XPath auf das Advice-Element für die die Aspektdatei
	private String buildXPath(AdviceToCheck atc) {
		
		String orgXPath = atc.getPointcutXpath();
		orgXPath = orgXPath.replace("\'", "\"");
		
		String xpath = "";
		xpath += "/aspect/advicegroup/advices/";

		if (atc.getName().equalsIgnoreCase("deleteElement"))
			xpath += "delete";
		else
			xpath += atc.getName().toLowerCase();

		xpath += "[self::node()/pointcut[contains(text(),'" + orgXPath + "')]]";
		return xpath;
	}

	// wende xpath auf xml an und gib die entsprechende Zeilennummer des
	// Elements zurück
	private int evaluateXPathandReturnLinenumber(File newFile, String xp) {
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

	// Hilfsfunktion zur Ausgabe eines Documents
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
	
	

	// löschen aller Marker
	private void deleteMarkers() {
		try {
			project.deleteMarkers(ASPECT_INTERFERENCE_ERROR, false, IResource.DEPTH_INFINITE);
			project.deleteMarkers(ASPECT_INTERFERENCE_WARNING, false, IResource.DEPTH_INFINITE);
			project.deleteMarkers(WRONG_OR_EMPTY_CONFIGFILE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// Hilfsfunktion zum Setzen eines Markers inklusive Attribute
	private void addMarker(String markerType, IFile file, String message, int lineNumber, int chrStart, int chrEnd, int severity) {
		try {
			IMarker marker;
			if (file != null) {
				marker = file.createMarker(markerType);
			} else {
				marker = project.createMarker(markerType);
			}
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (chrEnd > 0) {
				marker.setAttribute(IMarker.CHAR_START, chrStart);
				marker.setAttribute(IMarker.CHAR_END, chrEnd);
			}
			if (lineNumber == -1)
				lineNumber = 1;
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// lies alle gegebenen Aspectdateien aus ConfigDatei aus, die untersucht
	// werden sollen
	//TODO sinnfrei, ¸berall entfernen ... manchmal fragt man sich echt ....
	private List<String> catchAspectFiles(String filename) {
		List<String> aspectFiles = null;
		File configFile = new File(filename);
		try {
			aspectFiles = (JAXBHelper.unmarshallConfig(configFile.toURI().toURL())).getAspectFiles().getAspectFile();
		} catch (XMLWeaverException e) {
			 e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aspectFiles;
	}

	// überprüft ob eine der codeFiles beim letzten Speichern (->Delta) geändert
	// wurde
	private ArrayList<IFile> getAspectFiles(List<String> codeFiles) {

		ArrayList<IFile> aspectFiles = new ArrayList<IFile>();
		
		for (String s : codeFiles) {
			File file = new File(base + s);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath location = Path.fromOSString(file.getAbsolutePath());
			IFile ifile = workspace.getRoot().getFileForLocation(location);
			aspectFiles.add(ifile);
//			IFile file = project.getFile(s);
//			aspectFiles.add(file);
		}

		if (aspectFiles.size() != 0)
			return aspectFiles;
		else
			return null;
	}

	private void createMarkers(AdviceError ae, ArrayList<IFile> aspectFiles) {
		AdviceToCheck a = ae.getFirstAdvice();
		AdviceToCheck b = ae.getSecondAdvice();
		String firstAspectFilename = a.getPathName();
		String firstXpath = buildXPath(a);
		boolean isError = ae.getIsError();

		IFile file = null;
		for (IFile f : aspectFiles) {
			if (firstAspectFilename.contains(f.getName()))
				file = f;
		}

		String message = "";

		// hole Liniennummer für marker
		int lineNumber = evaluateXPathandReturnLinenumber(new File(firstAspectFilename), firstXpath);

		if (b != null) {
			String secondAspectFilename = b.getPathName();
			String secondXPath = buildXPath(b);

			// hole Liniennummer für Marker

			int lineNumberSecond = evaluateXPathandReturnLinenumber(new File(secondAspectFilename),
					secondXPath);

			// nette message generieren
			if (ae.isDependent() == null) {
				message = "Conflict with advice \"" + b.getName() + "\" on line "
						+ lineNumberSecond + " in Aspectfile " + b.getFilename();
			} else {
				if (ae.isDependent().equals(AdviceError.INHIBITS)) {
					// if (ae.getSecondAdvice() == null)
					// message = "potential inhibited conflict";
					// else
					message = "inhibited conflict on advice \"" + b.getName() + "\" @ line "
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
		if (b == null) {
			addMarker(ASPECT_INTERFERENCE_WARNING, null, message, 0, 0, 0, IMarker.SEVERITY_INFO);
		} else {
			if (isError)
				addMarker(ASPECT_INTERFERENCE_ERROR, file, message, lineNumber, 0, 0,
						IMarker.SEVERITY_ERROR);
			else
				addMarker(ASPECT_INTERFERENCE_WARNING, file, message, lineNumber, 0, 0,
						IMarker.SEVERITY_WARNING);
		}
	}

}
