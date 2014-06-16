package org.freeplane.plugin.docear.core;

import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;
import org.freeplane.plugin.docear.core.msg.DocearMessage;

public final class ModeContributeRequestMessage extends DocearMessage {

	private final ModeController modeController;

	public ModeContributeRequestMessage(DocearExtensionIdentifier src, ModeController modeController) {
		super(src);
		this.modeController = modeController;
	}
	
	public ModeController getModeController() {
		return this.modeController;
	}

}
