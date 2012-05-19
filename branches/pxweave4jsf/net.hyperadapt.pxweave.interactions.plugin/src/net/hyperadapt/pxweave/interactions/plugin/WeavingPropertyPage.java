package net.hyperadapt.pxweave.interactions.plugin;

import java.io.File;

import org.eclipse.core.resources.IResource;
//import org.eclipse.jdt.internal.core.JavaProject
//import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * class representing the property page for defining place of weaver-config-file
 * and pipelinestations-file (sitemap.xmp) just active, when project have nature
 * "interaction.plugin.interactionprojectnature"
 * 
 * @author danielkadner
 * 
 */
public class WeavingPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private FileFieldEditor ffe;
	private FileFieldEditor ffe2;
	private Button b;
	private Button d;
	private IResource resource = null;
	public static final String WEB_CONFIG_DATA = "interaction.plugin.WebConfigData";
	public static final String WEB_CONFIG_AUTOMATIC = "interaction.plugin.WeaverAutomatic";
	public static final String WEB_CONFIG_DEBUG = "interaction.plugin.WeaverDebug";
	public static final String WEB_CONFIG_SITEMAP = "interaction.plugin.Sitemap";

	public WeavingPropertyPage() {
	}

	@Override
	/**
	 * create gui panel
	 */
	protected Control createContents(Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		// panel.setLayout(layout);
		panel.setLayout(new FillLayout(SWT.VERTICAL));

		String path = null;
		String automatic = null;
		String debug = null;
		String sitemap = null;
		
		 
		   
		IAdaptable a = getElement();
		resource = (IResource)a.getAdapter(IResource.class);
		   
		try {
//			this.getElement()
			path = resource.getPersistentProperty(new QualifiedName("",
					WEB_CONFIG_DATA));
			sitemap = resource.getPersistentProperty(new QualifiedName("",
					WEB_CONFIG_SITEMAP));
			automatic = resource.getPersistentProperty(new QualifiedName("",
					WEB_CONFIG_AUTOMATIC));
			debug = resource.getPersistentProperty(new QualifiedName("",
					WEB_CONFIG_DEBUG));
//			debug = ((IResource) getElement()).getPersistentProperty(new QualifiedName("",
//					WEB_CONFIG_DEBUG));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ffe = new FileFieldEditor("Weaver", "Path to Weaver-Configfile", panel);
		String[] filterextensions = new String[] { "*.xml" };
		String filterpath = resource.getProject().getLocation().toString();
		ffe.setFileExtensions(filterextensions);
		ffe.setFilterPath(new File(filterpath));
		if (path != null)
			ffe.setStringValue(path);

		ffe2 = new FileFieldEditor("Sitemap", "Path to Sitemap-File", panel);
		String[] filterextensions2 = new String[] { "*.xmap" };
		String filterpath2 = resource.getProject().getLocation().toString();
		ffe2.setFileExtensions(filterextensions2);
		ffe2.setFilterPath(new File(filterpath2));
		if (sitemap != null)
			ffe2.setStringValue(sitemap);

		Group group = new Group(panel, SWT.NONE);
		group.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		b = new Button(group, SWT.CHECK);
		b.setBounds(10, 0, 400, 18);
		b.setText("Perform automatic detection while saving");
		if (automatic != null && Boolean.parseBoolean(automatic))
			b.setSelection(true);
		else
			b.setSelection(false);

		d = new Button(group, SWT.CHECK);
		d.setBounds(10, 24, 100, 18);
		d.setText("Debug-Mode");
		if (debug != null && Boolean.parseBoolean(debug))
			d.setSelection(true);
		else
			d.setSelection(false);

		return panel;
	}

	
	/**
	 * save as permanent settings if pressed ok in property page
	 */
	public boolean performOk() {
		try {
			resource.setPersistentProperty(
					new QualifiedName("", WEB_CONFIG_DATA), ffe.getStringValue());

			resource.setPersistentProperty(new QualifiedName("",
					WEB_CONFIG_SITEMAP), ffe2.getStringValue());

			if (b.getSelection())
				resource.setPersistentProperty(new QualifiedName("",
						WEB_CONFIG_AUTOMATIC), "true");
			else
				resource.setPersistentProperty(new QualifiedName("",
						WEB_CONFIG_AUTOMATIC), "false");

			if (d.getSelection())
				resource.setPersistentProperty(new QualifiedName("",
						WEB_CONFIG_DEBUG), "true");
			else
				resource.setPersistentProperty(new QualifiedName("",
						WEB_CONFIG_DEBUG), "false");
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
}
