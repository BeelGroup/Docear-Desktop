package org.freeplane.plugin.workspace.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class ProjectRootCreator extends AWorkspaceNodeCreator {
	
	
	public ProjectRootCreator() {
		
	}

	public AWorkspaceTreeNode getNode(XMLElement data) {		
		ProjectRootNode node = new ProjectRootNode();
		String name = data.getAttribute("name", "project");
		String id = data.getAttribute("id", null);
		node.setName(name);
		node.setProjectID(id);
		return node;
	}	
}
