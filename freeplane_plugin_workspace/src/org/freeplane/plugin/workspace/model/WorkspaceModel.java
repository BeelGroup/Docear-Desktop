package org.freeplane.plugin.workspace.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectModelListener;
import org.freeplane.plugin.workspace.model.project.ProjectModel;
import org.freeplane.plugin.workspace.model.project.ProjectModelEvent;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRootNode;

public abstract class WorkspaceModel implements TreeModel {	
	
	private List<AWorkspaceProject> projects = new ArrayList<AWorkspaceProject>();
	private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	
	private WorkspaceRootNode root;
	private IProjectModelListener projectModelListener;
	
	public void addProject(AWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (projects) {
			if(!projects.contains(project)) {
				projects.add(project);
				project.getModel().addProjectModelListener(getProjectModelListener());
				fireProjectInserted(project, projects.size()-1);				
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
				fireProjectRemoved(project, index);
			}
		}
	}
	
	private IProjectModelListener getProjectModelListener() {
		if(projectModelListener == null) {
			projectModelListener = new IProjectModelListener() {
				
				public void treeStructureChanged(ProjectModelEvent event) {
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeStructureChanged(event);
						}
					}
				}
				
				public void treeNodesRemoved(ProjectModelEvent event) {
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesRemoved(event);
						}
					}
				}
				
				public void treeNodesInserted(ProjectModelEvent event) {
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesInserted(event);
						}
					}
				}
				
				public void treeNodesChanged(ProjectModelEvent event) {
					synchronized (listeners) {
						for (int i = listeners.size()-1; i >= 0; i--) {
							listeners.get(i).treeNodesChanged(event);
						}
					}
				}
			};
		}
		return projectModelListener;
	}

	
	private void fireProjectRemoved(AWorkspaceProject project, int index) {
		synchronized (listeners) {
			TreeModelEvent event = new TreeModelEvent(this, new Object[]{getRoot()}, new int[]{index}, new Object[]{project.getModel().getRoot()});
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).treeNodesRemoved(event);
			}
		}			
	}

	private void fireProjectInserted(AWorkspaceProject project, int index) {
		synchronized (listeners) {
			TreeModelEvent event = new TreeModelEvent(this, new Object[]{getRoot()}, new int[]{index}, new Object[]{project.getModel().getRoot()});
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).treeNodesInserted(event);
			}
		}		
	}
	
	private void fireWorkspaceRenamed(String oldName, String newName) {
		synchronized (listeners) {
			TreeModelEvent event = new TreeModelEvent(this, new Object[]{getRoot()}, new int[]{}, new Object[]{});
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).treeNodesChanged(event);
			}
		}		
	}

	public AWorkspaceTreeNode getRoot() {
		if(root == null) {
			root = new WorkspaceRootNode();
			root.setModel(new DefaultWorkspaceTreeModel());
		}
		return root;
	}

	public Object getChild(Object parent, int index) {
		if(parent == getRoot()) {
			synchronized (projects) {
				 AWorkspaceTreeNode node = projects.get(index).getModel().getRoot();
				 if(node == null) {
					 node = new ProjectRootNode();
					 node.setName("new project_"+projects.get(index).getProjectID()+"...");
				 }
				 return node;
			}
		}
		else {
			return ((AWorkspaceTreeNode) parent).getChildAt(index);
		}
	}

	public int getChildCount(Object parent) {
		if(parent == getRoot()) {
			synchronized (projects) {
				return projects.size();
			}
		}
		else {
			return ((AWorkspaceTreeNode) parent).getChildCount();
		}
	}

	public boolean isLeaf(Object node) {
		if(node == getRoot()) {
			return false;
		}
		else {
			return ((TreeNode) node).isLeaf();
		}
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		if(path == null || getRoot().equals(path.getLastPathComponent())) {
			return;
		}
		
		AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
		if (node instanceof IWorkspaceNodeActionListener) {
			((IWorkspaceNodeActionListener) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_CHANGED, newValue));
			((ProjectModel) node.getModel()).nodeChanged(node);
		}
		else {
			node.setName(newValue.toString());
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		if(parent == getRoot()) {
			synchronized (projects) {
				int index = 0;
				for (AWorkspaceProject project : projects) {
					if(child.equals(project.getModel().getRoot())) {
						return index;
					}
					index++;
				}
				return -1;
			}
		}
		else {
			return ((AWorkspaceTreeNode) parent).getIndex((TreeNode) child);
		}
	}

	public void addTreeModelListener(TreeModelListener l) {
		if(l == null) {
			return;
		}
		synchronized (listeners) {
			if(listeners.contains(l)) {
				return;
			}
			listeners.add(l);
		}
	}

	public void removeTreeModelListener(TreeModelListener l) {
		if(l == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(l);
		}
	}
	
	public List<AWorkspaceProject> getProjects() {
		return this.projects;
	}

	public static WorkspaceModel createDefaultModel() {
		return new WorkspaceModel() {
		};
	}
	
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
			return WorkspaceModel.this.getRoot();
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
			synchronized (getProjects()) {
	    		for (AWorkspaceProject project : getProjects()) {
	    			project.getModel().requestSave();
	    		}
			}
		}
	}

	public AWorkspaceProject getProject(WorkspaceTreeModel model) {
		synchronized (this.projects) {
    		for (AWorkspaceProject project : this.projects) {
    			if(project.getModel().equals(model)) {
    				return project;
    			}
    		}
		}
		return null;
	}
}
