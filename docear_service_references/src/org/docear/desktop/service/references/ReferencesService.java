package org.docear.desktop.service.references;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;

public class ReferencesService implements DocearService {

	public ReferencesService() {
	}
	
	public void start(DocearServiceContext context) {
		System.out.println("References Service starting ...");
		
	}

	public void stop(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
