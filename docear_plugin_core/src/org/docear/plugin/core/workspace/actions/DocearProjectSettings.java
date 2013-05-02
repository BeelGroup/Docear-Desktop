package org.docear.plugin.core.workspace.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;

public class DocearProjectSettings implements IWorkspaceProjectExtension {
	private boolean includeDemo = false;
	private List<URI> repositoryPathList = new ArrayList<URI>();
	private URI bibPath = null;
	private boolean addDefaultRepository = true;
	private String name;
	
	public boolean includeDemoFiles() {
		return includeDemo;
	}
	
	public void includeDemoFiles(boolean checked) {
		this.includeDemo = checked;
	}
	
	public List<URI> getRepositoryPathURIs() {
		return repositoryPathList ;
	}
	
	public void addRepositoryPathURI(URI uri) {
		repositoryPathList.add(uri);
	}
	
	public URI getBibTeXLibraryPath() {
		return bibPath;
	}
	
	public void setBibTeXLibraryPath(URI uri) {
		this.bibPath  = uri;
	}
	
	public boolean useDefaultRepositoryPath() {
		return addDefaultRepository;
	}

	public void setUseDefaultRepositoryPath(boolean enabled) {
		this.addDefaultRepository  = enabled;
	}

	public void setProjectName(String name) {
		this.name = name;
	}
	
	public String getProjectName() {
		return this.name;
	}
	
}
