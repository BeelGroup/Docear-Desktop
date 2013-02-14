package org.docear.plugin.core.workspace.node;

import java.io.File;
import java.io.FileFilter;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

public class LiteratureRepositoryPathNode extends FolderLinkNode {
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "repository_path";
	

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
	
	public void refresh() {
		File folder;
		try {
			folder = WorkspaceController.resolveFile(getPath());
			if (folder.isDirectory()) {
				getModel().removeAllElements(this);
				WorkspaceController.getFileSystemMgr().scanFileSystem(this, folder, new FileFilter() {
					public boolean accept(File pathname) {
						if(pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".pdf")) {
							return true;
						}
						return false;
					}
				});
				getModel().reload(this);				
			}
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}		
	}
}