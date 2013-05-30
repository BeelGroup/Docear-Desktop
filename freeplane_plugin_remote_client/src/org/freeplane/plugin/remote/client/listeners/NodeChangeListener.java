package org.freeplane.plugin.remote.client.listeners;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.plugin.remote.client.ClientController;

public class NodeChangeListener extends BaseListener implements INodeChangeListener {

	public NodeChangeListener(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		if (!isUpdating() && isListening()) {
			if (event != null && event.getProperty() != null)
				LogUtils.info("attribute: " + event.getProperty().toString());
			if (event != null && event.getNewValue() != null)
				LogUtils.info("value: " + event.getNewValue().toString());

			if (event.getProperty() != null && event.getProperty().equals("node_text")) {
				LogUtils.info("node_text");
				webservice().changeNode(user(), mapIdentifier(), event.getNode().getID(), "nodeText", event.getNewValue());
			}
		}
	}
}
