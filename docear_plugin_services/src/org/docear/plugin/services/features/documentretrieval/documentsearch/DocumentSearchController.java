package org.docear.plugin.services.features.documentretrieval.documentsearch;

import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.actions.ShowDocumentSearchAction;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;
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
	private SearchModel searchModel = null;
	
	public final static DocumentSearchController getController() {
		if (controller == null) {
			controller = new DocumentSearchController();
		}
		
		if (controller instanceof DocumentSearchController) {
			return (DocumentSearchController) controller;
		}
		else {
			controller.closeDocumentView();
			new ShowDocumentSearchAction().actionPerformed(null);
			return (DocumentSearchController) controller;
		}
	}
	
	public void setQuery(String query) {
		this.query = query;
	}	
	
	@Override
	protected DocearServiceResponse getRequestResponse(boolean userRequest) {
		MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		
		if (searchModel != null) {
			params.add("searchModelId", String.valueOf(searchModel.getId()));
		}
		if (getDocumentsSetId() != null) {
			params.add("searchDocumentsSetId", String.valueOf(getDocumentsSetId()));
		}
		params.add("page", String.valueOf(page));
		params.add("userName", ServiceController.getCurrentUser().getName());
		params.add("number", "10");
		
		if (this.query.trim().length() > 0) {
			return ServiceController.getConnectionController().get("/documents/" + createLuceneSearchString(query) + "/", params);
		}
		else {
			return null;
		}
	}

	private String createLuceneSearchString(String query) {
		query = query.toLowerCase().trim();
		if (query.contains("and") || query.contains("or")) {
			return query;
		}
		
		StringBuilder sb = new StringBuilder();
		
		String[] terms = query.split(" ");
		sb.append(terms[0]);
		for (int i=1; i<terms.length; i++) {
			sb.append(" AND ");
			sb.append(terms[i]);
		}
		
		String queryString = sb.toString();
		LogUtils.info("using query string: "+queryString);
		
		return queryString;
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
    				if (response != null && response.getStatus() == Status.OK) {    					
        				DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
        				IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
        				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
        				parser.setBuilder(xmlBuilder);
        				parser.setReader(reader);
        				parser.parse();
        				DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
        				
        				DocearXmlElement element = result.find("searchmodel");
        				searchModel = new SearchModel(Long.valueOf(element.getAttributeValue("id")), element.getContent().trim());
        				
        				if (searchModel != null) {
        					sendModelReceivedConfirmation();
        				}
    					return searchModel;
    				}
				}
				catch(NullPointerException ignore) {}
				return null;
			}
			
			private void sendModelReceivedConfirmation() {				
				try {
    				MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
    				params.add("searchModelId", String.valueOf(searchModel.getId()));    				
    				
    				DocearServiceResponse resp = ServiceController.getConnectionController().put("/user/"+ServiceController.getCurrentUser().getUsername()+"/searchmodel/", params);
    				
    				if(resp.getStatus() != Status.OK) {
    					DocearLogger.info(resp.getContentAsString());
    				}
				}
				catch (Exception e) {
					System.out.println("exception in DocumentSearchController.getSearchModel().new Callable() {...}.sendModelReceivedConfirmation(): "
							+ e.getMessage());
				}
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

	@Override
	public void sendReceiveConfirmation(final DocumentsModel model) {
		DocearController.getController().getEventQueue().invoke(new Runnable() {
			public void run() {
				try {
    				MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
    				
    				params.add("page", String.valueOf(page));
    				params.add("userName", ServiceController.getCurrentUser().getName());
    				params.add("searchDocumentsSetId", String.valueOf(getDocumentsSetId()));
    				
    				DocearServiceResponse resp = ServiceController.getConnectionController().put("/documents/" + query + "/", params);
    				
    				if(resp.getStatus() != Status.OK) {
    					DocearLogger.info(resp.getContentAsString());
    				}
				}
				catch (Exception e) {
					LogUtils.warn("exception in DocumentSearchController.sendReceiveConfirmation(...).new Runnable() {...}.run(): " + e.getMessage());
				}
			}
		});
	}
}
