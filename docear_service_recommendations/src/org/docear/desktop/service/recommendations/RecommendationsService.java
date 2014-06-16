package org.docear.desktop.service.recommendations;

import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;
import org.freeplane.plugin.docear.util.DocearLogger;

public class RecommendationsService implements DocearService {

	public RecommendationsService() {
	}
	
	public void install(DocearServiceContext context) {
		DocearLogger.info("Recommendations Service starting ...");
		
	}

	public void uninstall(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
