package org.freeplane.plugin.workspace.model.project;

import java.util.EventListener;


public interface IProjectModelListener extends EventListener {

	public void treeNodesChanged(ProjectModelEvent event);

	public void treeNodesInserted(ProjectModelEvent event);

	public void treeNodesRemoved(ProjectModelEvent event);

	public void treeStructureChanged(ProjectModelEvent event);
	
}
