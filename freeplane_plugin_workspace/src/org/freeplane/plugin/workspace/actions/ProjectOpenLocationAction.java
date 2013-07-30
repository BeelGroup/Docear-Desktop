package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class ProjectOpenLocationAction extends AWorkspaceAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String KEY = "workspace.action.project.open.location";

	public ProjectOpenLocationAction() {
		super(KEY);
	}
	
	
	@Override
	public void setEnabled() {		
		setEnabled(WorkspaceController.getSelectedProject() != null);		
	}

	public void actionPerformed(ActionEvent arg0) {
		NodeOpenLocationAction action = new NodeOpenLocationAction();
		action.openFolder(URIUtils.getAbsoluteFile(WorkspaceController.getSelectedProject().getProjectHome()));		
	}

	
}
