package net.hyperadapt.pxweave.interactions.plugin;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * nature to add interaction builder to the project
 * 
 * @author danielkadner
 *
 */
public class InteractionProjectNature implements IProjectNature {

	public static final String NATURE_ID = "interaction.plugin.interactionprojectnature";

	private IProject project;

	@Override
	/**
	 * add builder
	 */
	public void configure() throws CoreException {
//		System.out.println("hinzugefügt");
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(InteractionBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(InteractionBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	@Override
	/**
	 * del builder
	 */
	public void deconfigure() throws CoreException {
		System.out.println("weggenommen");
		deleteMarkers();
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(InteractionBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				return;
			}
		}
	}
	
	/**
	 * delete markers is builder is delete
	 */
	private void deleteMarkers() {
		try {
			getProject().deleteMarkers(InteractionBuilder.ASPECT_INTERFERENCE_ERROR, false, IResource.DEPTH_INFINITE);
			getProject().deleteMarkers(InteractionBuilder.ASPECT_INTERFERENCE_WARNING, false, IResource.DEPTH_INFINITE);
			getProject().deleteMarkers(InteractionBuilder.WRONG_OR_EMPTY_CONFIGFILE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
