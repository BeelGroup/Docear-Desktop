package org.freeplane.plugin.workspace.model.project;

import java.io.File;
import java.net.URI;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.ProjectModelEvent.ProjectModelEventType;

public abstract class AWorkspaceProject {	
	
	class DefaultProjectSettings implements IProjectSettings {

	}
	
	private static IWorkspaceProjectCreater creator = null;

	private ProjectModel model;
	private IProjectSettings settings;
	
	public abstract URI getProjectHome();
	
	public abstract String getProjectID();
	
	public abstract URI getProjectDataPath();
	
	public abstract URI getRelativeURI(URI uri);
	
	protected abstract void setProjectHome(URI uri);

	public ProjectModel getModel() {
		if(this.model == null) {
			this.model = new ProjectModel(this);
			this.model.addProjectModelListener(new DefaultModelChangeListener());
		}
		return this.model;
	}
	
	public IProjectSettings getSettings() {
		if(this.settings == null) {
			this.settings = new DefaultProjectSettings();
		}
		return this.settings;
	}
	
	public static void setCurrentProjectCreator(IWorkspaceProjectCreater pCreator) {
		creator = pCreator;
	}
	
	public static AWorkspaceProject create(String projectID, URI projectHome) {
		if(projectHome == null) {
			throw new IllegalArgumentException("projectHome(URI)");
		}
		
		if(creator == null) {
			creator =  new DefaultWorkspaceProjectCreator();
		}
		
		return creator.newProject(projectID, projectHome);
	}
	
	public String toString() {
		return getModel().getRoot().getName() +"[id="+getProjectID()+";home="+getProjectHome()+"]";
	}
	
	private final class DefaultModelChangeListener implements IProjectModelListener {
		
		public void treeStructureChanged(ProjectModelEvent event) {
		}

		public void treeNodesRemoved(ProjectModelEvent event) {
		}

		public void treeNodesInserted(ProjectModelEvent event) {
		}

		public void treeNodesChanged(ProjectModelEvent event) {
			if(event.getType() == ProjectModelEventType.RENAMED && getModel().getRoot().equals(event.getTreePath().getLastPathComponent())) {
				File file = WorkspaceController.resolveFile(getProjectHome());
				File targetFile = new File(file.getParentFile(), getModel().getRoot().getName());
				if(file.exists()) {
					file.renameTo(targetFile);
				}
				setProjectHome(targetFile.toURI());
				
			}
		}
	}		
}
