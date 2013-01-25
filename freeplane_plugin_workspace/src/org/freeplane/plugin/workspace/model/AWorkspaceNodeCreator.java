package org.freeplane.plugin.workspace.model;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;

public abstract class AWorkspaceNodeCreator implements IElementDOMHandler {
	
	abstract public AWorkspaceTreeNode getNode(final XMLElement data);
	
			
	public AWorkspaceNodeCreator() {
	}
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}		
		
		AWorkspaceTreeNode node = getNode(attributes);		
		if (node == null) { 
			return null;
		}
		node.setParent((AWorkspaceTreeNode) parent);
		node.setMandatoryAttributes(attributes);
		node.initializePopup();
			
		if (!WorkspaceController.getCurrentModel().containsNode(node.getKey())) {
			if(node instanceof WorkspaceRoot) {
				WorkspaceController.getCurrentModel().setRoot(node);
			} 
			else {
				WorkspaceController.getCurrentModel().addNodeTo(node, (AWorkspaceTreeNode) parent);
			}
			
		}
		return node;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
	}	
}