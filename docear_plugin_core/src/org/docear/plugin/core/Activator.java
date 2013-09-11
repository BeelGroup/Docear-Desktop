package org.docear.plugin.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docear.plugin.core.workspace.compatible.DocearConversionURLHandler;
import org.docear.plugin.core.workspace.controller.DocearConversionDescriptor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.IWorkspaceDependingControllerExtension;
import org.freeplane.plugin.workspace.WorkspaceDependingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class Activator extends WorkspaceDependingService {
	CoreConfiguration config;
	
	public final void startPlugin(BundleContext context, ModeController modeController) {
		Hashtable<String, String[]>properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { DocearConversionDescriptor.OLD_WORKSPACE_URL_HANDLE });
        context.registerService(URLStreamHandlerService.class.getName(), new DocearConversionURLHandler(), properties);
        
		getConfig().initMode(modeController);		
		startPluginServices(context, modeController);
		DocearController.getController().getEventQueue().start();
	}
	
	protected Collection<IWorkspaceDependingControllerExtension> getControllerExtensions() {
		List<IWorkspaceDependingControllerExtension> controllerExtensions = new ArrayList<IWorkspaceDependingControllerExtension>();
		controllerExtensions.add(new IWorkspaceDependingControllerExtension() {
			public void installExtension(BundleContext context, Controller controller) {
				getConfig().initController(controller);
				LogUtils.info("Docear Core controller extension initiated.");
				startControllerExtensions(context, controller);
			}
		});
		return controllerExtensions;
	}
	
	private CoreConfiguration getConfig() {
		if(config == null) {
			config = new CoreConfiguration();
		}
		return config;
	}
		
	@SuppressWarnings("rawtypes")
	protected final void startPluginServices(BundleContext context, ModeController modeController) {		
		try {
			final ServiceReference[] dependends = context.getServiceReferences(DocearService.class.getName(),
					"(dependsOn="+DocearService.DEPENDS_ON+")");
			if (dependends != null) {
				List<DocearService> services = sortOnDependencies(dependends, context);
				for(DocearService service : services) {
					if(isValid(service)) {
						service.startService(context, modeController);
					}
				}
				
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected final void startControllerExtensions(BundleContext context, Controller controller) {		
		try {
			final ServiceReference[] extensions = context.getServiceReferences(IDocearControllerExtension.class.getName(), "(dependsOn="+DocearService.DEPENDS_ON+")");
//			if (extensions != null) {
//				List<?> extensions = sortOnDependencies(extensions, context);
//				for(Object extension : extensions) {
//					((IControllerExtensionProvider) extension).installExtension(controller);					
//				}
//				
//			}
			if (extensions != null) {
				for (ServiceReference serviceReference : extensions) {
					final IDocearControllerExtension extension = (IDocearControllerExtension) context.getService(serviceReference);
					extension.installExtension(context, controller);
					context.ungetService(serviceReference);
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isValid(DocearService service) {
		if(isBlacklisted(service.getBundleInfo().getBundleName())) {
			return false;
		}
		return true;
	}
	
	private boolean isBlacklisted(String packageName) {
		if("org.docear.plugin.backup".equals(packageName)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<DocearService> sortOnDependencies(ServiceReference[] dependends, BundleContext context) {
		ArrayList<DocearService> list = new ArrayList<DocearService>();
		HashMap<String, Set<DocearService>> requiredFor = new HashMap<String, Set<DocearService>>();
		
		for(ServiceReference serviceReference : dependends) {
			final DocearService service = (DocearService) context.getService(serviceReference); 
			for(DocearBundleInfo info : service.getBundleInfo().getRequiredBundles()) {
				if(info.getBundleName().startsWith("org.docear") && !info.getBundleName().equals("org.docear.plugin.core") && !inList(info, list)) {
					Set<DocearService> services = requiredFor.get(info.getBundleName());
					if (services == null) {
						services = new HashSet<DocearService>();
						requiredFor.put(info.getBundleName(), services);
					}					
					services.add(service);
					
				}
			}			
			if(!hasDependencies(service, requiredFor.values())) {
				if(!list.contains(service)) {
					list.add(service);
				}
				continue;
			}
			
//			resolveDependencies(list, requiredFor);			
		}
		while( requiredFor.size() > 0) {
			resolveDependencies(list, requiredFor);
		}
		
		return list;
	}

	private boolean hasDependencies(DocearService service, Collection<Set<DocearService>> collection) {
		for(Set<DocearService> set : collection) {
			if(set.contains(service)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param list
	 * @param requiredFor
	 */
	private void resolveDependencies(List<DocearService> list, Map<String, Set<DocearService>> map) {
		ArrayList<DocearService> buffer = new ArrayList<DocearService>();
		for(DocearService plugin : list) {
			if(map.containsKey(plugin.getBundleInfo().getBundleName())) {
				Set<DocearService> services = map.get(plugin.getBundleInfo().getBundleName());
				if (services != null) {
					Iterator<DocearService> iter = services.iterator();
					map.remove(plugin.getBundleInfo().getBundleName());
    				while (iter.hasNext()) {
    					DocearService inDept = iter.next();
    					iter.remove();
    					if(!hasMoreDepencies(map, inDept)) {
        					buffer.add(inDept);
        				}    					
    				}
				}
			}
		}
		for(DocearService plugin : buffer) {
			if(!list.contains(plugin)) {
				list.add(plugin);
			}
		}
	}

	private boolean hasMoreDepencies(Map<String, Set<DocearService>> map, DocearService inDept) {
		for(Set<DocearService> services : map.values()) {
			if(services.contains(inDept)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param info
	 * @param list
	 * @return <code>true</code> if ..., else <code>false</code>
	 */
	private boolean inList(DocearBundleInfo info, List<DocearService> list) {
		for(DocearService plugin : list) {
			if(plugin.getBundleInfo().getBundleName().equals(info.getBundleName())) {
				return true;
			}
		}
		return false;
	}

	public void stop(BundleContext context) throws Exception {
	}

	@Override
	protected void setupDefaults(BundleContext context) {
		// TODO Auto-generated method stub
		
	}
}
