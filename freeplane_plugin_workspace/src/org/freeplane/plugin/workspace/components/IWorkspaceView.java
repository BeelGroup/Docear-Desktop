package org.freeplane.plugin.workspace.components;

import java.awt.Component;

import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.controller.INodeTypeIconManager;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;


public interface IWorkspaceView {

	public void expandPath(TreePath treePath);

	public void collapsePath(TreePath treePath);
		
	public boolean containsComponent(Component comp);
		
	public WorkspaceTransferHandler getTransferHandler();	

	public TreePath getSelectionPath();

	public TreePath getPathForLocation(int x, int y);

	public INodeTypeIconManager getNodeTypeIconManager();
}
