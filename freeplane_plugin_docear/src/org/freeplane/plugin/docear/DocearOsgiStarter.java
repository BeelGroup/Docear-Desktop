package org.freeplane.plugin.docear;

import java.util.Hashtable;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.docear.core.ControllerContributeRequestMessage;
import org.freeplane.plugin.docear.core.DocearCore;
import org.freeplane.plugin.docear.core.ModeContributeRequestMessage;
import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;
import org.freeplane.plugin.docear.util.DocearLogger;
import org.freeplane.plugin.docear.util.Version;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author genzmehr@docear.org
 *
 */
final class DocearOsgiStarter implements BundleActivator {
	
	public static final DocearExtensionIdentifier STARTER_ID = new DocearExtensionIdentifier("org.docear.core.starter", new Version("1.0.0"));
	
	public void start(BundleContext context) {
		
		DocearCore.getInstance().startup();
		
		CoreControllerExtensionProvider ctrlExtProvider = new CoreControllerExtensionProvider(context);
		ctrlExtProvider.register();		
	}
	
	public void stop(BundleContext context) {
		DocearCore.getInstance().shutdown();
	}
	
	final class CoreControllerExtensionProvider implements IControllerExtensionProvider {
		
		private BundleContext context;

		public CoreControllerExtensionProvider(BundleContext context) {
			this.context = context;
		}
		
		public void register() {
			context.registerService(IControllerExtensionProvider.class.getName(), this, null);
		}

		@Override
		public void installExtension(Controller controller) {
			DocearLogger.info("requesting controller extensions ...");
			DocearCore.getInstance().dispatchMessage(new ControllerContributeRequestMessage(STARTER_ID, controller));
			
			CoreModeExtensionProvider.register(context, controller);
		}
		
	}
	
	
	final static class CoreModeExtensionProvider implements IModeControllerExtensionProvider {
				
		public static void register(BundleContext context, Controller controller) {
			final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
			props.put("mode", controller.getModes().toArray(new String[0]));
			context.registerService(IModeControllerExtensionProvider.class.getName(), new CoreModeExtensionProvider(), props);
		}

		@Override
		public void installExtension(ModeController modeController) {
			DocearLogger.info("requesting extensions for " + modeController.getModeName() + "...");
			DocearCore.getInstance().dispatchMessage(new ModeContributeRequestMessage(STARTER_ID, modeController));
		}
		
	}
}
