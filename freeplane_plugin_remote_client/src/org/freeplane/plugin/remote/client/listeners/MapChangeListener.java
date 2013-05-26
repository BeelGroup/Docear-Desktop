package org.freeplane.plugin.remote.client.listeners;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.remote.client.ClientController;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class MapChangeListener extends BaseListener implements IMapChangeListener {
	public MapChangeListener(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		if (!isUpdating() && isListening()) {
			LogUtils.info("Node Moved. Sending to Webservice");
			webservice().moveNodeTo(user().getUsername(), user().getAccessToken(), "5", newParent.getID(), child.getID(), newIndex);
		}
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		if (!isUpdating() && isListening()) {
			LogUtils.info("Node Added. Sending to Webservice");
			try {
				final String newNodeId = Await.result(webservice().createNode(user().getUsername(), user().getAccessToken(), "5", parent.getID()), Duration.create("10 seconds"));
				child.setID(newNodeId);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		if (!isUpdating() && isListening()) {
			LogUtils.info("Node Deleted. Sending to Webservice");
			webservice().removeNode(user().getUsername(), user().getAccessToken(), "5", child.getID());
		}

	}

	@Override
	public void mapChanged(MapChangeEvent event) {

	}

	
}
