/**
 * author: Marcel Genzmehr
 * 16.08.2011
 */
package org.freeplane.plugin.workspace.model.node;

import java.net.URI;


/**
 * 
 */
public abstract class ALinkNode extends AWorkspaceTreeNode {
	
	private static final long serialVersionUID = 1L;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public static final String LINK_TYPE_FILE = "file";

	/**
	 * @param type
	 */
	public ALinkNode(String type) {
		super(type);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public abstract URI getLinkPath();
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public final String getTagName() {
		return "link";
	}

	@Override
	public void initializePopup() {
		// TODO Auto-generated method stub
		
	}
}