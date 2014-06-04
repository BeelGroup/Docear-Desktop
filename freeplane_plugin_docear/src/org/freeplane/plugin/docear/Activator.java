package org.freeplane.plugin.docear;

import org.freeplane.plugin.docear.core.DocearCore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private DocearCore core;

	public void start(BundleContext bundleContext) throws Exception {
		core = DocearCore.getInstance();
		core.start(bundleContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		core.stop(bundleContext);
	}
}
