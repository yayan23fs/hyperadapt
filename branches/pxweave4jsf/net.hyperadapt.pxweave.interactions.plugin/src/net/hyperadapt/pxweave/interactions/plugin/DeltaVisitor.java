package net.hyperadapt.pxweave.interactions.plugin;


import java.util.ArrayList;
import java.util.List;

import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

/**
 * class to check if content of a file has changed since last save
 * 
 * @author danielkadner
 *
 */
public class DeltaVisitor implements IResourceDeltaVisitor {

	private boolean configParamsChanged;
	private ArrayList<IFile> configParamsFiles;
	private List<String> codeFiles;
	private boolean debug = DebugMode.debug;

	// public static String CONFIG_PARAM_FILENAME = "ConfigParams.java";

	/**
	 * the constructor
	 */
	public DeltaVisitor(List<String> codeFiles) {
		this.codeFiles = codeFiles;
		configParamsChanged = false;
		configParamsFiles = new ArrayList<IFile>();
	}

	/**
	 * collect all files of the observed codefiles who has changed since last save 
	 */
	public boolean visit(IResourceDelta delta) {
		configParamsFiles = new ArrayList<IFile>();
		configParamsChanged = false;
		
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			// handle added resource
			break;
		case IResourceDelta.REMOVED:
			// handle removed resource
			break;
		case IResourceDelta.CHANGED:
			// handle changed resource
			break;
		}

		String resName = delta.getResource().getName();
//		 System.out.println("projectrelativepath "+delta.getResource().getProjectRelativePath());
		for (String configParamFilename : codeFiles) {
			if (debug) {
				System.out.println("derzeitige Datei "+configParamFilename);
			}
			
			if (!configParamsChanged) {
				configParamsChanged = resName.equalsIgnoreCase(configParamFilename);
				if (debug) {
					System.out.println("\tist was verändert "+configParamsChanged);
				}
			}
			if (debug) {
				System.out.println("dateiname " + delta.getResource().getProject().getFile(configParamFilename).getName());
			}
			configParamsFiles.add(delta.getResource().getProject().getFile(configParamFilename));
		}
		
		return true;
	}

	public boolean isOneChanged() {
		return configParamsChanged;
	}

	/**
	 * if one of the observed files has changed return all as IFile
	 * @return a list of all observed files as ifile if one has changed
	 */
	public ArrayList<IFile> getEffectedFiles() {
		if (configParamsFiles.size() == 0 && !configParamsChanged)
			return null;
		else {
			if (debug) {
				System.out.println("Alle:");
				for (IFile f : configParamsFiles){
					System.out.print(f.getName() + " ");
				}
				System.out.println();
			}
			
			return configParamsFiles;
		}
	}

}
