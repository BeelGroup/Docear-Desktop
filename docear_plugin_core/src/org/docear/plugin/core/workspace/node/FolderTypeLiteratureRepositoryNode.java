package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.workspace.AVirtualDirectory;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryPathCreator;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;
import org.freeplane.plugin.workspace.nodes.AFolderNode;

/**
 * 
 */
public class FolderTypeLiteratureRepositoryNode extends AFolderNode implements IWorkspaceNodeActionListener
																				,IWorkspaceProjectExtension
																				,IFileSystemRepresentation {
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
		setName(null);
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
		super.setName(TextUtils.getText(this.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
	}
	
	public void addPath(URI uri) {
		if(uri == null) {
			return;
		}
		AWorkspaceTreeNode newPathItem = FolderTypeLiteratureRepositoryPathCreator.newPathItem(null, uri, false);
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
					"workspace.action.node.add.repository",
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

	public File getFile() {
		return new AVirtualDirectory("LiteratureRepository") {
			private static final long serialVersionUID = 1L;

			protected Collection<File> getChildren() {
				ArrayList<File> list = new ArrayList<File>();
				Enumeration<AWorkspaceTreeNode> paths = children();
				while(paths.hasMoreElements()) {
					LiteratureRepositoryPathNode node = (LiteratureRepositoryPathNode) paths.nextElement();
					list.add(URIUtils.getAbsoluteFile(node.getPath()));
				}
				return list;
			}
		};
	}
	
	public void orderDescending(boolean enable) {
		//not used
	}

	public boolean orderDescending() {
		return false;
	}

	@Override
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
			event.consume();
		}
	}
}
