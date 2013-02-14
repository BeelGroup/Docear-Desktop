package org.docear.plugin.core.workspace.model;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.util.UniqueIDCreator;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearWorkspaceProject extends AWorkspaceProject {
	//WORKSPACE - info: should docear-project ids have a prefix like "dcr_"?
	private String id;
	private URI home;	
	
	//Observers	
	private NodeAttributeObserver<Set<URI>> literatureRepositories = new NodeAttributeObserver<Set<URI>>();
	//WORKSPACE - Docear: add bibTexDatabase to observers
	//private BibTexDatabase bibtexDatabase 
	
	
	public DocearWorkspaceProject(final URI projectHome) {		
		this(null, projectHome);
	}
	
	public DocearWorkspaceProject(final String projectID, final URI projectHome) {
		this.id = projectID;
		this.home = projectHome;
	}
	
	public URI getProjectLibraryPath() {
		File home = WorkspaceController.resolveFile(getProjectDataPath());
		home = new File(home, "default_files");
		return home.toURI();
	}
	
	public Set<URI> getProjectLiteratureRepositories() {		
		return this.literatureRepositories.getValue();
	}
	
	public void addProjectLiteratureRepository(URI uri) {
		this.literatureRepositories.getValue().add(uri);
	}
	
	@Override
	public URI getProjectHome() {		
		return home;
	}

	@Override
	public String getProjectID() {
		if(this.id == null) {
			this.id = UniqueIDCreator.getCreator().uniqueID();
		}
		return this.id;		
	}

	@Override
	public URI getProjectDataPath() {
		File home = WorkspaceController.resolveFile(getProjectHome());
		home = new File(home, "_data");
		home = new File(home, getProjectID());
		return home.toURI();		
	}

	@Override
	//WORKSPACE - todo: implement
	public URI getRelativeURI(URI uri) {
		return uri;
	}

	@Override
	protected void setProjectHome(URI home) {
		this.home = home;
	}

}
