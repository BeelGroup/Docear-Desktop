/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryCreator extends AWorkspaceNodeCreator {

	public static final String FOLDER_TYPE_LITERATUREREPOSITORY = "literature_repository";

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", FOLDER_TYPE_LITERATUREREPOSITORY);
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(type);
		// TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		
		boolean descending = Boolean.parseBoolean(data.getAttribute("orderDescending", "false"));
		node.orderDescending(descending);
		
		//WORKSPACE - info: old path dialog disabled --> ProjectLoader: "NewProjectDialog"
		if (path == null || path.length()==0) {
//			URI uri = CoreConfiguration.repositoryPathObserver.getUri();
//			
//			if (uri == null) {
//				LocationDialog.showWorkspaceChooserDialog();	
//			}
//			else {
//				node.setPath(uri);
//			}
			return node;
		}
		
		node.setPath(URI.create(path));

		return node;
	}

	public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, node, lastBuiltElement);
		
		try {
    		File file = WorkspaceController.resolveFile(((FolderTypeLiteratureRepositoryNode) node).getPath());    		    		
    		if (node instanceof FolderTypeLiteratureRepositoryNode && ((FolderTypeLiteratureRepositoryNode) node).getChildCount() == 0) {
    			WorkspaceController.getFileSystemMgr().scanFileSystem((AWorkspaceTreeNode) node, file);
    		}
		}
		catch(Exception e) {
			System.err.println(this.getClass()+ ".endElement()"+e.getMessage());
		}

	}
}
