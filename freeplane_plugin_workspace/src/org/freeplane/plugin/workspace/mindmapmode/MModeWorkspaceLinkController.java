package org.freeplane.plugin.workspace.mindmapmode;

import java.io.File;
import java.net.URI;

import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.plugin.workspace.WorkspaceController;
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
	
	public static URI extendPath(URI base, String child) {
		return new File(WorkspaceController.resolveFile(base), child).toURI();
	}
}
