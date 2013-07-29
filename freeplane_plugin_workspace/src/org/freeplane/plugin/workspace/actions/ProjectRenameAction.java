package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange = true)
public class ProjectRenameAction extends AWorkspaceAction {

	public static final String KEY = "workspace.action.project.rename";
	private static final long serialVersionUID = 1L;

	public ProjectRenameAction() {
		super(KEY);
	}

	@Override
	public void setEnabled() {
		try {
			setEnabled(WorkspaceController.getSelectedProject().getModel().getRoot() != null);
		}
		catch(NullPointerException e) {
			setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		NodeRenameAction action = new NodeRenameAction();
		WorkspaceController.getController();		
		AWorkspaceTreeNode projectNode = WorkspaceController.getSelectedProject().getModel().getRoot();
		action.actionPerformed(projectNode);
	}

}
