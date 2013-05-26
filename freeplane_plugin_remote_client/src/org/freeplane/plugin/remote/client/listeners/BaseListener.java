package org.freeplane.plugin.remote.client.listeners;

import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.WS;

public class BaseListener {

	private final ClientController clientController;

	public BaseListener(ClientController clientController) {
		this.clientController = clientController;
	}
	
	protected WS webservice() {
		return clientController.webservice();
	}

	protected boolean isUpdating() {
		return clientController.isUpdating();
	}

	protected User user() {
		return clientController.getUser();
	}
	
	protected boolean isListening() {
		return clientController.isListening();
	}
}
