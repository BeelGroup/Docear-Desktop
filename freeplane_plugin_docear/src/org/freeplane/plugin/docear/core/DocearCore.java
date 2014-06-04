package org.freeplane.plugin.docear.core;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.docear.core.spi.DocearService;
import org.osgi.framework.BundleContext;

public class DocearCore {
	private static DocearCore core;

	private DocearServicesClassLoader servicesClassLoader;
	private ServiceLoader<DocearService> loader;

	private DocearCore() {
		servicesClassLoader = new DocearServicesClassLoader(Thread.currentThread().getContextClassLoader());
		loader = ServiceLoader.load(DocearService.class, servicesClassLoader);
	}

	public static DocearCore getInstance() {
		if (core == null) {
			core = new DocearCore();
		}
		return core;
	}

	public void start(BundleContext context) {
		loadServices();
		try {
			Class<?> cls = Class.forName("org.docear.desktop.service.literature.LiteratureService", true, servicesClassLoader);
			System.out.println(cls);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		startServices(newServiceContext(context));
		
		context.registerService(IControllerExtensionProvider.class.getName(), new IControllerExtensionProvider() {
			public void installExtension(Controller controller) {
				LogUtils.info("Workspace controller installed.");
				//startControllerServices(context, controller);
			}
		}, null);

		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		// TODO (low) list all available modes from freeplane controller
		props.put("mode", new String[] { MModeController.MODENAME });
		// ModeExtensions
		context.registerService(IModeControllerExtensionProvider.class.getName(), new IModeControllerExtensionProvider() {
			public void installExtension(ModeController modeController) {
				//startModeServices(context, modeController);
			}
		}, props);

	}

	private void startServices(DocearServiceContext context) {
		try {
            Iterator<DocearService> services = loader.iterator();
            while (services.hasNext()) {
            	DocearService ds = services.next();
                ds.start(context);            
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }
	}
	
	private void stopServices(DocearServiceContext context) {
		try {
            Iterator<DocearService> services = loader.iterator();
            while (services.hasNext()) {
            	DocearService ds = services.next();
                ds.stop(context);            }
        } catch (ServiceConfigurationError serviceError) {
            LogUtils.warn(serviceError); 
        }
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
		System.out.println(serviceDir.getAbsolutePath());
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
		}
		loader.reload();
	}

	public void stop(BundleContext context) {
		stopServices(newServiceContext(context));
	}

	private DocearServiceContext newServiceContext(final BundleContext context) {
		return new DocearServiceContext() {

			@Override
			public void registerControllerExtensionProvider(IControllerExtensionProvider provider) {
				context.registerService(IControllerExtensionProvider.class.getName(), provider, null);
			}

			@Override
			public void registerModeControllerExtensionProvider(IModeControllerExtensionProvider provider, Hashtable<String, String[]> props) {
				context.registerService(IModeControllerExtensionProvider.class.getName(), provider, props);
			}

			@Override
			public void registerCoreExtension(DocearExtension extension) {
				//DocearCore.this.setExtension();
			}
		};
	}

}
