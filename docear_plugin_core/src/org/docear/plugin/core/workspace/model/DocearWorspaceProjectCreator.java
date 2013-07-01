package org.docear.plugin.core.workspace.model;

import java.net.URI;

import org.freeplane.plugin.workspace.model.project.DefaultWorkspaceProjectCreator;

public class DocearWorspaceProjectCreator extends DefaultWorkspaceProjectCreator {
	
	public DocearWorkspaceProject newProject(String projectID, URI projectHome) {
		return new DocearWorkspaceProject(projectID, projectHome);
	}
}
