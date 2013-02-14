package org.docear.plugin.core.workspace.node;

import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryPathCreator;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryNode extends AFolderNode {
	//WORKSPACE - todo: implement dnd handling
	private static final long serialVersionUID = 1L;
	private WorkspacePopupMenu popupMenu = null;
	
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLiteratureRepositoryNode.class.getResource("/images/books.png"));
	public static final String TYPE = "literature_repository";

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLiteratureRepositoryNode() {
		this(TYPE);
	}
	
	public FolderTypeLiteratureRepositoryNode(String type) {
		super(type);
		//WORKSPACE - todo: implement observer structure
//		CoreConfiguration.repositoryPathObserver.addChangeListener(this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public AWorkspaceTreeNode clone() {
		FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(getType());
		return clone(node);
	}
	
	public void disassociateReferences()  {
//		CoreConfiguration.repositoryPathObserver.removeChangeListener(this);
	}
	
	public void setName(String name) {
		super.setName("Literature Repository");
	}
	
	public void addPath(URI uri) {
		if(uri == null) {
			return;
		}
		AWorkspaceTreeNode newPathItem = FolderTypeLiteratureRepositoryPathCreator.newPathItem(uri, false);
		this.getModel().addNodeTo(newPathItem, this);
		this.refresh();
		newPathItem.refresh();
		
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void initializePopup() {
		if (popupMenu == null) {
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
//					"workspace.action.node.new.folder",
//					"workspace.action.file.new.mindmap",
					//WorkspacePopupMenuBuilder.SEPARATOR,
					//"workspace.action.file.new.file",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.paste",
					"workspace.action.node.physical.sort",
					WorkspacePopupMenuBuilder.SEPARATOR,		
					"workspace.action.node.refresh"
			});
		}
		
	}	
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public URI getPath() {
		// not used here
		return null;
	}
}
