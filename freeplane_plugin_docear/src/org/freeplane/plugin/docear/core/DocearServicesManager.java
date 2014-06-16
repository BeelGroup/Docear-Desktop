package org.freeplane.plugin.docear.core;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.freeplane.core.util.LogUtils;
import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.plugin.docear.core.concurrent.DocearConcurrencyController;
import org.freeplane.plugin.docear.core.extension.DocearExtension;
import org.freeplane.plugin.docear.core.spi.DocearService;
import org.freeplane.plugin.docear.util.DocearLogger;

/**
 * @author genzmehr@docear.org
 *
 */
final class DocearServicesManager {

	private DocearServicesClassLoader servicesClassLoader;
	private ServiceLoader<DocearService> loader;

	DocearServicesManager() {
		// create own url classloader to be able to add packages later
		servicesClassLoader = new DocearServicesClassLoader(Thread.currentThread().getContextClassLoader());
		//
		DocearConcurrencyController.getInstance().setDefaultClassLoader(servicesClassLoader);
		loader = ServiceLoader.load(DocearService.class, servicesClassLoader);
	}
	
	protected void dispatchServiceAction(DocearServiceAction action) {
		try {
            Iterator<DocearService> services = loader.iterator();
          //TODO - determine order by dependencies!?
            while (services.hasNext()) {
            	DocearService ds = services.next();
                if(action.isTarget(ds)) {
                	action.execute(ds);            
            	}
            }
        } catch (ServiceConfigurationError serviceError) {
            LogUtils.warn(serviceError); 
        }
	}
	
	private void installServices(DocearServiceContext context) {
		DocearLogger.info("preparing services installation...");
		dispatchServiceAction(new InstallServiceAction(context));
	}
	
	private void uninstallServices(DocearServiceContext context) {
		DocearLogger.info("preparing services shutdown...");
		dispatchServiceAction(new UninstallServiceAction(context));
	}

	private void loadServices() {
		if (null == System.getProperty("org.docear.core.services.dir", null)) {
			final File root = new File(FreeplaneGUIStarter.getResourceBaseDir()).getAbsoluteFile().getParentFile();
			try {
				String rootUrl = root.toURI().toURL().toString();
				if (!rootUrl.endsWith("/")) {
					rootUrl = rootUrl + "/";
				}
				final String servicesDir = rootUrl + "services/";
				System.setProperty("org.docear.core.services.dir", servicesDir);
			} catch (MalformedURLException ignore) {
			}
		}

		File serviceDir = new File(System.getProperty("org.docear.core.services.dir", null));
		if (serviceDir.exists() && serviceDir.isDirectory()) {
			final File[] childFiles = serviceDir.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				final File child = childFiles[i];
				if (child.getName().toLowerCase().endsWith(".jar")) {
					try {
						servicesClassLoader.addURL(child.toURI().toURL());
					} catch (MalformedURLException ignore) {
					}
				}
			}
			File serviceLibDir = new File(serviceDir, "lib");
			loadServicesLibs(serviceLibDir);
		}
		loader.reload();
	}

	/**
	 * recursively loads all jar files from the given directory and all sub-directories 
	 * @param libDir directory to start the search for jar files
	 */
	private void loadServicesLibs(File libDir) {
		if (libDir.exists() && libDir.isDirectory()) {
			final File[] childFiles = libDir.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				final File child = childFiles[i];
				if (child.isFile() && child.getName().toLowerCase().endsWith(".jar")) {
					try {
						servicesClassLoader.addURL(child.toURI().toURL());
					} catch (MalformedURLException ignore) {
					}
				}
				else {
					if(child.isDirectory()) {
						loadServicesLibs(child);
					}
				}
			}
		}
	}

	protected void initializeServices(DocearServiceContext context) {
		loadServices();		
		installServices(context);
	}
	
	public void shutdown() {
		uninstallServices(new DocearServiceContext() {
			public void registerCoreExtension(DocearExtension extension) {}
		});		
	}
	
	class UninstallServiceAction extends DocearServiceAction {
		private final DocearServiceContext context;
		
		public UninstallServiceAction(DocearServiceContext ctx) {
			this.context = ctx;
		}

		public void execute(DocearService ds) {
			ds.uninstall(this.context);
		}
		
	}
	
	class InstallServiceAction extends DocearServiceAction {
		private final DocearServiceContext context;
		
		public InstallServiceAction(DocearServiceContext ctx) {
			this.context = ctx;
		}

		public void execute(DocearService ds) {
			ds.install(this.context);
		}
		
	}
}
