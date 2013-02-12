/**
 * author: Marcel Genzmehr
 * 23.08.2011
 */
package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectSettings;


public class FolderTypeProjectNode extends AFolderNode implements IMutableLinkNode, IWorkspaceNodeActionListener, IDropAcceptor {

	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(AFolderNode.class.getResource("/images/project-open-2.png"));
	private static WorkspacePopupMenu popupMenu = null;
	private String projectID;
	private URI projectRoot = null;
	private AWorkspaceProject projectContainer;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderTypeProjectNode() {
		this(null);
	}
	
	public FolderTypeProjectNode(String type) {
		super(null);
		setParent(WorkspaceController.getCurrentModel().getRoot());
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public URI getPath() {
		return this.projectRoot;		
	}
	
	@ExportAsAttribute(name="ID")
	public String getProjectID() {
		return this.projectID;
	}
	
	public void setProjectID(String id) {
		this.projectID = id;
	}
	
	public IProjectSettings getSetting() {
		return null;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}

	protected AWorkspaceTreeNode clone(FolderTypeProjectNode node) {
		return super.clone(node);
	}
	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			File targetDir = WorkspaceController.resolveFile(getPath());
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
					File srcFile = WorkspaceController.resolveFile(((LinkTypeFileNode) node).getLinkPath());
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
//			WorkspaceController.getController().getExpansionStateHandler().addPathKey(this.getKey());
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {
			File targetDir = WorkspaceController.resolveFile(getPath());			
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
			File targetDir = WorkspaceController.resolveFile(getPath());			
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
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	public void refresh() {
		try {
			getModel().reload(this);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AWorkspaceTreeNode clone() {
		FolderTypeProjectNode node = new FolderTypeProjectNode(getType());
		return clone(node);
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.file.new.mindmap",
					//WorkspacePopupMenuBuilder.SEPARATOR,
					//"workspace.action.file.new.file",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.docear.uri.change",
					"workspace.action.node.open.location",
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

	@SuppressWarnings("unchecked")
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
						uriList.add(URI.create(singleUri));
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
	
	public String getTagName() {
		return "project";
	}

	public void initiateMyFile(AWorkspaceProject project) {
		projectContainer = project;
		FolderTypeMyFilesNode myFilesNode = new FolderTypeMyFilesNode(project); 
		getModel().addNodeTo(myFilesNode, this);
		myFilesNode.refresh();
	}

	public boolean changeName(String newName, boolean renameLink) {
		try {
			if(renameLink) {
				this.getModel().changeNodeName(this, newName);
				this.refresh();
			}
			else {
				this.setName(newName);
				this.getParent().refresh();
			}
			
			
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file") + " ("+ex.getMessage()+")", 
					TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
