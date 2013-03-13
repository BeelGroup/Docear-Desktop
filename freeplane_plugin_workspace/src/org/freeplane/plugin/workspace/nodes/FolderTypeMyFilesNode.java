package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.WorkspaceNewProjectAction;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class FolderTypeMyFilesNode extends AFolderNode implements IWorkspaceNodeActionListener, IDropAcceptor, IFileSystemRepresentation {

	public static final String TYPE = "myFiles"; 
	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject project;
	private static WorkspacePopupMenu popupMenu = null;
	private boolean orderDescending = false;

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
	
	public boolean isSystem() {
		return true;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public void initializePopup() {
		if (popupMenu == null) {
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					WorkspaceNewProjectAction.KEY,
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.new.folder",
					"workspace.action.file.new.mindmap",
					//WorkspacePopupMenuBuilder.SEPARATOR,
					//"workspace.action.file.new.file",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",						
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
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
	
	public void refresh() {
		try {
			File file = URIUtils.getAbsoluteFile(getPath());
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
	
	public boolean acceptDrop(DataFlavor[] flavors) {
		for(DataFlavor flavor : flavors) {
			if(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_NODE_FLAVOR.equals(flavor)
			) {
				return true;
			}
		}
		return false;
	}
	
	public boolean processDrop(DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable transferable = event.getTransferable();
		if(processDrop(transferable, event.getDropAction())) {
			event.dropComplete(true);
			return true;
		}
		event.dropComplete(false);
		return false;
	}
	
	public boolean processDrop(Transferable transferable, int dropAction) {
		try {
			if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				processWorkspaceNodeDrop((List<AWorkspaceTreeNode>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR), dropAction);	
			}
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				processFileListDrop((List<File>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR), dropAction);
			} 
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				ArrayList<URI> uriList = new ArrayList<URI>();
				String uriString = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
				if (!uriString.startsWith("file://")) {
					return false;
				}
				String[] uriArray = uriString.split("\r\n");
				for(String singleUri : uriArray) {
					try {
						uriList.add(URIUtils.createURI(singleUri));
					}
					catch (Exception e) {
						LogUtils.info("DOCEAR - "+ e.getMessage());
					}
				}
				processUriListDrop(uriList, dropAction);	
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return true;
	}
	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			File targetDir = URIUtils.getAbsoluteFile(getPath());
			for(AWorkspaceTreeNode node : nodes) {
				if(node instanceof DefaultFileNode) {					
					if(targetDir != null && targetDir.isDirectory()) {
						if(dropAction == DnDConstants.ACTION_COPY) {
							((DefaultFileNode) node).copyTo(targetDir);
						} 
						else if(dropAction == DnDConstants.ACTION_MOVE) {
							File oldFile = ((DefaultFileNode) node).getFile();
							((DefaultFileNode) node).moveTo(targetDir);
							File newFile = new File(targetDir, ((DefaultFileNode) node).getName());
							AWorkspaceTreeNode parent = node.getParent();
							getModel().cutNodeFromParent(node);
							parent.refresh();
							getModel().nodeMoved(node, oldFile, newFile);
						}
				}
				}
				else if(node instanceof LinkTypeFileNode) {
					File srcFile = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
					if(targetDir != null && targetDir.isDirectory()) {
						FileUtils.copyFileToDirectory(srcFile, targetDir);
						if(dropAction == DnDConstants.ACTION_MOVE) {
							AWorkspaceTreeNode parent = node.getParent();
							getModel().cutNodeFromParent(node);
							parent.refresh();
							getModel().nodeMoved(node, srcFile, new File(targetDir, srcFile.getName()));
						}
					}
				}
			}
			//WorkspaceController.getController().getExpansionStateHandler().addPathKey(this.getKey());
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {
			File targetDir = URIUtils.getAbsoluteFile(getPath());			
			for(File srcFile : files) {
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processUriListDrop(List<URI> uris, int dropAction) {
	try {
			File targetDir = URIUtils.getAbsoluteFile(getPath());			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
		
	}

	public File getFile() {
		return URIUtils.getAbsoluteFile(getPath());
	}

	public void orderDescending(boolean enable) {
		this.orderDescending = enable;
	}

	public boolean orderDescending() {
		return orderDescending;
	}

}
