package org.freeplane.plugin.workspace.model.project;

import java.net.URI;

import org.freeplane.core.util.UniqueIDCreator;

public abstract class AWorkspaceProject {	
	
	class DefaultProjectSettings implements IProjectSettings {

	}

	private ProjectModel model;
	private IProjectSettings settings;
	
	public abstract URI getProjectHome();
	
	public abstract String getProjectID();
	
	public abstract URI getProjectDataPath();

	public ProjectModel getModel() {
		if(this.model == null) {
			this.model = new ProjectModel(this);
		}
		return this.model;
	}
	
	public IProjectSettings getSettings() {
		if(this.settings == null) {
			this.settings = new DefaultProjectSettings();
		}
		return this.settings;
	}

	public static AWorkspaceProject create(final String projectID, final URI projectHome) {
		if(projectHome == null) {
			throw new IllegalArgumentException("projectHome(URI)");
		}
		return new AWorkspaceProject() {	
			private String id = projectID;
			private URI home = projectHome;
			
			@Override
			public String getProjectID() {
				if(this.id == null) {
					this.id = UniqueIDCreator.getCreator().uniqueID();
				}
				return this.id;
			}
			
			@Override
			public URI getProjectHome() {
				return this.home;
			}

			@Override
			public URI getProjectDataPath() {
				return URI.create(getProjectHome().toString()+"/_data/"+getProjectID());
			}
		};
	}
		
}
