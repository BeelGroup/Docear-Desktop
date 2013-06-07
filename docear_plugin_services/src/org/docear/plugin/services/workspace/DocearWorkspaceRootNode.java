package org.docear.plugin.services.workspace;

import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.action.DocearUserLoginAction;
import org.docear.plugin.services.features.user.action.DocearUserRegistrationAction;
import org.docear.plugin.services.features.user.action.DocearUserServicesAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.actions.NodeRefreshAction;
import org.freeplane.plugin.workspace.actions.WorkspaceImportProjectAction;
import org.freeplane.plugin.workspace.actions.WorkspaceNewProjectAction;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.nodes.WorkspaceRootNode;

public class DocearWorkspaceRootNode extends WorkspaceRootNode {

	private static final long serialVersionUID = 4058474904352649840L;
	private static WorkspacePopupMenu popupMenu;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public String getName() {
		String text = TextUtils.getText("docear.node.root.default");
		String name = DocearUserController.getActiveUser().getUsername();
		if(name != null) {
			text = TextUtils.format("docear.node.root.name", name);
		}
		return text; 
	}
	
	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspaceNewProjectAction.KEY,
					WorkspaceImportProjectAction.KEY,
					WorkspacePopupMenuBuilder.SEPARATOR,
					DocearUserLoginAction.KEY,
					DocearUserRegistrationAction.KEY,
					DocearUserServicesAction.KEY,
					WorkspacePopupMenuBuilder.SEPARATOR,
					NodeRefreshAction.KEY					
			});
		}
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
