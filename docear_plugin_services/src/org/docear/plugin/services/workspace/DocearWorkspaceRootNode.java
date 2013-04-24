package org.docear.plugin.services.workspace;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.util.TextUtils;
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
		String text = TextUtils.getText("docear.node.root.default");
		String name = CommunicationsController.getController().getRegisteredUserName();
		if(name != null) {
			text = TextUtils.format("docear.node.root.name", name);
		}
		return text; 
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
