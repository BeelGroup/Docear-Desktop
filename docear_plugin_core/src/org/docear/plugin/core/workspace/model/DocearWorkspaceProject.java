package org.docear.plugin.core.workspace.model;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.ILibraryRepository;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.workspace.AVirtualDirectory;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.UniqueIDCreator;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectModelListener;
import org.freeplane.plugin.workspace.model.project.ProjectVersion;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class DocearWorkspaceProject extends AWorkspaceProject {
	public  static final String REPOSITORY_PATH_ADDED = "__added_repository_path__";
	public  static final ProjectVersion CURRENT_PROJECT_VERSION = new ProjectVersion("docear 1.0");
	//WORKSPACE - should docear-project ids have a prefix like "dcr_"?
	private String id;
	private URI home;	
	
	private FolderTypeLiteratureRepositoryNode literatureRepository = null;
	private ILibraryRepository library = null;
	//WORKSPACE - Docear: add bibTexDatabase to observers
	private final Vector<IBibtexDatabase> referencesIndex = new Vector<IBibtexDatabase>();
	private List<IDocearProjectListener> listeners = new ArrayList<IDocearProjectListener>();
	
	public DocearWorkspaceProject(final URI projectHome) {		
		this(null, projectHome);
	}
	
	public DocearWorkspaceProject(final String projectID, final URI projectHome) {
		this.id = projectID;
		this.home = projectHome;
		
		this.getModel().addProjectModelListener(new IProjectModelListener() {
			public void treeStructureChanged(WorkspaceModelEvent event) {
				
			}
			
			public void treeNodesRemoved(WorkspaceModelEvent event) {
				
			}
			
			@Override
			public void treeNodesInserted(WorkspaceModelEvent event) {
				if(DocearWorkspaceProject.this.equals(event.getProject())) {
					if(event.getChildren()[0] instanceof FolderTypeLiteratureRepositoryNode) {
						literatureRepository = (FolderTypeLiteratureRepositoryNode) event.getChildren()[0];
					}
					else if(event.getChildren()[0] instanceof FolderTypeLibraryNode) {
						library = (ILibraryRepository) event.getChildren()[0];
					}
					else if(event.getChildren()[0] instanceof IBibtexDatabase) {
						addReferenceToIndex((IBibtexDatabase) event.getChildren()[0]);
					}
				}
			}
			
			@Override
			public void treeNodesChanged(WorkspaceModelEvent event) {
				
			}
		});
	}
	
	public URI getProjectLibraryPath() {
		File home = URIUtils.getAbsoluteFile(getProjectDataPath());
		home = new File(home, "default_files");
		return home.toURI();
	}
	
	public List<URI> getLibraryMaps() {
		if(library == null) {
			return Collections.EMPTY_LIST;
		}
		return library.getMaps();
	}
	
	protected void addReferenceToIndex(IBibtexDatabase ref) {
		synchronized (referencesIndex) {
			referencesIndex.clear();
			if(ref == null) {
				return;
			}
			referencesIndex.add(ref);
		}
		DocearProjectChangedEvent event = new DocearProjectChangedEvent(this, ref, DocearEventType.LIBRARY_CHANGED); 
					//new DocearEvent(this, (DocearWorkspaceProject) WorkspaceController.getCurrentModel().getProject(getModel()), DocearEventType.LIBRARY_CHANGED, ref);
		fireProjectChanged(event);
		
	}
	
	public URI getBibtexDatabase() {
		synchronized (referencesIndex) {
			if(referencesIndex.size() > 0) {
				return referencesIndex.get(0).getUri();
			}
		}
		return null;
	}
	
	public URI getRelativeLibraryPath() {
		return getRelativeURI(getProjectLibraryPath());
	}
	
	public AVirtualDirectory getProjectLiteratureRepository() {
		if(this.literatureRepository == null) {
			return null;
		}
		return (AVirtualDirectory) this.literatureRepository.getFile();
	}
	
	public void addToProjectLiteratureRepository(URI uri) {
		if(uri == null || this.literatureRepository == null) {
			return;
		}
		LiteratureRepositoryPathNode pathNode = new LiteratureRepositoryPathNode();
		pathNode.setPath(uri);
		this.literatureRepository.getModel().addNodeTo(pathNode, this.literatureRepository);
		DocearProjectChangedEvent event = new DocearProjectChangedEvent(this, this.literatureRepository, REPOSITORY_PATH_ADDED);
		fireProjectChanged(event);
	}
	
	public void addProjectListener(IDocearProjectListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			if(listeners.contains(listener)) {
				return;
			}
			listeners.add(listener);
		}
	}
	
	public void removeProjectListener(IDocearProjectListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void fireProjectChanged(DocearProjectChangedEvent event) {
		if(event == null) {
			return;
		}
		synchronized (listeners) {
			for (IDocearProjectListener listener : listeners) {
				listener.changed(event);
			}
		}		
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
		File home = URIUtils.getAbsoluteFile(getProjectHome());
		home = new File(home, "_data");
		home = new File(home, getProjectID());
		return home.toURI();		
	}

	@Override
	public URI getRelativeURI(URI uri) {
		//WORKSPACE - todo: check new implementation 
		try {
			URI relativeUri = LinkController.getController().createRelativeURI(new File(getProjectHome()), new File(uri), LinkController.LINK_RELATIVE_TO_MINDMAP);
			return LinkController.createURI(WorkspaceController.PROJECT_RESOURCE_URL_PROTOCOL + "://"+ getProjectID() +"/"+relativeUri.getPath());
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return null;
	}

	@Override
	public ProjectVersion getVersion() {
		ProjectVersion version = null;
		if(getModel().getRoot() == null || ((ProjectRootNode)getModel().getRoot()).getVersion() == null) {
			version = CURRENT_PROJECT_VERSION;
		}
		else {
			version = new ProjectVersion(((ProjectRootNode)getModel().getRoot()).getVersion());
		}
		
		return version;
	}

	public static boolean isCompatible(AWorkspaceProject project) {
		if(project == null || project.getVersion() == null) {
			return false;
		}
		//DOCEAR - todo: implement more differentiated version compare method
		return CURRENT_PROJECT_VERSION.getVersionString().equals(project.getVersion().getVersionString());
	}

	
	
	
}
