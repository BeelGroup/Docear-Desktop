package org.docear.plugin.core.workspace.creator;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.ui.LocationDialog;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.FolderTypeProjectsNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FolderTypeProjectsCreator extends AWorkspaceNodeCreator {
	public static final String FOLDER_TYPE_PROJECTS = "projects";
	
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
		String type = data.getAttribute("type", FOLDER_TYPE_PROJECTS);
		FolderTypeProjectsNode node = new FolderTypeProjectsNode(type);

		String name = data.getAttribute("name", null);		
		if(name == null) {
			return null;
		}		
		node.setName(name);
		
		boolean monitor = Boolean.parseBoolean(data.getAttribute("monitor", "false"));
		node.enableMonitoring(monitor);
		
		boolean descending = Boolean.parseBoolean(data.getAttribute("orderDescending", "false"));
		node.orderDescending(descending);
		
		String path = data.getAttribute("path", null);
		if(path == null || path.trim().length() == 0) {
			URI uri = CoreConfiguration.projectPathObserver.getUri();
			if (uri == null) {
				LocationDialog.showWorkspaceChooserDialog();		    	
			}
			else {
				node.setPath(uri);
			}
			return node;
		}
		
		node.setPath(URI.create(path));
		return node;
	}
	
	public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
		super.endElement(parent, tag, node, lastBuiltElement);
    		
    	try {    		
    		if (node instanceof FolderTypeProjectsNode && ((FolderTypeProjectsNode) node).getChildCount() == 0 && ((FolderTypeProjectsNode) node).getPath() != null) {
    			File file = WorkspaceController.resolveFile(((FolderTypeLiteratureRepositoryNode) node).getPath());        		  
    			WorkspaceController.getFileSystemMgr().scanFileSystem((AWorkspaceTreeNode) node, file);    		
    		}
		}
		catch (Exception e) {
			System.err.println(this.getClass()+ ".endElement()"+e.getMessage());
		}
	}
}
