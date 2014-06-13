package org.docear.plugin.services.features.documentsearch;

import java.util.NoSuchElementException;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentsearch.model.view.DocumentSearchView;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.recommendations.DocumentRetriever;
import org.docear.plugin.services.features.recommendations.DocumentView;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class DocumentSearchController extends DocumentRetriever {
	
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
	public DocumentView getView() throws NoSuchElementException {
		return DocumentSearchView.getView();
	}
}
