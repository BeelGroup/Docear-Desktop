package org.freeplane.plugin.workspace.model.project;

import java.util.Locale;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.FolderTypeMyFilesNode;
import org.freeplane.plugin.workspace.nodes.FolderTypeProjectNode;
import org.freeplane.plugin.workspace.nodes.FolderVirtualNode;

public class DefaultProject extends  FolderTypeProjectNode {

	private static final long serialVersionUID = 1L;

	public void prepare(AWorkspaceProject project) {
		this.setModel(project.getModel());
		this.setName(WorkspaceController.resolveFile(project.getProjectHome()).getName());
		setProjectID(project.getProjectID());
		// create and load all default nodes
		super.initiateMyFile(project);
		FolderVirtualNode misc = new FolderVirtualNode(AFolderNode.FOLDER_TYPE_VIRTUAL);
		misc.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".miscnode.name"));
		project.getModel().addNodeTo(misc, this);
		//misc -> help.mm
		refresh();
	}
}
