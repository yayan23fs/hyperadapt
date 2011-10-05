package net.hyperadapt.pxweave.interactions.plugin;



import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * builder class if project has nature
 * "interaction.plugin.interactionprojectnature"
 * 
 * @author danielkadner
 * 
 */
public class InteractionBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "interaction.plugin.interactionbuilder";
	public static final String ASPECT_INTERFERENCE_ERROR = "interaction.plugin.AspectInterferenceErrorMarker";
	public static final String ASPECT_INTERFERENCE_WARNING = "interaction.plugin.AspectInterferenceWarningMarker";
	public static final String WRONG_OR_EMPTY_CONFIGFILE = "interaction.plugin.WrongOrEmptyConfigFile";
	public static boolean debug = false;
	private String base;

	/**
	 * starting point for "build"ing a project
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {

//		String data1 = null;
		URI data = null;
//		
		String sitemap = null;
		boolean performAutomatic = true;
		boolean debugMode = false;
		try {
			// configdatei speicherort
			data = new URI(getProject().getPersistentProperty(
					new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_DATA)));

			// sitemap speicherort
			sitemap = getProject().getPersistentProperty(
					new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_SITEMAP));
			PipelineStations.setFile(sitemap);

			// soll automatisch beim speichern untersucht werden?
			performAutomatic = Boolean.parseBoolean(getProject().getPersistentProperty(
					new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_AUTOMATIC)));

			// zustand des debug-modes?
			debugMode = Boolean.parseBoolean(getProject().getPersistentProperty(
					new QualifiedName("", WeavingPropertyPage.WEB_CONFIG_DEBUG)));
			DebugMode.setDebugMode(debugMode);
			debug = DebugMode.debug;

		} catch (CoreException e) {
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (debug) {
			System.out.println("InteractionBuilder building");
		}

		deleteMarkers();

		if (PipelineStations.getStations() == null
				|| PipelineStations.getStations().getLength() == 0) {
			addMarker(WRONG_OR_EMPTY_CONFIGFILE, null,
					"Please set Sitemap-File in Properties or no transformer set yet", 0, 0, 0,
					IMarker.SEVERITY_INFO);
		}

		// zerlege den Speicherortstring
		base = null;
		String configFile = null;
//		String configFile1 = null;
		if (performAutomatic) {
			if (data != null) {
				String fs = File.separator;
//				configFile1 = data1.substring(data1.lastIndexOf("/") + 1, data1.length());
//				base = data.substring(0, data.lastIndexOf("/") + 1);
				base = data.getPath().substring(0, data.getPath().lastIndexOf(fs) + 1);
				configFile = data.getPath().substring(data.getPath().lastIndexOf(fs) + 1, data.getPath().length());

				// hole alle aspectdateien aus der configdatei
				List<String> codeFiles = catchAspectFiles(base + configFile);

				// falls keine aspekte definiert waren
				if (codeFiles == null)
					codeFiles = new ArrayList<String>();

				// füge configfile in Liste der zu überwachenden Dateien hinzu
				codeFiles.add(configFile);
				// überprüfe ob eine der zu überwachenden Dateien verändert
				// wurde und gib alle zu untersuchenden zurück
				ArrayList<IFile> aspectFiles = filterFiles(codeFiles);

				// es müssen mindestens 2 dateien vorhanden sein. configfile
				// + min ein aspektfile
				if (aspectFiles != null) {
					if (aspectFiles.size() >= 2) {

						// teste Containment
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
		} else {
			addMarker(WRONG_OR_EMPTY_CONFIGFILE, null,
					"Automatic detection while saving turned off", 0, 0, 0, IMarker.SEVERITY_INFO);
		}
		return null;
	}

	/**
	 * methode to generate xpath on advice element for aspectfile
	 * 
	 * @param atc
	 *            the advice
	 * @return the xpath
	 */
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
	private int evaluateXPathandReturnLinenumber(File newFile, String xp) {
		int nr = -1;
		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			XMLLinenumberRecorder handler = new XMLLinenumberRecorder(d);
			sp.parse(newFile, handler);

			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nl = (NodeList) xpath.evaluate(xp, d, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				int line = (Integer) n.getUserData(XMLLinenumberRecorder.KEY_LINE_NO);
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
	 * delete all markers
	 */
	private void deleteMarkers() {
		try {
			getProject().deleteMarkers(ASPECT_INTERFERENCE_ERROR, false, IResource.DEPTH_INFINITE);
			getProject()
					.deleteMarkers(ASPECT_INTERFERENCE_WARNING, false, IResource.DEPTH_INFINITE);
			getProject().deleteMarkers(WRONG_OR_EMPTY_CONFIGFILE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * help method to create a marker
	 * 
	 * @param markerType
	 *            the type of the marker (see plugin.xml)
	 * @param file
	 *            the file the makres is created in
	 * @param message
	 *            the informative message for the user
	 * @param lineNumber
	 *            the number of the line where the marker has to be set
	 * @param chrStart
	 *            start row number for underlining
	 * @param chrEnd
	 *            end row number for unerlining
	 * @param severity
	 *            kind of marker (warning, error, info sign)
	 */
	public void addMarker(String markerType, IFile file, String message, int lineNumber,
			int chrStart, int chrEnd, int severity) {
		try {
			IMarker marker;
			if (file != null) {
				marker = file.createMarker(markerType);
			} else {
				marker = getProject().createMarker(markerType);
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

	/**
	 * read all given aspectfiles from configfile
	 * 
	 * @param filename
	 *            the configfile
	 * @return a list with all used aspect files
	 */
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

	/**
	 * filters if one of the codefiles has changed since last save
	 * 
	 * @param codeFiles
	 *            the aspectfiles
	 * @return when observed files has changed the observed files, else null
	 */
	private ArrayList<IFile> filterFiles(List<String> codeFiles) {

		// checke ob eine verändert wurde
		// wenn ja denn erzeuge eine sammelliste mit allen IFiles
		IResourceDelta resDelta = this.getDelta(getProject());
		if (resDelta == null)
			return null;

		DeltaVisitor visitor = new DeltaVisitor(codeFiles);

		try {
			resDelta.accept(visitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		if (visitor.isOneChanged()) {
			ArrayList<IFile> aspectFiles = new ArrayList<IFile>();
			for (String s : codeFiles) {
				File file = new File(base + s);
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IPath location = Path.fromOSString(file.getAbsolutePath());
				IFile ifile = workspace.getRoot().getFileForLocation(location);
				aspectFiles.add(ifile);
			}
			return aspectFiles;
		}
		else
			return null;
	}

	/**
	 * help method to create markers with nice messages
	 * 
	 * @param ae
	 *            the founded advice error
	 * @param aspectFiles
	 *            all aspectfiles
	 */
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

		System.out.println(file.getFullPath());

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
					message = "inhibited conflict on advice \"" + b.getName() + "\" @ line "
							+ lineNumberSecond + " from aspectdata " + b.getFilename();
				}
				if (ae.isDependent().equals(AdviceError.TRIGGERS)) {
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
				// file anstatt null
				addMarker(ASPECT_INTERFERENCE_ERROR, file, message, lineNumber, 0, 0,
						IMarker.SEVERITY_ERROR);
			else
				addMarker(ASPECT_INTERFERENCE_WARNING, file, message, lineNumber, 0, 0,
						IMarker.SEVERITY_WARNING);
		}
	}
}