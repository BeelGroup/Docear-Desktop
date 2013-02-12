package org.freeplane.plugin.workspace.mindmapmode;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MModeWorkspaceUrlManager extends MFileManager {
	//WORKSPACE - todo implement workspace/project relative uri resolving
	
	public static MModeWorkspaceUrlManager getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return (MModeWorkspaceUrlManager) modeController.getExtension(UrlManager.class);
	}
	
    protected void init() {
    	
    }
}
