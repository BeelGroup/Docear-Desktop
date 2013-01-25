package org.freeplane.plugin.workspace.controller;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.model.WorkspaceModel;

public abstract class AWorkspaceModeExtension implements IExtension {
	private final IOController workspaceIOController = new IOController();
	
	public AWorkspaceModeExtension(ModeController modeController) {
	}

	public abstract WorkspaceModel getModel();
	public abstract IWorkspaceView getView();

	public IOController getIOController() {
		return workspaceIOController;
	}

	public abstract FileReadManager getFileTypeManager();

}
