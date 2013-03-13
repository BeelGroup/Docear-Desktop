package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.actions.NodeOpenLocationAction;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearLibraryOpenLocation extends NodeOpenLocationAction {
	
	private static final long serialVersionUID = 1L;

	public DocearLibraryOpenLocation() {
		super();
	}
	
	public void actionPerformed(ActionEvent event) {
		AWorkspaceProject project = getProjectFromActionEvent(event);
		if(project != null && project instanceof DocearWorkspaceProject) {
			this.openFolder(URIUtils.getAbsoluteFile(((DocearWorkspaceProject)project).getProjectLibraryPath()));
		}
		else {
			super.actionPerformed(event);
		}
	}

}
