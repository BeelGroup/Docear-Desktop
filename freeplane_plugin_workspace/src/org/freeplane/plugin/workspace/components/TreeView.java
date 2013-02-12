package org.freeplane.plugin.workspace.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.freeplane.core.ui.components.OneTouchCollapseResizer.ComponentCollapseListener;
import org.freeplane.core.ui.components.ResizeEvent;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.handler.DefaultNodeTypeIconManager;
import org.freeplane.plugin.workspace.handler.DefaultWorkspaceKeyHandler;
import org.freeplane.plugin.workspace.handler.DefaultWorkspaceMouseHandler;
import org.freeplane.plugin.workspace.handler.INodeTypeIconManager;
import org.freeplane.plugin.workspace.listener.DefaultTreeExpansionListener;
import org.freeplane.plugin.workspace.listener.DefaultWorkspaceSelectionListener;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspaceModel;

public class TreeView extends JPanel implements IWorkspaceView, ComponentCollapseListener {
	private static final long serialVersionUID = 1L;
	private static final int view_margin = 3;
	
	protected JTree mTree;
	protected JTextField m_display;
	private WorkspaceTransferHandler transferHandler;
	private INodeTypeIconManager nodeTypeIconManager;
	
	public TreeView() {
		this.setLayout(new BorderLayout());

		mTree = new JTree();
		mTree.setBorder(BorderFactory.createEmptyBorder(2, view_margin, view_margin, view_margin));
		mTree.putClientProperty("JTree.lineStyle", "Angled");
		mTree.setCellRenderer(new WorkspaceNodeRenderer());
		mTree.setCellEditor(new WorkspaceCellEditor(mTree, (DefaultTreeCellRenderer) mTree.getCellRenderer()));
		mTree.addTreeExpansionListener(new DefaultTreeExpansionListener());
        mTree.addTreeSelectionListener(new DefaultWorkspaceSelectionListener());
		mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		DefaultWorkspaceMouseHandler mouseHandler = new DefaultWorkspaceMouseHandler();
		mTree.addMouseListener(mouseHandler);
		mTree.addMouseMotionListener(mouseHandler);
		mTree.addKeyListener(new DefaultWorkspaceKeyHandler());
		mTree.setRowHeight(18);
		mTree.setShowsRootHandles(false);
		mTree.setEditable(true);
		
		this.transferHandler = WorkspaceTransferHandler.configureDragAndDrop(mTree);
		
				
		this.add(new JScrollPane(mTree), BorderLayout.CENTER);
		
		// TODO: DOCEAR - choose actions to use in Toolbar
//		WorkspaceToolBar workspaceToolBar = new WorkspaceToolBar();
//		add(workspaceToolBar, BorderLayout.NORTH);
	}
	
	public void addTreeMouseListener(MouseListener l) {
		this.mTree.addMouseListener(l);
	}

	public void addTreeComponentListener(ComponentListener l) {
		this.mTree.addComponentListener(l);
	}
	
	public void setPreferredSize(Dimension size) {
		super.setPreferredSize(new Dimension(Math.max(size.width, getMinimumSize().width), Math.max(size.height, getMinimumSize().height)));	
	}

	public void expandPath(TreePath treePath) {
		mTree.expandPath(treePath);		
	}

	public void collapsePath(TreePath treePath) {
		mTree.collapsePath(treePath);		
	}

	public void setModel(WorkspaceModel model) {
		if(model instanceof TreeModel) {
			mTree.setModel((TreeModel) model);
		}
		else {
			mTree.setModel(new TreeModelProxy(model));
		}
		
	}
	
	public WorkspaceTransferHandler getTransferHandler() {
		return this.transferHandler;
	}
	
	public void addSelectionPath(TreePath path) {
		mTree.addSelectionPath(path);		
	}
		
	public class TreeModelProxy implements TreeModel {
		private final WorkspaceModel model;

		public TreeModelProxy(WorkspaceModel model) {
			this.model = model;
		}

		public Object getRoot() {
			if(model == null) return null;
			return model.getRoot();
		}

		public Object getChild(Object parent, int index) {
			if(parent == null) return null;
			return ((AWorkspaceTreeNode) parent).getChildAt(index);			
		}

		public int getChildCount(Object parent) {
			if(parent == null) return 0;
			return ((AWorkspaceTreeNode) parent).getChildCount();
		}

		public boolean isLeaf(Object node) {
			return ((AWorkspaceTreeNode) node).isLeaf();
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			AWorkspaceTreeNode node = (AWorkspaceTreeNode) path.getLastPathComponent();
			if (node instanceof IWorkspaceNodeActionListener) {
				((IWorkspaceNodeActionListener) node).handleAction(new WorkspaceActionEvent(node, WorkspaceActionEvent.WSNODE_CHANGED, newValue));
				//nodeChanged(node);
			}
			else {
				node.setName(newValue.toString());
			}
		}

		public int getIndexOfChild(Object parent, Object child) {
			return ((AWorkspaceTreeNode) parent).getIndex((TreeNode) child);
		}

		public void addTreeModelListener(TreeModelListener l) {
			// TODO Auto-generated method stub

		}

		public void removeTreeModelListener(TreeModelListener l) {
			// TODO Auto-generated method stub

		}

	}

	public boolean containsComponent(Component comp) {
		if(this.equals(comp)) {
			return true;
		}
		else if(mTree.equals(comp)) {
			return true;
		}
		return false;
	}

	public TreePath getSelectionPath() {
		return mTree.getSelectionPath();
	}

	public TreePath getPathForLocation(int x, int y) {
		return mTree.getPathForLocation(x, y);
	}

	public INodeTypeIconManager getNodeTypeIconManager() {
		if(nodeTypeIconManager == null) {
			nodeTypeIconManager = new DefaultNodeTypeIconManager();
		}
		return nodeTypeIconManager;
	}

	public void addPathKey(String key) {
		//DOCEAR - modify: use TreePath as key
	}

	public void componentCollapsed(ResizeEvent event) {
		if(this.equals(event.getSource())) {
			super.setPreferredSize(new Dimension(0, getPreferredSize().height));
		}
	}

	public void componentExpanded(ResizeEvent event) {
		if(this.equals(event.getSource())) {
			// nothing
		}
	}

	public AWorkspaceTreeNode getNodeForLocation(int x, int y) {
		TreePath path = mTree.getPathForLocation(x, y);
		if(path == null) {
			return null;
		}
		return (AWorkspaceTreeNode) path.getLastPathComponent();		
	}	
	
}
