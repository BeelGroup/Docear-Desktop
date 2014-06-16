package org.docear.desktop.service.servicename;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;
import org.freeplane.plugin.docear.util.DocearLogger;

public class ServiceNameService implements DocearService {
	
	
	/**
	 * DO NOT REMOVE THIS CONSTRUCTOR!
	 */
	public ServiceNameService() {
	}
	
	public void install(DocearServiceContext context) {
		DocearLogger.info("installing ServiceName service...");
		
	}

	public void uninstall(DocearServiceContext context) {
		
	}

}
