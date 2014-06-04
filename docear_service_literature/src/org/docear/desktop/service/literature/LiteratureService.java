package org.docear.desktop.service.literature;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;

public class LiteratureService implements DocearService {

	public LiteratureService() {
	}
	
	public void start(DocearServiceContext context) {
		System.out.println("Literature Service starting ...");
		
	}

	public void stop(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
