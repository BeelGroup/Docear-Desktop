package org.docear.plugin.services.workspace;

import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearWorkspaceModel extends WorkspaceModel {

	public void addProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			if(!projects.contains(project)) {
				projects.add(project);
				project.getModel().addProjectModelListener(getProjectModelListener());
				fireProjectInserted(project);				
			}
		}
	}
	
	public void removeProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			int index = projects.indexOf(project)+1;
			if(index > -1) {				
				projects.remove(project);
				project.getModel().removeProjectModelListener(getProjectModelListener());
				fireProjectRemoved(project, index);
			}
		}
	}
	
	public int getProjectIndex(AWorkspaceProject project) {
		synchronized (projects) {
			int index = 1;
			for (AWorkspaceProject prj : projects) {
				if(prj.equals(project)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
}
