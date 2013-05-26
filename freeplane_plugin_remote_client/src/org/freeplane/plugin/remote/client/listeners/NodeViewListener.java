package org.freeplane.plugin.remote.client.listeners;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.WS;
import org.freeplane.plugin.remote.v10.model.EdgeModel;
import org.freeplane.plugin.remote.v10.model.NodeModelDefault;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

@SuppressWarnings("serial")
public class NodeViewListener extends NodeView implements INodeView {
	private final ClientController clientController;
	private NodeModelDefault lastNodeState;
	private final NodeModel model;

	public NodeViewListener(ClientController clientController, NodeModel model, MapView map, Container parent) {
		super(model, map, parent);
		lastNodeState = new NodeModelDefault(model, false);
		this.model = model;
		this.clientController = clientController;
	}

	@Override
	public void addContent(JComponent component, int pos) {
		// override to do nothing
	}

	@Override
	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// not important
	}

	@Override
	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
		// not important
	}

	@Override
	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// not important
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// not important
	}

	@Override
	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		// not important
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		// not important
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {

		LogUtils.info("nodeChange called");
		if (!isUpdating() && clientController.isListening()) {
			if (event != null && event.getProperty() != null)
				LogUtils.info("attribute: " + event.getProperty().toString());
			if (event != null && event.getNewValue() != null)
				LogUtils.info("value: " + event.getNewValue().toString());

			if (event.getProperty() != null) {
				final Object property = event.getProperty();
				if (property.toString().contains("FOLDING")) {
					LogUtils.info("folding");
					webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "folded", event.getNewValue());
				}
				// note
				else if (property.equals("note_text")) {
					LogUtils.info("note_text");
					webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "note", event.getNewValue());
				}
				// images
				else if (property.equals(ExternalResource.class)) {
					LogUtils.info("image");
					// TODO is not handled by the server side, yet.
					// use this code when handling is implemented

					// final ExternalResource resource = (ExternalResource)
					// event.getNewValue();
					// webservice().changeNode("5", event.getNode().getID(),
					// "image", resource.getUri().toString());
				}
				// link
				else if (property.equals("hyperlink_changed")) {
					LogUtils.info("link");
					webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "link", event.getNewValue());
				}
				else if (property.equals(NodeModel.UNKNOWN_PROPERTY)) {
					// Do nothing, because logic has changed
				}

			}
		}

	}

	// private long lastMillis = -1;
	public void updateCurrentState() {
		lastNodeState = new NodeModelDefault(model, false);
	}

	public NodeChangeData getChangedAttributes() {
		final NodeChangeData data = new NodeChangeData();
		NodeModelDefault now = new NodeModelDefault(model, false);

		// node text is a recognized change
		// note text is a recognized change
		// fold is a recognized change

		// moving is not recognized
		if (!lastNodeState.hGap.equals(now.hGap)) {
			LogUtils.info("hGap changed to " + now.hGap);
			data.putNodeChange("hGap", now.hGap);
		}
		//
		if (!lastNodeState.shiftY.equals(now.shiftY)) {
			LogUtils.info("hGap changed to " + now.shiftY);
			data.putNodeChange("shiftY", now.shiftY);
		}

		// links are not recognized
		if(isValueUpdated(lastNodeState.link, now.link)) {
			data.putNodeChange("link", now.link);
		}

		// EdgeStyles are not recognized
		final EdgeModel oldEdge = lastNodeState.edgeStyle;
		final EdgeModel newEdge = now.edgeStyle;
		if(isValueUpdated(oldEdge.color, newEdge.color)) {
			data.putEdgeChange("color", newEdge.color);
		}
		if(isValueUpdated(oldEdge.width, newEdge.width)) {
			data.putEdgeChange("width", newEdge.width);
		}
		if(isValueUpdated(oldEdge.style, newEdge.style)) {
			data.putEdgeChange("style", newEdge.style);
		}
		
		
		
		
		lastNodeState = now;

		return data;
	}

	private <T> boolean isValueUpdated(T oldValue, T newValue) {
		if (oldValue == null && newValue != null) {
			return true;
		} else if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
			return true;
		} else if (oldValue != null && newValue == null) {
			return true;
		}
		
		return false;
	}
	
	public static final class NodeChangeData {
		private final Map<String, Object> nodeChanges;
		private final Map<String, Object> edgeChanges;

		public NodeChangeData() {
			this.nodeChanges = new HashMap<String, Object>();
			this.edgeChanges = new HashMap<String, Object>();
		}

		public Map<String, Object> getNodeChanges() {
			return nodeChanges;
		}

		public void putNodeChange(String key, Object value) {
			nodeChanges.put(key, value);
		}

		public Map<String, Object> getEdgeChanges() {
			return edgeChanges;
		}

		public void putEdgeChange(String key, Object value) {
			edgeChanges.put(key, value);
		}
	}

	private WS webservice() {
		return clientController.webservice();
	}

	private boolean isUpdating() {
		return clientController.isUpdating();
	}

	private User user() {
		return clientController.getUser();
	}
}
