package org.docear.plugin.services.features.documentretrieval.documentsearch;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.io.DocearServiceResponse;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class DocumentSearchController extends DocumentRetrievalController {
	
	private final static DocumentSearchController controller = new DocumentSearchController();
	private String query = null;
	
	public final static DocumentSearchController getController() {
		return controller;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	protected DocearServiceResponse getRequestResponse(String userName, boolean userRequest) {
		MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		
		params.add("userName", userName);
		params.add("number", "10");
		
		if (this.query != null) {
			return ServiceController.getConnectionController().get("/documents/" + query + "/", params);
		}
		else {
			return null;
		}
	}

	@Override
	public void refreshDocuments() {
		initializeDocumentSearcher();
		refreshDocuments(null);
	}	
}
