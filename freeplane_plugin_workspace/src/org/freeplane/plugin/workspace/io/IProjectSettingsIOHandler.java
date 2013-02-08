package org.freeplane.plugin.workspace.io;

import java.io.IOException;
import java.io.Writer;

import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public interface IProjectSettingsIOHandler {

	
	public void loadProject(AWorkspaceProject project) throws IOException;
	public void storeProject(Writer writer, AWorkspaceProject project) throws IOException;
	public void storeProject(AWorkspaceProject project) throws IOException;
}
