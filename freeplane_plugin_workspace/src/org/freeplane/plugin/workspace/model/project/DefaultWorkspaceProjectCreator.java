package org.freeplane.plugin.workspace.model.project;

import java.net.URI;

import org.freeplane.core.util.UniqueIDCreator;

public class DefaultWorkspaceProjectCreator implements IWorkspaceProjectCreater {

	public AWorkspaceProject newProject(final String projectID, final URI projectHome) {
		
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

			protected void setProjectHome(URI home) {
				this.home = home;
				
			}

			public URI getRelativeURI(URI uri) {
				return uri;
			}
		};
	}

}