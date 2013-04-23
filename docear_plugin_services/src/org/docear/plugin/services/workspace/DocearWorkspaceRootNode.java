package org.docear.plugin.services.workspace;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.plugin.workspace.nodes.WorkspaceRootNode;

public class DocearWorkspaceRootNode extends WorkspaceRootNode {

	private static final long serialVersionUID = 4058474904352649840L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public String getName() {
		String name = CommunicationsController.getController().getRegisteredUserName();
		if(name != null) {
			name = name+"'s workspace";
		}
		else {
			name = "default workspace";
		}
		return name; 
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
