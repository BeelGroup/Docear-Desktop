package org.freeplane.plugin.workspace.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceMapModelExtension implements IExtension {
	private String projectID = null;
	
	public WorkspaceMapModelExtension() {
	}
	
	public AWorkspaceProject getProject() {
		return WorkspaceController.getCachedProjectByID(projectID);
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
