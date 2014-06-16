package org.freeplane.plugin.docear.core;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.plugin.docear.core.extension.DocearExtension;
import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;
import org.freeplane.plugin.docear.core.msg.DocearMessage;
import org.freeplane.plugin.docear.core.msg.DocearMessageLoop;


/**
 * @author genzmehr@docear.org
 *
 */
public class DocearCore {
	private static DocearCore core;

	private final DocearServicesManager servicesMgr;
	
	private final DocearMessageLoop messageLoop;
	
	private final Map<DocearExtensionIdentifier, DocearExtension> extensions;

	private DocearCore() {
		servicesMgr = new DocearServicesManager();
		extensions = new HashMap<DocearExtensionIdentifier, DocearExtension>();
		messageLoop = new DocearMessageLoop();
	}

	public static DocearCore getInstance() {
		if (core == null) {
			core = new DocearCore();
		}
		return core;
	}
	
	public void startup() {
		servicesMgr.initializeServices(new DocearServiceContext() {
			
			public void registerCoreExtension(DocearExtension extension) {
				DocearCore.getInstance().setExtension(extension);
			}
		});
	}
	
	public void shutdown() {
		servicesMgr.shutdown();		
	}
	
	public DocearExtensionIdentifier[] getExtensionIDs() {
		DocearExtensionIdentifier[] ids = new DocearExtensionIdentifier[0]; 
		synchronized (extensions) {
			ids = extensions.keySet().toArray(ids);
		}
		return ids;
	}
	
	public void setExtension(final DocearExtension extension) {
		synchronized (extensions) {
			extensions.put(extension.getID(), extension);
		}
		
	}
	
	public DocearExtension getExtension(DocearExtensionIdentifier key){
		return getExtension(key, DocearExtension.class);		
	}
	
	public <T extends DocearExtension> T getExtension(DocearExtensionIdentifier key, Class<T> cast){
		synchronized (extensions) {
			return cast.cast(extensions.get(key));
		}
	}
	
	public void dispatchMessage(DocearMessage msg) {
		this.messageLoop.dispatchMessage(msg);
	}
}
