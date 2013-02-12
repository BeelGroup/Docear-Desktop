package org.docear.plugin.core.workspace.controller;

import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;

public class DocearProjectLoader extends ProjectLoader {
	
	private FolderTypeLibraryCreator folderTypeLibraryCreator;
	private FolderTypeLiteratureRepositoryCreator folderTypeLiteratureRepositoryCreator;	
	
	private LinkTypeIncomingCreator linkTypeIncomingCreator;
	private LinkTypeLiteratureAnnotationsCreator linkTypeLiteratureAnnotationsCreator;
	private LinkTypeMyPublicationsCreator linkTypeMyPublicationsCreator;
	private LinkTypeReferencesCreator linkTypeReferencesCreator;
	

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
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, new FolderTypeLibraryCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, new FolderTypeLiteratureRepositoryCreator());		
		
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING, new LinkTypeIncomingCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS, new LinkTypeLiteratureAnnotationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS, new LinkTypeMyPublicationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES, new LinkTypeReferencesCreator());
	}
	
	public FolderTypeLibraryCreator getFolderTypeLibraryCreator() {
		if (folderTypeLibraryCreator == null) {
			folderTypeLibraryCreator = new FolderTypeLibraryCreator();
		}
		return folderTypeLibraryCreator;
	}

	public FolderTypeLiteratureRepositoryCreator getFolderTypeLiteratureRepositoryCreator() {
		if (folderTypeLiteratureRepositoryCreator == null) {
			folderTypeLiteratureRepositoryCreator = new FolderTypeLiteratureRepositoryCreator();
		}
		return folderTypeLiteratureRepositoryCreator;
	}

	public LinkTypeIncomingCreator getLinkTypeIncomingCreator() {
		if (linkTypeIncomingCreator == null) {
			linkTypeIncomingCreator = new LinkTypeIncomingCreator();
		}
		return linkTypeIncomingCreator;
	}

	public LinkTypeLiteratureAnnotationsCreator getLinkTypeLiteratureAnnotationsCreator() {
		if (linkTypeLiteratureAnnotationsCreator == null) {
			linkTypeLiteratureAnnotationsCreator = new LinkTypeLiteratureAnnotationsCreator();
		}
		return linkTypeLiteratureAnnotationsCreator;
	}

	public LinkTypeMyPublicationsCreator getLinkTypeMyPublicationsCreator() {
		if (linkTypeMyPublicationsCreator == null) {
			linkTypeMyPublicationsCreator = new LinkTypeMyPublicationsCreator();
		}
		return linkTypeMyPublicationsCreator;
	}

	public LinkTypeReferencesCreator getLinkTypeReferencesCreator() {
		if (linkTypeReferencesCreator == null) {
			linkTypeReferencesCreator = new LinkTypeReferencesCreator();
		}
		return linkTypeReferencesCreator;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
