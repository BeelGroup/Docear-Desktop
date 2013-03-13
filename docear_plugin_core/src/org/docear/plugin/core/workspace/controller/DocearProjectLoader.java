package org.docear.plugin.core.workspace.controller;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import org.apache.commons.io.IOExceptionWithCause;
import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryPathCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.core.workspace.node.LinkTypeLiteratureAnnotationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeMyPublicationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;
import org.freeplane.plugin.workspace.nodes.FolderTypeMyFilesNode;
import org.freeplane.plugin.workspace.nodes.FolderVirtualNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class DocearProjectLoader extends ProjectLoader {	
	private FolderTypeLibraryCreator folderTypeLibraryCreator;
	private FolderTypeLiteratureRepositoryCreator folderTypeLiteratureRepositoryCreator;
	private FolderTypeLiteratureRepositoryPathCreator folderTypeLiteratureRepositoryPathCreator;
	
	private LinkTypeIncomingCreator linkTypeIncomingCreator;
	private LinkTypeLiteratureAnnotationsCreator linkTypeLiteratureAnnotationsCreator;
	private LinkTypeMyPublicationsCreator linkTypeMyPublicationsCreator;
	private LinkTypeReferencesCreator linkTypeReferencesCreator;
	private IResultProcessor resultProcessor;
	
	
	//DOCEAR - required for backwards compatibility   
//	private final static String CONFIG_FILE_NAME = "workspace.xml";
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearProjectLoader() {
		super();	
		initDocearReadManager();	
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void initDocearReadManager() {		
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, getFolderTypeLibraryCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, getFolderTypeLiteratureRepositoryCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLiteratureRepositoryPathCreator.REPOSITORY_PATH_TYPE, getFolderTypeLiteratureRepositoryPathCreator());
		
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING, getLinkTypeIncomingCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS, getLinkTypeLiteratureAnnotationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS, getLinkTypeMyPublicationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES, getLinkTypeReferencesCreator());
	}
	
	private AWorkspaceNodeCreator getFolderTypeLiteratureRepositoryPathCreator() {
		if (folderTypeLiteratureRepositoryPathCreator == null) {
			folderTypeLiteratureRepositoryPathCreator = new FolderTypeLiteratureRepositoryPathCreator();
		}
		return folderTypeLiteratureRepositoryPathCreator;
	}
	
	private FolderTypeLibraryCreator getFolderTypeLibraryCreator() {
		if (folderTypeLibraryCreator == null) {
			folderTypeLibraryCreator = new FolderTypeLibraryCreator();
		}
		return folderTypeLibraryCreator;
	}

	private FolderTypeLiteratureRepositoryCreator getFolderTypeLiteratureRepositoryCreator() {
		if (folderTypeLiteratureRepositoryCreator == null) {
			folderTypeLiteratureRepositoryCreator = new FolderTypeLiteratureRepositoryCreator();
		}
		return folderTypeLiteratureRepositoryCreator;
	}
	
	

	private LinkTypeIncomingCreator getLinkTypeIncomingCreator() {
		if (linkTypeIncomingCreator == null) {
			linkTypeIncomingCreator = new LinkTypeIncomingCreator();
		}
		return linkTypeIncomingCreator;
	}

	private LinkTypeLiteratureAnnotationsCreator getLinkTypeLiteratureAnnotationsCreator() {
		if (linkTypeLiteratureAnnotationsCreator == null) {
			linkTypeLiteratureAnnotationsCreator = new LinkTypeLiteratureAnnotationsCreator();
		}
		return linkTypeLiteratureAnnotationsCreator;
	}

	private LinkTypeMyPublicationsCreator getLinkTypeMyPublicationsCreator() {
		if (linkTypeMyPublicationsCreator == null) {
			linkTypeMyPublicationsCreator = new LinkTypeMyPublicationsCreator();
		}
		return linkTypeMyPublicationsCreator;
	}

	private LinkTypeReferencesCreator getLinkTypeReferencesCreator() {
		if (linkTypeReferencesCreator == null) {
			linkTypeReferencesCreator = new LinkTypeReferencesCreator();
		}
		return linkTypeReferencesCreator;
	}
	
	public synchronized LOAD_RETURN_TYPE loadProject(AWorkspaceProject project) throws IOException {
		try {
			File projectSettings = new File(URIUtils.getAbsoluteFile(project.getProjectDataPath()),"settings.xml");
			if(projectSettings.exists()) {
				getDefaultResultProcessor().setProject(project);
				load(projectSettings.toURI());
				return LOAD_RETURN_TYPE.EXISTING_PROJECT;
			}
			else {
				createDefaultProject((DocearWorkspaceProject)project);
				return LOAD_RETURN_TYPE.NEW_PROJECT;
			}
		}
		catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
	}
	
	public IResultProcessor getDefaultResultProcessor() {
		if(this.resultProcessor == null) {
			this.resultProcessor = new DocearResultProcessor();
		}
		return this.resultProcessor;
	}
	
	private void createDefaultProject(DocearWorkspaceProject project) {
		File home = URIUtils.getFile(project.getProjectHome());
		if(!home.exists()) {
			home.mkdirs();
		}
		ProjectRootNode root = new ProjectRootNode();
		root.setProjectID(project.getProjectID());				
		root.setModel(project.getModel());
		root.setName(home.getName());
		
		project.getModel().setRoot(root);
		
		
		// create and load all default nodes
		FolderTypeLibraryNode libNode = new FolderTypeLibraryNode();
		libNode.setName(TextUtils.getText(libNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(libNode, root);
		
		URI libPath = project.getRelativeURI(project.getProjectLibraryPath());
		
		LinkTypeIncomingNode incomNode = new LinkTypeIncomingNode();
		incomNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/incoming.mm"));
		incomNode.setName(TextUtils.getText(incomNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(incomNode, libNode);
		
		LinkTypeLiteratureAnnotationsNode litNode = new LinkTypeLiteratureAnnotationsNode();
		litNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/literature_and_annotations.mm"));
		litNode.setName(TextUtils.getText(litNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(litNode, libNode);
		
		LinkTypeMyPublicationsNode pubNode = new LinkTypeMyPublicationsNode();
		pubNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/my_publications.mm"));
		pubNode.setName(TextUtils.getText(pubNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(pubNode, libNode);
		
		LinkTypeFileNode tempNode = new LinkTypeFileNode();
		tempNode.setLinkURI(URIUtils.createURI(libPath.toString()+"/temp.mm"));
		tempNode.setName(TextUtils.getText(tempNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".temp.label" ));
		tempNode.setSystem(true);
		project.getModel().addNodeTo(tempNode, libNode);
		
		LinkTypeFileNode trashNode = new LinkTypeFileNode();
		trashNode.setLinkURI(URIUtils.createURI(libPath.toString()+"/trash.mm"));
		trashNode.setName(TextUtils.getText(trashNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".trash.label" ));
		trashNode.setSystem(true);
		project.getModel().addNodeTo(trashNode, libNode);
				
		FolderVirtualNode refs = new FolderVirtualNode() {
			private static final long serialVersionUID = 1L;

			public WorkspacePopupMenu getContextMenu() {
				return null;
			}
			
			public boolean acceptDrop(DataFlavor[] flavors) {
				return false;
			}
		};
		refs.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".refnode.name"));
		refs.setSystem(true);
		project.getModel().addNodeTo(refs, root);
		
		LinkTypeReferencesNode defaultRef = new LinkTypeReferencesNode();
		defaultRef.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".refnode.name"));
		defaultRef.setLinkURI(URIUtils.createURI(libPath.toString()+"/default.bib"));
		project.getModel().addNodeTo(defaultRef, refs);
				
		FolderTypeLiteratureRepositoryNode litRepoNode = new FolderTypeLiteratureRepositoryNode();
		litRepoNode.setSystem(true);		
		project.getModel().addNodeTo(litRepoNode, root);
		
		LiteratureRepositoryPathNode pathNode = new LiteratureRepositoryPathNode();
		pathNode.setPath(URIUtils.createURI(libPath.toString()+"/literature_repositiory"));
		pathNode.setName(TextUtils.getText(pathNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".default.label" ));
		pathNode.setSystem(true);
		project.getModel().addNodeTo(pathNode, litRepoNode);		
		
		root.initiateMyFile(project);
		
		FolderVirtualNode misc = new FolderVirtualNode();
		misc.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".miscnode.name"));
		project.getModel().addNodeTo(misc, root);
		//misc -> help.mm
		root.refresh();
	}
	
	/***********************************************************************************
	 * NESTED CLASSES
	 **********************************************************************************/
	
	private class DocearResultProcessor implements IResultProcessor {

		private AWorkspaceProject project;

		public AWorkspaceProject getProject() {
			return project;
		}

		public void setProject(AWorkspaceProject project) {
			this.project = project;
		}

		public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
			if(getProject() == null) {
				LogUtils.warn("Missing project container! cannot add node to a model.");
				return;
			}
			if(node instanceof ProjectRootNode) {
				getProject().getModel().setRoot(node);
				if(((ProjectRootNode) node).getProjectID() == null) {
					((ProjectRootNode) node).setProjectID(getProject().getProjectID());
				}
				
			}
			else {
				if(parent == null) {
					if (!getProject().getModel().containsNode(node.getKey())) {
						getProject().getModel().addNodeTo(node, parent);			
					}
				}
				else {
					if (!parent.getModel().containsNode(node.getKey())) {
						parent.getModel().addNodeTo(node, parent);			
					}
				}
				//add myFiles after a certain node type
//				if(node instanceof FolderTypeLibraryNode)
				if(node instanceof FolderTypeLiteratureRepositoryNode)
				{
					((ProjectRootNode) parent.getModel().getRoot()).initiateMyFile(getProject());
				}
			}
		}

	}
}
