package org.freeplane.plugin.docear;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private BundleActivator activatorImpl;
	
	public void start(BundleContext bundleContext) throws Exception {
		activatorImpl = new DocearOsgiStarter();
		activatorImpl.start(bundleContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		activatorImpl.stop(bundleContext);
	}
}
