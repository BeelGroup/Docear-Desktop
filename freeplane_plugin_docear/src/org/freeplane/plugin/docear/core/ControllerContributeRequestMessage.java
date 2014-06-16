package org.freeplane.plugin.docear.core;

import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;
import org.freeplane.plugin.docear.core.msg.DocearMessage;

public final class ControllerContributeRequestMessage extends DocearMessage {

	private final Controller controller;

	public ControllerContributeRequestMessage(DocearExtensionIdentifier src, Controller controller) {
		super(src);
		this.controller = controller;
	}
	
	public Controller getController() {
		return this.controller;
	}

}
