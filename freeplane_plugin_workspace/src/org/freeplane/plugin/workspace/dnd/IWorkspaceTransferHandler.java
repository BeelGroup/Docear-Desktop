package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.Transferable;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public interface IWorkspaceTransferHandler {
	public void registerNodeDropHandler(Class<? extends AWorkspaceTreeNode> clzz, INodeDropHandler handler);
	
	public boolean handleDrop(AWorkspaceTreeNode targetNode, Transferable transf, int dndAction) throws NoDropHandlerFoundExeption;
}
