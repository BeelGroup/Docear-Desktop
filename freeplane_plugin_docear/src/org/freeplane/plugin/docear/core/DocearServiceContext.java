package org.freeplane.plugin.docear.core;

import java.util.Hashtable;

import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;

public interface DocearServiceContext {
	
	public void registerControllerExtensionProvider(IControllerExtensionProvider provider);
	
	public void registerModeControllerExtensionProvider(IModeControllerExtensionProvider provider, Hashtable<String, String[]> props);
	
	public void registerCoreExtension(DocearExtension extension);
}
