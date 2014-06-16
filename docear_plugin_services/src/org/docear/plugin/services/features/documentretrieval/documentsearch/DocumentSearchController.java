package org.docear.plugin.services.features.documentretrieval.documentsearch;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.io.DocearServiceResponse;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class DocumentSearchController extends DocumentRetrievalController {
	
	private final static DocumentSearchController controller = new DocumentSearchController();
	
	public final static DocumentSearchController getController() {
		return controller;
	}
	
	@Override
	protected DocearServiceResponse getRequestResponse(String userName, boolean userRequest) {
		String query = "docear";
		
		MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		
		params.add("userName", userName);
		params.add("number", "10");
		
		return ServiceController.getConnectionController().get("/documents/" + query + "/", params);
	}

	@Override
	public void refreshRecommendations() {
		initializeDocumentSearcher();
		refreshDocuments(null);
	}	
}
