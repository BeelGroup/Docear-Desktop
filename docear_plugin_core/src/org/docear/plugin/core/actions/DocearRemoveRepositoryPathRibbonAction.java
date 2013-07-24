package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.tree.TreePath;

import org.docear.plugin.core.workspace.actions.DocearRemoveRepositoryPathAction;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class DocearRemoveRepositoryPathRibbonAction extends AWorkspaceAction {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY = "workspace.action.ribbon.remove.repository";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearRemoveRepositoryPathRibbonAction() {
		super(KEY);
	}	
	
	@Override
	public void setEnabled() {
		try {
    		TreePath path = WorkspaceController.getCurrentModeExtension().getView().getSelectionPath();    
    		Object o = path.getLastPathComponent();
    		setEnabled(WorkspaceController.getCurrentProject() != null && path.getLastPathComponent() instanceof LiteratureRepositoryPathNode);    		
		}
		catch(Exception e) {
			setEnabled(false);
		}
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		DocearRemoveRepositoryPathAction action = new DocearRemoveRepositoryPathAction();
		
		TreePath path = WorkspaceController.getCurrentModeExtension().getView().getSelectionPath();
		Object o = path.getLastPathComponent();
		action.actionPerformed(WorkspaceController.getCurrentProject(), (AWorkspaceTreeNode) path.getLastPathComponent());
		
	}
}
