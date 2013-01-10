package org.docear.plugin.core.listeners;

import org.docear.plugin.core.features.DocearNodeMovedExtensionController;
import org.freeplane.features.map.AMapChangeListenerAdapter;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

public class MapChangeListenerAdapter extends AMapChangeListenerAdapter {

	public void mapChanged(MapChangeEvent event) {
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {	
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		DocearNodeMovedExtensionController.getController().updateMovedTime(child);
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void nodeChanged(NodeChangeEvent event) {
		
	}

	public void onCreate(MapModel map) {
	}

	public void onRemove(MapModel map) {
	}

	public void onSavedAs(MapModel map) {
	}

	public void onSaved(MapModel map) {
	}

}
