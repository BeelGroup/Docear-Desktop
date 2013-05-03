package org.freeplane.plugin.remote.client.listeners;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.WS;

public class NodeChangeListener implements INodeChangeListener {
	private final ClientController clientController;

	public NodeChangeListener(ClientController clientController) {
		super();
		this.clientController = clientController;
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		if (!isUpdating()) {
			if (event != null && event.getProperty() != null)
				LogUtils.info("attribute: " + event.getProperty().toString());
			if (event != null && event.getNewValue() != null)
				LogUtils.info("value: " + event.getNewValue().toString());

			if (event.getProperty() != null && event.getProperty().equals("node_text")) {
				LogUtils.info("node_text");
				webservice().changeNode(user().getUsername(), user().getAccessToken(), "5", event.getNode().getID(), "nodeText", event.getNewValue());
				// final ListenableFuture<Boolean> future =
				// webservice().changeNode("5", event.getNode().getID(),
				// "nodeText", event.getNewValue());
				// Futures.addCallback(future, new FutureCallback<Boolean>() {
				// @Override
				// public void onFailure(Throwable t) {
				// t.printStackTrace();
				// }
				//
				// @Override
				// public void onSuccess(Boolean success) {
				// if (!success) {
				// isUpdating(true);
				// event.getNode().setText(event.getOldValue().toString());
				// isUpdating(false);
				// }
				// }
				// });
			}
			// node_text

		}
	}

	public WS webservice() {
		return clientController.webservice();
	}

	public boolean isUpdating() {
		return clientController.isUpdating();
	}

	public void isUpdating(boolean value) {
		clientController.isUpdating(value);
	}
	
	private User user() {
		return clientController.getUser();
	}
}
