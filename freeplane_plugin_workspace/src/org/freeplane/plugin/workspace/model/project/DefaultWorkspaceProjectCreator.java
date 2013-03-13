package org.freeplane.plugin.workspace.model.project;

import java.io.File;
import java.net.URI;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.UniqueIDCreator;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;

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
				return URIUtils.createURI(getProjectHome().toString()+"/_data/"+getProjectID());
			}

			public URI getRelativeURI(URI uri) {
				//WORKSPACE - todo: check new implementation 
				try {
					URI relativeUri = LinkController.getController().createRelativeURI(new File(getProjectHome()), new File(uri), LinkController.LINK_RELATIVE_TO_MINDMAP);
					return new URI(WorkspaceController.PROJECT_RESOURCE_URL_PROTOCOL + "://"+ getProjectID() +"/"+relativeUri.getPath());
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
				return null;
			}
		};
	}

}