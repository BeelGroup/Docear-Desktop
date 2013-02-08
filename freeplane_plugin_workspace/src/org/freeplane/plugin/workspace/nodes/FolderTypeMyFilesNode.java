package org.freeplane.plugin.workspace.nodes;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Locale;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class FolderTypeMyFilesNode extends AFolderNode {
	public static final String TYPE = "myFiles"; 
	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject project;

	public FolderTypeMyFilesNode(AWorkspaceProject project) {
		super(TYPE);
		this.project = project;
	}

	public String getName() {
		return TextUtils.getText(FolderTypeMyFilesNode.class.getName().toLowerCase(Locale.ENGLISH)+".name");
	}
	
	@Override
	public URI getPath() {
		return project.getProjectHome();
	}
	
	public AWorkspaceTreeNode clone() {
		return super.clone(new FolderTypeMyFilesNode(project));
	}

	@Override
	public void initializePopup() {
	}

	@Override
	public WorkspacePopupMenu getContextMenu() {
		return null;
	}
	
	public void refresh() {
		try {
			File file = WorkspaceController.resolveFile(getPath());
			if (file != null) {
				getModel().removeAllElements(this);
				WorkspaceController.getFileSystemMgr().scanFileSystem(this, file, new FileFilter() {
					
					public boolean accept(File pathname) {
						if("_data".equals(pathname.getName())) {
							return false;
						}
						return true;
					}
				});
				getModel().reload(this);
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final String getTagName() {
		return null;
	}

}
