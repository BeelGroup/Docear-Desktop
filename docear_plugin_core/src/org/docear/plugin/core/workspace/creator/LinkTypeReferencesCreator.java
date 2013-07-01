/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class LinkTypeReferencesCreator extends AWorkspaceNodeCreator {

	public static final String LINK_TYPE_REFERENCES = LinkTypeReferencesNode.TYPE;
	
	

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
		LinkTypeReferencesNode node = new LinkTypeReferencesNode();		
		String name = data.getAttribute("name", null);
		if (name==null || name.trim().length()==0) {
			name = TextUtils.getText(LinkTypeReferencesNode.class+".notyetset.text");
		}
		node.setName(name);
		String path = data.getAttribute("path", null);
		
		node.setLinkURI(URIUtils.createURI(path));					
		
		return node;
	}
}
