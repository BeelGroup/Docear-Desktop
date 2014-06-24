package org.docear.plugin.services.features.documentretrieval.documentsearch;

import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class DocumentSearchController extends DocumentRetrievalController {
	private String query = "";
	private int page = 1;
	private Long searchModelId = null;
	private SearchModel searchModel = null;
	
	public final static DocumentSearchController getController() {
		if (controller == null) {
			controller = new DocumentSearchController();
		}
		
		return (DocumentSearchController) controller;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void setsearchModelId(Long searchModelId) {
		this.searchModelId = searchModelId;
	}
	
	@Override
	protected DocearServiceResponse getRequestResponse(String userName, boolean userRequest) {
		MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		
		if (searchModelId != null) {
			params.add("searchModelId", String.valueOf(this.searchModelId));
		}
		params.add("page", String.valueOf(page));
		params.add("userName", userName);
		params.add("number", "10");
		
		if (this.query.trim().length() > 0) {
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
	
	public String getQuery() {
		return query;
	}
	
	public SearchModel getSearchModel() {
		if (searchModel != null) {
			return searchModel;
		}
		
		final DocearUser user = ServiceController.getCurrentUser();		
		if (user == null) {
			return null;
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<SearchModel> task = executor.submit(new Callable<SearchModel>() {
			public SearchModel call() throws Exception {
				try {
    				DocearServiceResponse response = ServiceController.getConnectionController().get("/user/"+user.getUsername()+"/searchmodel/");
    				
    				DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
    				IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
    				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
    				parser.setBuilder(xmlBuilder);
    				parser.setReader(reader);
    				parser.parse();
    				DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
    				
    				DocearXmlElement element = result.find("searchmodel");
    				searchModel = new SearchModel(Long.valueOf(element.getAttributeValue("id")), element.getContent().trim());
					return searchModel;
				}
				catch(NullPointerException ignore) {}
				return null;
			}
		});
		try {
			return task.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);		
		}
		catch(Exception e) {
			LogUtils.warn(e);
		}
		
		return null;
	}
	
	public void search(String query) {
		this.query = query;

		if (query == null) {
			return;
		}
			
		query = query.trim().toLowerCase();
		if (query.length() == 0) {
			return;
		}
		
		DocumentSearchController.getController().setsearchModelId(searchModelId);
		DocumentSearchController.getController().setQuery(query);
		DocumentSearchController.getController().refreshDocuments();
	}


	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public void shutdown() {
		this.query = "";
		this.searchModel = null;
	}
}
