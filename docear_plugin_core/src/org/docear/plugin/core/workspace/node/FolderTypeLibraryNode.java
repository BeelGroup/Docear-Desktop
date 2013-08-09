/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ILibraryRepository;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class FolderTypeLibraryNode extends AFolderNode implements ILibraryRepository, IDocearEventListener, IWorkspaceNodeActionListener, IWorkspaceTransferableCreator, TreeModelListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/folder-database.png"));
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "library";	
	
	private final Set<URI> mindmapIndex = new HashSet<URI>();
	private static WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLibraryNode() {
		this(TYPE);
	}
	
	public FolderTypeLibraryNode(String type) {
		super(type);
		DocearController.getController().getEventQueue().addEventListener(this);
		WorkspaceController.getCurrentModel().addTreeModelListener(this);
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void initializePopup() {
		if (popupMenu  == null) {
			popupMenu = new WorkspacePopupMenu();
			
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
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
		LogUtils.info("DOCEAR: adding mindmap to library: "+ uri);
		mindmapIndex.add(uri);
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					addMindmapToIndex(uri);
				}
			}			
		}
		else if(event.getType() == DocearEventType.LIBRARY_EMPTY_MINDMAP_INDEX_REQUEST) {
			mindmapIndex.clear();			
		}
	}
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
		
	}
	
	public WorkspaceTransferable getTransferable() {
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
	
	public void treeNodesChanged(TreeModelEvent e) {		
	}
	
	//WORKSPACE - info
	public void treeNodesInserted(TreeModelEvent event) {
		if(this.getTreePath().isDescendant(event.getTreePath())) {
			for(Object newNode : event.getChildren()) {
				URI uri = null;
				try {
					if(newNode instanceof LinkTypeIncomingNode) {
						uri = URIUtils.getAbsoluteURI(((LinkTypeIncomingNode)newNode).getLinkURI());
					}
					else
					if(newNode instanceof LinkTypeLiteratureAnnotationsNode) {
						uri = URIUtils.getAbsoluteURI(((LinkTypeLiteratureAnnotationsNode)newNode).getLinkURI());
					}
					else
					if(newNode instanceof LinkTypeMyPublicationsNode) {
						uri = URIUtils.getAbsoluteURI(((LinkTypeMyPublicationsNode)newNode).getLinkURI());
					}
					else
					if(newNode instanceof DefaultFileNode) {
						uri = ((DefaultFileNode)newNode).getFile().toURI();
					} 
					else
					if(newNode instanceof LinkTypeFileNode && ((LinkTypeFileNode)newNode).getLinkURI() != null) {
						uri = URIUtils.getAbsoluteURI(((LinkTypeFileNode)newNode).getLinkURI());
					}
				}
				catch (Exception e) {
					LogUtils.warn("Exception in "+ this.getClass() +".treeNodesInserted(): "+ e.getMessage() );
				}
				if(uri != null) {
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
					URI uri = URIUtils.getAbsoluteURI(((LinkTypeFileNode)newNode).getLinkURI());
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

	@Override
	public Collection<URI> getMaps() {
		return mindmapIndex;
	}	
	
}
