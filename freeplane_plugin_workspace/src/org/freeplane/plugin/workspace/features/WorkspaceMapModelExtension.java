package org.freeplane.plugin.workspace.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceMapModelExtension implements IExtension {
	private String projectID = null;
	private final ModeController modeController;
	
	public WorkspaceMapModelExtension() {
		this(null);
	}
	
	public WorkspaceMapModelExtension(ModeController mode) {
		modeController = mode;
	}
	
	public AWorkspaceProject getProject() {
		if(modeController == null) {
			return WorkspaceController.getCurrentModel().getProject(projectID);
		}
		return WorkspaceController.getModeExtension(modeController).getModel().getProject(projectID);
	}
	
	public void setProject(AWorkspaceProject project) {
		if(project == null) {
			projectID = null;
		}
		else {
			projectID = project.getProjectID();
		}
		
	}

}
