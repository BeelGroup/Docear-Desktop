package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.tree.TreePath;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.NodeOpenLocationAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class DocearLibraryOpenLocation extends NodeOpenLocationAction {
	
	private static final long serialVersionUID = 1L;

	public DocearLibraryOpenLocation() {
		super();
	}
	
	@Override
	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		if(node != null && node instanceof FolderTypeLibraryNode) {
			AWorkspaceProject project = WorkspaceController.getSelectedProject(node);
			File f = URIUtils.getAbsoluteFile(((DocearWorkspaceProject)project).getProjectLibraryPath());
			setEnabled(f.exists());
		}
		else {
			super.setEnabledFor(node, selectedPaths);
		}
	}

	public void actionPerformed(ActionEvent event) {
		AWorkspaceTreeNode targetNode = getNodeFromActionEvent(event);
		if(targetNode != null && targetNode instanceof FolderTypeLibraryNode) {
			AWorkspaceProject project = getProjectFromActionEvent(event);
			this.openFolder(URIUtils.getAbsoluteFile(((DocearWorkspaceProject)project).getProjectLibraryPath()));
		}
		else {
			super.actionPerformed(event);
		}
	}

}
