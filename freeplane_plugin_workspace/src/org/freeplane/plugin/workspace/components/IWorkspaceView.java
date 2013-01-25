package org.freeplane.plugin.workspace.components;

import java.awt.Component;

import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.controller.INodeTypeIconManager;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.model.WorkspaceModel;


public interface IWorkspaceView {

	public void expandPath(TreePath treePath);

	public void collapsePath(TreePath treePath);
	
	public void setModel(WorkspaceModel model);
	
	public WorkspaceTransferHandler getTransferHandler();

	public boolean containsComponent(Component comp);

	public TreePath getSelectionPath();

	public TreePath getPathForLocation(int x, int y);

	public INodeTypeIconManager getNodeTypeIconManager();
}
