package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceNewProjectAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.project.new";
	
	public WorkspaceNewProjectAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		File path = WorkspaceController.resolveFile(WorkspaceController.getDefaultProjectHome());
		path = new File(path, "My Project");
		
		path.getParentFile().mkdirs();
		
		int counter = 1;
		while(path.exists()) {
			path = new File(path.getParentFile(), "My Project_"+counter++);
		}
		path.mkdir();
		
		AWorkspaceProject project = AWorkspaceProject.create(null, path.toURI());
		WorkspaceController.getCurrentModel().addProject(project);
		try {
			WorkspaceController.getCurrentModeExtension().getProjectLoader().loadProject(project);
		} catch (IOException e) {
			LogUtils.severe(e);
		}
	}

}
