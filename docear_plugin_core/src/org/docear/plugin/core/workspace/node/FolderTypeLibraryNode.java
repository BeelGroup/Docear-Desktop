/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.workspace.actions.DocearLibraryNewMindmap;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.mindmapmode.MModeWorkspaceLinkController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class FolderTypeLibraryNode extends AFolderNode implements IDocearEventListener, IDocearLibrary, IWorkspaceNodeActionListener, IWorkspaceTransferableCreator, IDropAcceptor, TreeModelListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/folder-database.png"));
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "library";	
	
	private final Vector<URI> mindmapIndex = new Vector<URI>();
	private final Vector<IBibtexDatabase> referencesIndex = new Vector<IBibtexDatabase>();

	private static WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLibraryNode() {
		this(TYPE);
	}
	
	public FolderTypeLibraryNode(String type) {
		super(type);
		DocearEvent event = new DocearEvent(this, DocearEventType.NEW_LIBRARY);
		DocearController.getController().dispatchDocearEvent(event);
		DocearController.getController().addDocearEventListener(this);
		WorkspaceController.getCurrentModel().addTreeModelListener(this);
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void initializePopup() {
		if (popupMenu  == null) {
			Controller controller = Controller.getCurrentController();
			controller.addAction(new DocearLibraryNewMindmap());
			//WORKSPACE - todo: implement
//			controller.removeAction(new DocearLibraryOpenLocation().getKey());
//			controller.addAction(new DocearLibraryOpenLocation());
			
			
			popupMenu = new WorkspacePopupMenu();
			
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.node.new.link",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.library.new.mindmap",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.open.location",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",						
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"
			});
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
		
	protected AWorkspaceTreeNode clone(FolderTypeLibraryNode node) {		
		for(IBibtexDatabase ref : referencesIndex) {
			node.addReferenceToIndex(ref);
		}
		for(URI uri : mindmapIndex) {
			node.addMindmapToIndex(uri);
		}
		return super.clone(node);
	}
	
	public AWorkspaceTreeNode clone() {
		FolderTypeLibraryNode node = new FolderTypeLibraryNode(getType());
		return clone(node);
	}
	
	protected void addMindmapToIndex(URI uri) {
		mindmapIndex.add(uri);
	}
	
	protected void addReferenceToIndex(IBibtexDatabase ref) {
		referencesIndex.clear();
		if(ref == null) {
			return;
		}
		referencesIndex.add(ref);
		DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_CHANGED, ref);
		DocearController.getController().dispatchDocearEvent(event);
	}
	
	/**
	 * @param file
	 * @return
	 */
	private AWorkspaceTreeNode createFSNodeLinks(File file) {
		AWorkspaceTreeNode node = null;
		AWorkspaceProject project = WorkspaceController.getCurrentModel().getProject(getModel());
		if(file.isDirectory()) {
			FolderLinkNode pNode = new FolderLinkNode();			
			pNode.setPath(project.getRelativeURI(file.toURI()));
			node = pNode;
		}
		else {
			LinkTypeFileNode lNode = new LinkTypeFileNode();
			lNode.setLinkPath(project.getRelativeURI(file.toURI()));
			node = lNode;
		}
		node.setName(file.getName());
		return node;
	}

	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			for(AWorkspaceTreeNode node : nodes) {
				AWorkspaceTreeNode newNode = null;
				if(node instanceof DefaultFileNode) {					
					newNode = createFSNodeLinks(((DefaultFileNode) node).getFile());
				}
				else {
					if(dropAction == DnDConstants.ACTION_COPY) {
						newNode = node.clone();
					} 
					else if (dropAction == DnDConstants.ACTION_MOVE) {
						AWorkspaceTreeNode parent = node.getParent();
						getModel().cutNodeFromParent(node);						
						parent.refresh();
						newNode = node;
					}
				}
				if(newNode == null) {
					continue;
				}
				getModel().addNodeTo(newNode, this);
				getModel().requestSave();
				//WORKSPACE - todo: handle node expands
				//WorkspaceController.getController().getExpansionStateHandler().addPathKey(this.getKey());
			}
			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {		
			for(File srcFile : files) {
				getModel().addNodeTo(createFSNodeLinks(srcFile), this);		
			}
			getModel().requestSave();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processUriListDrop(List<URI> uris, int dropAction) {
		try {			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				getModel().addNodeTo(createFSNodeLinks(srcFile), this);
			};
			getModel().requestSave();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
		
	}	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding mindmap to library: "+ uri);
					addMindmapToIndex(uri);
				}
			}			
		} 
		else if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof IBibtexDatabase) {
				if(!referencesIndex.contains((IBibtexDatabase) event.getEventObject())) {
					LogUtils.info("DOCEAR: adding reference database to library: "+ event.getEventObject());
					addReferenceToIndex((IBibtexDatabase) event.getEventObject());
				}
			}			
		}
		else if(event.getType() == DocearEventType.LIBRARY_EMPTY_MINDMAP_INDEX_REQUEST) {
			mindmapIndex.removeAllElements();			
		}
	}
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
		
	}
	
	public List<URI> getMindmaps() {
		return mindmapIndex;
	}
	
	public URI getBibtexDatabase() {
		URI uri = null;
		if(referencesIndex.size() > 0) {
			return referencesIndex.get(0).getUri();
		}
		return uri;
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
	
	
	
	public Transferable getTransferable() {
		return null;
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		
		return popupMenu;
	}

	public URI getPath() {
		// this is a virtual folder, no path is needed
		return null;
	}
	
	public URI getLibraryPath() {
		DocearWorkspaceProject project = (DocearWorkspaceProject) WorkspaceController.getCurrentModeExtension().getModel().getProject(getModel());
		return MModeWorkspaceLinkController.getController().getProjectRelativeURI(project, project.getProjectLibraryPath());
	}
	
	public void treeNodesChanged(TreeModelEvent e) {		
	}
	
	//WORKSPACE - info
	public void treeNodesInserted(TreeModelEvent event) {
		if(this.getTreePath().isDescendant(event.getTreePath())) {
			for(Object newNode : event.getChildren()) {
				if(newNode instanceof DefaultFileNode) {
					URI uri = ((DefaultFileNode)newNode).getFile().toURI();
					addToIndex(uri);
				} 
				else
				if(newNode instanceof LinkTypeFileNode && ((LinkTypeFileNode)newNode).getLinkPath() != null) {
					URI uri = WorkspaceController.resolveURI(((LinkTypeFileNode)newNode).getLinkPath());
					addToIndex(uri);
				}
			}
		}
	}

	private void addToIndex(URI uri) {
		if((new File(uri)).getName().endsWith(".mm") && !mindmapIndex.contains(uri)) {
			LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
			mindmapIndex.add(uri);	
		}
	}

	public void treeNodesRemoved(TreeModelEvent event) {
		//TODO: propagate other filetypes
		if(this.getTreePath().isDescendant(event.getTreePath())) {
			for(Object newNode : event.getChildren()) {
				if(newNode instanceof DefaultFileNode) {
					URI uri = ((DefaultFileNode)newNode).getFile().toURI();
					removeFromIndex(uri);
				} 
				else
				if(newNode instanceof LinkTypeFileNode) {
					URI uri = WorkspaceController.resolveURI(((LinkTypeFileNode)newNode).getLinkPath());
					removeFromIndex(uri);
				}
			}
		}
		
	}

	private void removeFromIndex(URI uri) {
		if((new File(uri)).getName().endsWith(".mm") && mindmapIndex.contains(uri)) {
			LogUtils.info("DOCEAR: mindmap removed from library: "+ uri);
			mindmapIndex.remove(uri);	
		}
	}
	
	public void treeStructureChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	

	
	
	
}
