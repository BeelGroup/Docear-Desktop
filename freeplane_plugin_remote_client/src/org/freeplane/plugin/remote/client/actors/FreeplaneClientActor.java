package org.freeplane.plugin.remote.client.actors;

import org.docear.messages.models.MapIdentifier;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;

import akka.actor.UntypedActor;

/**
 * Base actor for actors that require the ClientController
 *
 */
public abstract class FreeplaneClientActor extends UntypedActor {

	private final ClientController clientController;

	public FreeplaneClientActor(ClientController clientController) {
		super();
		this.clientController = clientController;
	}

	public ClientController getClientController() {
		return clientController;
	}
	
	protected User user() {
		return clientController.getUser();
	}
	
	protected MapIdentifier mapIdentifier() {
		return clientController.getMapIdentifier();
	}

}
