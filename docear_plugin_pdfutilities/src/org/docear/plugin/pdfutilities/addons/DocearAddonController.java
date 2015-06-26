package org.docear.plugin.pdfutilities.addons;

import java.io.File;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;

import org.freeplane.core.resources.ResourceController;

public class DocearAddonController {
	
	private static DocearAddonController controller;
	
	private PluginManager manager;
	
	public DocearAddonController(){
		String userDir = ResourceController.getResourceController().getFreeplaneUserDirectory();	
		File addonsDir = new File(userDir + File.separatorChar + "addons");	    
		manager = PluginManagerFactory.createPluginManager();		
		manager.addPluginsFrom(addonsDir.toURI(), new OptionReportAfter());			
	}
	
	public <P extends Plugin> P getAddon(final Class<P> requestedPlugin){
		return manager.getPlugin(requestedPlugin);
	}
	
	public boolean hasPlugin(final Class<? extends Plugin> requestedPlugin){
		return manager.getPlugin(requestedPlugin) != null;
	}
	
	public static DocearAddonController getController(){
		if(controller == null){
			controller = new DocearAddonController();
		}
		return controller;
	}
	
}
