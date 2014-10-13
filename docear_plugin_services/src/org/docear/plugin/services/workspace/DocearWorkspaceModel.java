package org.docear.plugin.services.workspace;

import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearWorkspaceModel extends WorkspaceModel {

	private static final int XTRA_NODES = 3;

	public void removeProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			int index = projects.indexOf(project);
			if(index > -1) {				
				projects.remove(project);
				project.getModel().removeProjectModelListener(getTreeModelListener());
				fireProjectRemoved(project, index+XTRA_NODES);
			}
		}
	}
	
	public int getProjectIndex(AWorkspaceProject project) {
		synchronized (projects) {
			int index = XTRA_NODES;
			for (AWorkspaceProject prj : projects) {
				if(prj.equals(project)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}
	
	public AWorkspaceTreeNode getRoot() {
		if(root == null) {
			root = new DocearWorkspaceRootNode();
			root.setModel(new DefaultWorkspaceTreeModel());
		}
		return root;
	}
	
	/**********************************************************************
	 * NESTED CLASSES
	 **********************************************************************/
}
