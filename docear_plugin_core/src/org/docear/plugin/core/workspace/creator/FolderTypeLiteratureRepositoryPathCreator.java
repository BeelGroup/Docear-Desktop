package org.docear.plugin.core.workspace.creator;

import java.net.URI;

import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FolderTypeLiteratureRepositoryPathCreator extends AWorkspaceNodeCreator {

	public static final String REPOSITORY_PATH_TYPE = LiteratureRepositoryPathNode.TYPE;

	public AWorkspaceTreeNode getNode(XMLElement data) {
		final String path = data.getAttribute("path", null);
		boolean descending = Boolean.parseBoolean(data.getAttribute("orderDescending", "false"));
		try {
			return newPathItem(URI.create(path), descending);
    	}
    	catch (Exception ex) {
    		return null;
    	}
	}



	public static AWorkspaceTreeNode newPathItem(final URI path, boolean descending) {
		LiteratureRepositoryPathNode node = new LiteratureRepositoryPathNode();
		node.orderDescending(descending);
		if(path == null) {
			return null;
		}
		node.setPath(path);
		
		return node;
	}

}
