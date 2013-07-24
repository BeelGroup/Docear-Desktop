package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class DocearRemoveRepositoryPathAction extends AWorkspaceAction {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY = "workspace.action.node.remove.repository";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearRemoveRepositoryPathAction() {
		super(KEY);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabled() {
		setEnabled(DocearWorkspaceProject.isCompatible(WorkspaceController.getCurrentProject()));
	}
	
	@Override
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node instanceof LiteratureRepositoryPathNode) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(AWorkspaceProject project, AWorkspaceTreeNode... targetNodes) {
		if(DocearWorkspaceProject.isCompatible(project)) {
			FolderTypeLiteratureRepositoryNode litRepoNode = project.getExtensions(FolderTypeLiteratureRepositoryNode.class);
			if(litRepoNode == null) {
				return;
			}
			
			if(targetNodes == null || targetNodes.length == 0) {
				return;
			}
			String confirmText = "";
			if(targetNodes.length > 1) {
				confirmText = "";
			}
			else {
				confirmText = targetNodes[0].getName();
			}
			int option = JOptionPane.showConfirmDialog(
				UITools.getFrame()
				,TextUtils.format("workspace.action.repository.remove.confirm.text", confirmText)
				,TextUtils.getRawText("workspace.action.node.remove.confirm.title")
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE
			);
			if(option == JOptionPane.YES_OPTION) {			
				for (AWorkspaceTreeNode targetNode : targetNodes) {
					if(targetNode instanceof LiteratureRepositoryPathNode) {
						project.getModel().removeNodeFromParent(targetNode);
					}
				}
				
				litRepoNode.refresh();
				
				IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
				if(view != null) {
					view.refreshView();
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
    		AWorkspaceProject project = WorkspaceController.getCurrentProject();
    		AWorkspaceTreeNode[] targetNodes = getSelectedNodes(e);
    		
    		actionPerformed(project, targetNodes);
		}
		catch(Exception ex) {
			LogUtils.warn("DocearRemoveRepositoryPathAction.actionPerformed(): " + ex.getMessage());
		}
		

	}
}
