package org.docear.desktop.service.servicename;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;

public class ServiceNameService implements DocearService {
	
	
	/**
	 * DO NOT REMOVE THIS CONSTRUCTOR!
	 */
	public ServiceNameService() {
	}
	
	public void start(DocearServiceContext context) {
		System.out.println("ServiceName Service starting ...");
		
	}

	public void stop(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
