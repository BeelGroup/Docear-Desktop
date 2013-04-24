package org.docear.plugin.services.workspace;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.WorkspaceModelException;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearWorkspaceModel extends WorkspaceModel {

	private static final int XTRA_NODES = 2;

	public void addProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			if(!projects.contains(project)) {
				projects.add(project);
				project.getModel().addProjectModelListener(getProjectModelListener());
				fireProjectInserted(project);				
			}
		}
	}
	
	public void removeProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			int index = projects.indexOf(project);
			if(index > -1) {				
				projects.remove(project);
				project.getModel().removeProjectModelListener(getProjectModelListener());
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
	
	/**
	 * 
	 * @author mag
	 *
	 */
	private final class DefaultWorkspaceTreeModel implements WorkspaceTreeModel {
		public void removeNodeFromParent(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			
			if(getRoot().equals(node.getParent())) {
				//forbidden: use removeProject
			}
			else {
				node.getModel().removeNodeFromParent(node);
			}
		}

		public void removeAllElements(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			
			if(getRoot().equals(node)) {
				//forbidden: use removeProject
			}
			else {
				node.getModel().removeAllElements(node);
			}
		}

		public void reload(AWorkspaceTreeNode node) {
			if(node == null) {
				return;
			}
			resetClipboard();
			if(getRoot().equals(node)) {
				for (AWorkspaceProject project : getProjects()) {
					project.getModel().reload();
				}
			}
			else {
				node.getModel().reload(node);
			}			
		}

		public void nodeMoved(AWorkspaceTreeNode node, Object from, Object to) {
			if(getRoot().equals(node)) {
				//forbidden for root node
			}
			else {
				node.getModel().nodeMoved(node, from, to);
			}
		}

		public boolean insertNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, int atPos, boolean allowRenaming) {
			if(getRoot().equals(targetNode)) {
				//forbidden: only allowed via addProject()
				return false;
			}
			else {
				return targetNode.getModel().insertNodeTo(node, targetNode, atPos, allowRenaming);
			}
		}

		public AWorkspaceTreeNode getRoot() {
			return DocearWorkspaceModel.this.getRoot();
		}

		public boolean containsNode(String key) {
			for (AWorkspaceProject project : getProjects()) {
				if(project.getModel().containsNode(key)) {
					return true;
				}
			}
			return false;
		}

		public AWorkspaceTreeNode getNode(String key) {
			AWorkspaceTreeNode node = null;
			for (AWorkspaceProject project : getProjects()) {
				node = project.getModel().getNode(key);
				if(node != null) {
					break; 
				}
			}
			return node;
		}

		public void cutNodeFromParent(AWorkspaceTreeNode node) {
			if(getRoot().equals(node.getParent())) {
				//forbidden: use removeProject
			}
			else {
				node.getModel().cutNodeFromParent(node);
			}
			
		}

		public void changeNodeName(AWorkspaceTreeNode node, String newName) throws WorkspaceModelException {
			if(getRoot().equals(node)) {
				String oldName = node.getName();
				node.setName(newName);				
				fireWorkspaceRenamed(oldName, newName);
			}
			else {
				node.getModel().changeNodeName(node, newName);
			}			
		}

		public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode, boolean allowRenaming) {
			return insertNodeTo(node, targetNode, targetNode.getChildCount(), allowRenaming);
		}

		public boolean addNodeTo(AWorkspaceTreeNode node, AWorkspaceTreeNode targetNode) {
			return addNodeTo(node, targetNode, true);
		}

		public void requestSave() {
			WorkspaceController.save();
		}

		public void nodeChanged(AWorkspaceTreeNode node, Object oldValue, Object newValue) {
			if(getRoot().equals(node)) {
				//should not happen
			}
			else {
				node.getModel().nodeChanged(node, oldValue, newValue);
			}			
		}
	}
}
