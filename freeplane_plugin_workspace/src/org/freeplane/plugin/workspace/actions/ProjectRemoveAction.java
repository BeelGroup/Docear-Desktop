/**
 * author: Marcel Genzmehr
 * 10.11.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

@EnabledAction(checkOnPopup = true)
public class ProjectRemoveAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.project.remove";
	private static final long serialVersionUID = -8965412338727545850L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ProjectRemoveAction() {
		super(KEY);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node.isSystem() || !node.isTransferable() || node instanceof DefaultFileNode) {
			setEnabled(false);
		}
		else{
			setEnabled();
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		//WORKSPACE - todo: dialog that asks for physical deletion as well
		int option = JOptionPane.showConfirmDialog(
				UITools.getFrame()
				,TextUtils.format("workspace.action.node.remove.confirm.text", getNodeFromActionEvent(e).getName())
				,TextUtils.getRawText("workspace.action.node.remove.confirm.title")
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE
		);
		if(option == JOptionPane.YES_OPTION) {
			AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
			WorkspaceModel model = WorkspaceController.getCurrentModel();
			model.removeProject(model.getProject(targetNode.getModel()));
			model.getRoot().getModel().requestSave();
		}
	}
}
