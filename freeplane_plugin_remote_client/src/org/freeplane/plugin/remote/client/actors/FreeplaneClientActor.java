package org.freeplane.plugin.remote.client.actors;

import org.freeplane.plugin.remote.client.ClientController;

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

}
