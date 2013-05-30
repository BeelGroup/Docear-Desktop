package org.freeplane.plugin.remote.client.listeners;

import org.docear.messages.models.MapIdentifier;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.WS;

public class BaseListener {

	private final ClientController clientController;

	public BaseListener(ClientController clientController) {
		this.clientController = clientController;
	}

	public ClientController getClientController() {
		return clientController;
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
	
	protected MapIdentifier mapIdentifier() {
		return clientController.getMapIdentifier();
	}

	protected boolean isListening() {
		return clientController.isListening();
	}
}
