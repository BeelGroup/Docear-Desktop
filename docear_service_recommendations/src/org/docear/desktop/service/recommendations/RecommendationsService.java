package org.docear.desktop.service.recommendations;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;

public class RecommendationsService implements DocearService {

	public RecommendationsService() {
	}
	
	public void start(DocearServiceContext context) {
		System.out.println("Recommendations Service starting ...");
		
	}

	public void stop(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
