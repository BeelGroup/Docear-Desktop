package org.docear.plugin.core.workspace.controller;

import java.io.File;
import java.io.IOException;
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
import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.core.workspace.node.LinkTypeLiteratureAnnotationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeMyPublicationsNode;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.WorkspaceNewMapAction;
import org.freeplane.plugin.workspace.mindmapmode.MModeWorkspaceLinkController;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
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
	
	public synchronized void loadProject(AWorkspaceProject project) throws IOException {
		try {
			File projectSettings = new File(WorkspaceController.resolveFile(project.getProjectDataPath()),"settings.xml");
			if(projectSettings.exists()) {
				getDefaultResultProcessor().setProject(project);
				load(projectSettings.toURI());
			}
			else {
				createDefaultProject((DocearWorkspaceProject)project);				
			}
		}
		catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
	}
	
	private void createDefaultProject(DocearWorkspaceProject project) {
		ProjectRootNode root = new ProjectRootNode();
		root.setProjectID(project.getProjectID());				
		root.setModel(project.getModel());
		root.setName(WorkspaceController.resolveFile(project.getProjectHome()).getName());
		root.setProjectID(project.getProjectID());
		project.getModel().setRoot(root);
		
		// create and load all default nodes
		FolderTypeLibraryNode libNode = new FolderTypeLibraryNode();
		libNode.setName(TextUtils.getText(libNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(libNode, root);
		
		LinkTypeIncomingNode incomNode = new LinkTypeIncomingNode();
		incomNode.setLinkPath(MModeWorkspaceLinkController.extendPath(libNode.getLibraryPath(), "incoming.mm"));
		incomNode.setName(TextUtils.getText(incomNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(incomNode, libNode);
		
		LinkTypeLiteratureAnnotationsNode litNode = new LinkTypeLiteratureAnnotationsNode();
		litNode.setLinkPath(MModeWorkspaceLinkController.extendPath(libNode.getLibraryPath(), "literature_and_annotations.mm"));
		litNode.setName(TextUtils.getText(litNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(litNode, libNode);
		
		LinkTypeMyPublicationsNode pubNode = new LinkTypeMyPublicationsNode();
		pubNode.setLinkPath(MModeWorkspaceLinkController.extendPath(libNode.getLibraryPath(), "my_publications.mm"));
		pubNode.setName(TextUtils.getText(pubNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(pubNode, libNode);
		
		LinkTypeFileNode tempNode = new LinkTypeFileNode();
		tempNode.setLinkPath(MModeWorkspaceLinkController.extendPath(libNode.getLibraryPath(), "temp.mm"));
		tempNode.setName(TextUtils.getText(tempNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".temp.label" ));
		tempNode.setSystem(true);
		project.getModel().addNodeTo(tempNode, libNode);
		
		LinkTypeFileNode trashNode = new LinkTypeFileNode();
		trashNode.setLinkPath(MModeWorkspaceLinkController.extendPath(libNode.getLibraryPath(), "trash.mm"));
		trashNode.setName(TextUtils.getText(trashNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".trash.label" ));
		trashNode.setSystem(true);
		project.getModel().addNodeTo(trashNode, libNode);
		
		root.initiateMyFile(project);
		
		FolderVirtualNode misc = new FolderVirtualNode(AFolderNode.FOLDER_TYPE_VIRTUAL);
		misc.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".miscnode.name"));
		project.getModel().addNodeTo(misc, root);
		//misc -> help.mm
		root.refresh();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
