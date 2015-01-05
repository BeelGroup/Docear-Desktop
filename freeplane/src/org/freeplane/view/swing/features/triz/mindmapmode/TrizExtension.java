package org.freeplane.view.swing.features.triz.mindmapmode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class TrizExtension implements IExtension, IMapChangeListener{

	public static TrizExtension getExtension(final NodeModel node) {
		return (TrizExtension) node.getExtension(TrizExtension.class);
	}
	
	private final NodeModel node;
	public TrizExtension(final NodeModel node) {
		this.node = node;
	}
	
	private void displayStateIcon(final NodeModel parent, final TrizState state) {
		displayState(state, parent, true);
	}
	
	private boolean isAncestorNode(final NodeModel parent) {
		for (NodeModel n = node; n != null; n = n.getParentNode()) {
			if (n.equals(parent)) {
				return true;
			}
		}
		return false;
	}
	
	public void mapChanged(MapChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		// TODO Auto-generated method stub
		
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		displayStateIcon(parent, TrizState.TRIZ_VISIBLE);
		
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex,
			NodeModel newParent, NodeModel child, int newIndex) {
		displayStateIcon(newParent, TrizState.TRIZ_VISIBLE);
		
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex,
			NodeModel newParent, NodeModel child, int newIndex) {
		displayStateIcon(oldParent, null);
		
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode,
			int index) {
		displayStateIcon(oldParent, null);
		
	}
	
	public NodeModel getNode() {
		return node;
	}

	public void displayState(final TrizState stateAdded, final NodeModel pNode,
            final boolean recurse) {
		if(stateAdded != null)
			pNode.putExtension(stateAdded);
		else
			pNode.removeExtension(TrizState.class);
		Controller.getCurrentModeController().getMapController().nodeRefresh(pNode);
		if (!recurse) {
			return;
		}
		final NodeModel parentNode = pNode.getParentNode();
		if (parentNode == null) {
			return;
		}
		displayState(stateAdded, parentNode, recurse);
	}
	
	public int getWorseingFeatures() {
		return worseingFeatures;
	}

	public void setWorseingFeatures(int worseingFeatures) {
		this.worseingFeatures = worseingFeatures;
	}

	public int getImprovingFeatures() {
		return improvingFeatures;
	}

	public void setImprovingFeatures(int improvingFeatures) {
		this.improvingFeatures = improvingFeatures;
	}

	private int worseingFeatures;
    private int improvingFeatures;
}
