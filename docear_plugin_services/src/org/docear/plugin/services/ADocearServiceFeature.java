package org.docear.plugin.services;

import org.freeplane.features.mode.ModeController;


public abstract class ADocearServiceFeature {
	
	protected abstract void installDefaults(ModeController modeController);
	
	public abstract void shutdown();
}
