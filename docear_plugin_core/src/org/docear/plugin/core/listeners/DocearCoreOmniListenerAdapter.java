package org.docear.plugin.core.listeners;

import java.awt.Component;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearMapModelExtension.DocearMapType;
import org.docear.plugin.core.features.DocearNodeModifiedExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController.DocearNodePrivacyExtension;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController.DocearPrivacyLevel;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;

public class DocearCoreOmniListenerAdapter implements IMapLifeCycleListener, INodeSelectionListener, INodeView, IMapViewChangeListener, IDocearEventListener {

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
		if("true".equals(System.getProperty("docear.debug", "false"))) {
			DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(node);
			if(ext != null) {
				Controller.getCurrentController().getViewController().addStatusInfo("DocearPrivacy", "Privacy: "+ext.getPrivacyLevel().toString());
			}
			else {
				Controller.getCurrentController().getViewController().removeStatus("DocearPrivacy");
			}
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
	}
	
	public void onCreate(MapModel map) {
		map.addMapChangeListener(this);
		
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme != null && dmme.getType() != null) {
			DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
			if(ext == null) {
				DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
			}
		}
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

	/***********************************************
	 * Docear event handling
	 ***********************************************/
	
	@Override
	public void handleEvent(DocearEvent event) {
		if (DocearEventType.NEW_INCOMING.equals(event.getType())) {
			MapModel map = (MapModel) event.getEventObject();
			DocearMapModelExtension dmme = DocearMapModelController.getModel(map);
			if(dmme == null) {
				dmme = DocearMapModelController.setModelWithCurrentVersion(map);
			}
			dmme.setType(DocearMapType.incoming);
			DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
			if(ext == null) {
				DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
			}
		}
		if (DocearEventType.NEW_MY_PUBLICATIONS.equals(event.getType())) {
			MapModel map = (MapModel) event.getEventObject();
			DocearMapModelExtension dmme = DocearMapModelController.getModel(map);
			if(dmme == null) {
				dmme = DocearMapModelController.setModelWithCurrentVersion(map);
			}
			dmme.setType(DocearMapType.my_publications);
			DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
			if(ext == null) {
				DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
			}
		}
		if (DocearEventType.NEW_LITERATURE_ANNOTATIONS.equals(event.getType())) {
			MapModel map = (MapModel) event.getEventObject();
			DocearMapModelExtension dmme = DocearMapModelController.getModel(map);
			if(dmme == null) {
				dmme = DocearMapModelController.setModelWithCurrentVersion(map);
			}
			dmme.setType(DocearMapType.literature_annotations);
			DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
			if(ext == null) {
				DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
			}
		}		
	}

}
