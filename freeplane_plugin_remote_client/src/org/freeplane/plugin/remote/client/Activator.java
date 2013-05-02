package org.freeplane.plugin.remote.client;

import java.util.Hashtable;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private ClientController clientController;
	
	@Override
	public void start(BundleContext context) {
		registerToFreeplaneStart(context);
	}

	private void registerToFreeplaneStart(final BundleContext context) {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();

		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(), new IModeControllerExtensionProvider() {
			public void installExtension(ModeController modeController) {
				clientController =  new ClientController();
			}
		}, props);
	}

	@Override
	public void stop(BundleContext context) {
		clientController.stop();
	}
}
