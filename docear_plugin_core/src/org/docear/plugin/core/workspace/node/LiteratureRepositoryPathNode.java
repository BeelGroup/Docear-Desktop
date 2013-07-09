package org.docear.plugin.core.workspace.node;

import java.io.File;
import java.io.FileFilter;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import org.docear.plugin.core.workspace.actions.DocearRemoveRepositoryPathAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

public class LiteratureRepositoryPathNode extends FolderLinkNode implements TreeExpansionListener {
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "repository_path";
	
	private WorkspacePopupMenu popupMenu = null;
	private boolean inRefresh = false;
	

	public LiteratureRepositoryPathNode() {
		super(TYPE);
	}

	public AWorkspaceTreeNode clone() {
		LiteratureRepositoryPathNode node = new LiteratureRepositoryPathNode();
		return super.clone(node);
	}
	
	public boolean isSystem() {
		return true;
	}
	
	public String getName() {
		if(super.getName() == null) {
			setName(getFile().getName());
		}
		return super.getName();
	}
	
	public void initializePopup() {
		if (popupMenu == null) {
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					"workspace.action.node.open.location",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.copy",
					"workspace.action.node.paste",
					"workspace.action.node.physical.sort",
					WorkspacePopupMenuBuilder.SEPARATOR,
					DocearRemoveRepositoryPathAction.KEY,
//					"workspace.action.node.remove",
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
	
	public void refresh() {
		File folder;
		try {
			inRefresh = true;
			getModel().removeAllElements(this);
			folder = URIUtils.getAbsoluteFile(getPath());
			loadDirectoryFiles(folder);
			getModel().reload(this);
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
		finally {
			inRefresh = false;
		}
	}

	private void loadDirectoryFiles(File folder) {
		if (folder != null && folder.isDirectory()) {
			WorkspaceController.getFileSystemMgr().scanFileSystem(this, folder, false, new FileFilter() {
				public boolean accept(File pathname) {
//					if(pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".pdf")) {
						return true;
//					}
//					return false;
				}
			});
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		if(!inRefresh && getChildCount() <= 0) {
			loadDirectoryFiles(URIUtils.getAbsoluteFile(getPath()));
		}
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {		
	}
}