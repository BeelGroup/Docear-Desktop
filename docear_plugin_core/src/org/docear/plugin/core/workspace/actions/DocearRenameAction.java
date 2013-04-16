/**
 * author: Marcel Genzmehr
 * 22.11.2011
 */
package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;

@CheckEnableOnPopup
public class DocearRenameAction extends AWorkspaceAction {

	private static final long serialVersionUID = 1L;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearRenameAction() {
		super("workspace.action.docear.node.rename");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setEnabledFor(AWorkspaceTreeNode node) {
		if(node instanceof IMutableLinkNode) {
			setEnabled(true);	
		}
		else{
			setEnabled(false);
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void actionPerformed(ActionEvent e) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(e);
	
		if(targetNode instanceof IMutableLinkNode) {
			String oldName = targetNode.getName();		
			String newName = JOptionPane.showInputDialog(UITools.getFrame(),
					TextUtils.getText("docear.confirm.action.rename.mindmap.text"), oldName);

			if (newName != null) {
				if(((IMutableLinkNode) targetNode).changeName(newName, false)) {
					targetNode.getModel().requestSave();
				}
				else {
					JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getContentPane(),
							TextUtils.getText("docear.error.rename.mindmap.text"), TextUtils.getText("docear.error.rename.mindmap.title"), 
							JOptionPane.ERROR_MESSAGE);
				}
				
			}
		}
		
	}
}
