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
		if (!isUpdating()) {
			if (event != null && event.getProperty() != null)
				LogUtils.info("attribute: " + event.getProperty().toString());
			if (event != null && event.getNewValue() != null)
				LogUtils.info("value: " + event.getNewValue().toString());

			if (event.getProperty() != null) {
				final Object property = event.getProperty();
				if (property.toString().contains("FOLDING")) {
					LogUtils.info("folding");
					webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "folded", event.getNewValue());
//					final ListenableFuture<Boolean> future = webservice().changeNode("5", event.getNode().getID(), "folded", event.getNewValue());
//					Futures.addCallback(future, new FutureCallback<Boolean>() {
//
//						@Override
//						public void onFailure(Throwable t) {
//							t.printStackTrace();
//						}
//
//						@Override
//						public void onSuccess(Boolean success) {
//							if (!success) {
//								isUpdating(true);
//								event.getNode().setFolded(!(Boolean) event.getNewValue());
//								isUpdating(false);
//							}
//						}
//					});
				}
				// note
				else if(property.equals("note_text")) {
					LogUtils.info("note_text");
					webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "note", event.getNewValue());
				}
				// images
				else if (property.equals(ExternalResource.class)) {
					LogUtils.info("image");
					// TODO is not handled by the server side, yet.
					// use this code when handling is implemented 
					
					//final ExternalResource resource = (ExternalResource) event.getNewValue();
					//webservice().changeNode("5", event.getNode().getID(), "image", resource.getUri().toString());
				}
				// send all because real change is unknown (only every 5
				// seconds)
				else if (property.equals(NodeModel.UNKNOWN_PROPERTY)) {
					// Do nothing, because logic has changed
//					long nowMillis = System.currentTimeMillis();
//					if (lastMillis < 0 || nowMillis - lastMillis > 5000) {
//						lastMillis = nowMillis;
//						LogUtils.info("unkown property changed, creating diff");
//						NodeModelDefault now = new NodeModelDefault(event.getNode(), false);
//						Map<String, Object> attributeValueMap = getChangedAttributes(lastNodeState, now);
//						lastNodeState = now;

//					}
				}

			}
		}

	}

//	private long lastMillis = -1;
	public void updateCurrentState() {
		lastNodeState = new NodeModelDefault(model,false);
	}

	public Map<String, Object> getChangedAttributes() {
		final Map<String, Object> attributes = new HashMap<String, Object>();
		NodeModelDefault now = new NodeModelDefault(model,false);

		// nodeText is a recognized change
		// fold is a recognized change

		// moving is not recognized
		if (!lastNodeState.hGap.equals(now.hGap)) {
			LogUtils.info("hGap changed to " + now.hGap);
			attributes.put("hGap", now.hGap);
		}
		//
		if (!lastNodeState.shiftY.equals(now.shiftY)) {
			LogUtils.info("hGap changed to " + now.shiftY);
			attributes.put("shiftY", now.shiftY);
		}
		
		//links are not recognized
		if(lastNodeState.link == null && now.link != null) {
			attributes.put("link",now.link);
		} else if (lastNodeState.link != null && now.link != null && !lastNodeState.link.equals(now.link)) {
			attributes.put("link",now.link);
		} else if(lastNodeState.link != null && now.link == null) {
			attributes.put("link", null);
		}
		
		//EdgeStyles are not recognized
		
		
		lastNodeState = now;

		return attributes;
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
