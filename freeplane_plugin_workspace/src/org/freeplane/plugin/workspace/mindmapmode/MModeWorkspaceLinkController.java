package org.freeplane.plugin.workspace.mindmapmode;

import java.net.URI;

import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
//WORKSPACE - todo register as LinkController but beware of addAction problems
public class MModeWorkspaceLinkController extends MLinkController {
	
	private static MModeWorkspaceLinkController self;

	protected void init() {
		
	}
	
	public static MModeWorkspaceLinkController getController() {
//		final ModeController modeController = Controller.getCurrentModeController();
//		return (MModeWorkspaceLinkController) getController(modeController);
		if(self == null) {
			self = new MModeWorkspaceLinkController();
		}
		return self;
	}
	
	public URI getProjectRelativeURI(AWorkspaceProject project, URI uri) {
		//WORKSPACE - todo: implement 
		//project.getProjectHome()
		return uri;
	}
}
