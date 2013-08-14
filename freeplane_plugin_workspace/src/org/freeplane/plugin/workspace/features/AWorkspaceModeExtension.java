package org.freeplane.plugin.workspace.features;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.tree.TreePath;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.dnd.INodeDropHandler;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferHandler;
import org.freeplane.plugin.workspace.dnd.NoDropHandlerFoundExeption;
import org.freeplane.plugin.workspace.event.AWorkspaceEvent;
import org.freeplane.plugin.workspace.event.IWorkspaceListener;
import org.freeplane.plugin.workspace.handler.INodeTypeIconHandler;
import org.freeplane.plugin.workspace.handler.INodeTypeIconManager;
import org.freeplane.plugin.workspace.handler.IOController;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;

public abstract class AWorkspaceModeExtension implements IExtension {
	private final IOController workspaceIOController = new IOController();
	private final Set<IWorkspaceListener> listeners = new LinkedHashSet<IWorkspaceListener>();
	private ProjectLoader projectLoader; 
	
	public AWorkspaceModeExtension(ModeController modeController) {
	}
	public abstract void start(ModeController modeController);
	
	public abstract WorkspaceModel getModel();
	public abstract void setModel(WorkspaceModel model);
	public abstract FileReadManager getFileTypeManager();
	public abstract URI getDefaultProjectHome();
	public abstract AWorkspaceProject getSelectedProject();
	public abstract void save();
	
	public abstract void shutdown();
	
	public abstract void load();
	
	public abstract void clear();
	
	public IWorkspaceView getView() {
		return new IWorkspaceView() {
			
			public void setPaintingEnabled(boolean enabled) {
			}
			
			public void refreshView() {
			}
			
			public boolean isPaintingEnabled() {
				return false;
			}
			
			public IWorkspaceTransferHandler getTransferHandler() {
				return new IWorkspaceTransferHandler() {

					public void registerNodeDropHandler(Class<? extends AWorkspaceTreeNode> clzz, INodeDropHandler handler) {
						
					}

					public boolean handleDrop(AWorkspaceTreeNode targetNode, Transferable transf, int dndAction)
							throws NoDropHandlerFoundExeption {
						return false;
					}
				};
			}
			
			public TreePath getSelectionPath() {
				return null;
			}
			
			public TreePath getPathForLocation(int x, int y) {
				return null;
			}
			
			public INodeTypeIconManager getNodeTypeIconManager() {
				return new INodeTypeIconManager() {
					
					public INodeTypeIconHandler removeNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type) {
						return null;
					}
					
					public Icon getIconForNode(AWorkspaceTreeNode node) {
						return null;
					}
					
					public void addNodeTypeIconHandler(Class<? extends AWorkspaceTreeNode> type, INodeTypeIconHandler handler) {
					}
				};
			}
			
			public AWorkspaceTreeNode getNodeForLocation(int x, int y) {
				return null;
			}
			
			public void expandPath(TreePath treePath) {
			}
			
			public boolean containsComponent(Component comp) {
				return false;
			}
			
			public void collapsePath(TreePath treePath) {
			}
			
			public void addProjectSelectionListener(IProjectSelectionListener projectSelectionListener) {
			}

			public WorkspaceNodeSelectionHandler getNodeSelectionHandler() {
				return new WorkspaceNodeSelectionHandler();
			}

			public Component getComponent() {
				return null;
			}
		};
	}
	
	public IOController getIOController() {
		return workspaceIOController;
	}

	public final void addWorkspaceListener(IWorkspaceListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public final void removeWorkspaceListener(IWorkspaceListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public final void dispatchWorkspaceEvent(AWorkspaceEvent event) {
		synchronized (listeners) {
			for (IWorkspaceListener listener : listeners) {
				listener.handleWorkspaceEvent(event);
			}
		}
	}

	public ProjectLoader getProjectLoader() {
		if(this.projectLoader == null) {
			this.projectLoader = new ProjectLoader(); 
		}
		return this.projectLoader;
	}
	
	public void setProjectLoader(ProjectLoader loader) {
		this.projectLoader = loader;
	}
}
