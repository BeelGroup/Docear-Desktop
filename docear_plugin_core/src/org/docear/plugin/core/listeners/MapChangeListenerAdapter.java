package org.docear.plugin.core.listeners;

import java.awt.Component;

import org.docear.plugin.core.features.DocearNodeModifiedExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController.DocearNodePrivacyExtension;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;

public class MapChangeListenerAdapter implements IMapLifeCycleListener, INodeSelectionListener, INodeView, IMapViewChangeListener {

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
//		LogUtils.info("onNodeInserted: "+ child);
//		try {
//			child.removeViewer(this);
//		}
//		catch (Exception e) {
//		}
//		child.addViewer(this);
	}
	
	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
//		LogUtils.info("onPreNodeDelete: "+ selectedNode);
	}
	
	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
//		LogUtils.info("onNodeDeleted: "+ child);
//		try {
//			child.removeViewer(this);
//		}
//		catch (Exception e) {
//		}
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}
	
	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		DocearNodeModifiedExtensionController.getController().updateMovedTime(child);
//		LogUtils.info("nodeMoved: "+child);
	}

	public void nodeChanged(NodeChangeEvent event) {
//		LogUtils.info("nodeChanged: "+event.getProperty());
	}
	
	public void onDeselect(NodeModel node) {
//		LogUtils.info("onDeselect "+node);
	}

	public void onSelect(NodeModel node) {
		DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(node);
		if(ext != null) {
			Controller.getCurrentController().getViewController().addStatusInfo("Docear Privacy", "Privacy "+ext.getPrivacyLevel().toString());
		}
		
//		LogUtils.info("onSelect "+node);
//		try {
//			node.removeViewer(this);
//		}
//		catch (Exception e) {
//		}
//		node.addViewer(this);
		
		
	}

	/*************************************************
	 * interface for map events
	 *************************************************/
	/**
	 * 
	 */
	public void mapChanged(MapChangeEvent event) {
//		LogUtils.info("mapChanged: "+event.getProperty());
	}
	
	public void onCreate(MapModel map) {
		map.addMapChangeListener(this);
//		LogUtils.info("onCreate");
	}

	public void onRemove(MapModel map) {
		map.removeMapChangeListener(this);
//		LogUtils.info("onRemove");
	}

	public void onSavedAs(MapModel map) {
	}

	public void onSaved(MapModel map) {
	}

	@Override
	public void afterViewChange(Component oldView, Component newView) {
//		LogUtils.info("afterViewChange");
	}

	@Override
	public void afterViewClose(Component oldView) {
//		LogUtils.info("afterViewClose");
	}

	@Override
	public void afterViewCreated(Component mapView) {
//		LogUtils.info("afterViewCreated");
	}

	@Override
	public void beforeViewChange(Component oldView, Component newView) {
//		LogUtils.info("beforeViewChange");
	}

	

}
