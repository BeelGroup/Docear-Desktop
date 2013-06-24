package org.freeplane.core.ui.ribbon;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.ui.IndexedTree.Node;

public class RibbonBuildContext {
	private final RibbonBuilder ribbonBuilder;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RibbonBuildContext(RibbonBuilder builder) {
		this.ribbonBuilder = builder;
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public RibbonBuilder getBuilder() {
		return ribbonBuilder;
	}
	
	public Node getStructureNode(Object obj) {
		String pathKey = (String) ribbonBuilder.structure.getKeyByUserObject(obj);
		if(pathKey != null) {
			return (Node) ribbonBuilder.structure.get(pathKey);
		}
		return null;
	}
	public DefaultMutableTreeNode getStructureRoot() {
		return (Node) ribbonBuilder.structure.getRoot();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
