package org.docear.plugin.services.recommendations;

import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.NullArgumentException;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.communications.features.DocearServiceResponse;
import org.docear.plugin.services.communications.features.DocearServiceResponse.Status;
import org.docear.plugin.services.recommendations.model.RecommendationsModel;
import org.docear.plugin.services.recommendations.model.RecommendationsModelNode;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public abstract class RecommendationsController {

	private static Object mutex = new Object();
	private static boolean isRequesting;

	public static void refreshRecommendations() {
		refreshRecommendations(null);
	}
	
	public static void refreshRecommendations(Collection<RecommendationEntry> recommendations) {
		RecommendationsModel model;
		if(recommendations == null) {
			try {
				model = requestRecommendations();
			} catch (AlreadyInUseException e) {
				return;
			}			
		}
		else {
			model = new RecommendationsModel(recommendations);
		}		
		updateRecommendationsView(model);
	}
	
	public static void updateRecommendationsView(RecommendationsModel model) {
		if(model == null) {
			model = getExceptionModel(new NullArgumentException("model is null"));
		}
		
		try {
			RecommendationsView view = RecommendationsView.getView();
			view.setModel(model);
		} catch (NoSuchElementException e) {
			LogUtils.severe(e);
		}
//		IMapViewManager mgr = Controller.getCurrentController().getViewController().getMapViewManager();		
		
		
	}
	
	private static RecommendationsModel requestRecommendations() throws AlreadyInUseException {
		RecommendationsModel model = null;		
		if (ServiceController.getController().isRecommendationsAllowed()) {
			final ProgressMonitor monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.request.wait.text"), null, 0, 100);
			monitor.setMillisToDecideToPopup(0);
			monitor.setMillisToPopup(0);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				Future<RecommendationsModel> task = executor.submit(new Callable<RecommendationsModel>() {
	
					public RecommendationsModel call() throws Exception {
						RecommendationsModel model = null;	
											
						monitor.setProgress(1);
						((JProgressBar)monitor.getAccessibleContext().getAccessibleChild(1)).setIndeterminate(true);
						long l = System.currentTimeMillis();
						Collection<RecommendationEntry> recommendations = getNewRecommendations(true);
						System.out.println("exec time: "+(System.currentTimeMillis()-l));
						model = new RecommendationsModel(recommendations);
						return model;
					}
					
				});
				model = task.get(CommunicationsController.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
			}
			catch (Exception e) {
				executor.shutdownNow();
				if(e instanceof AlreadyInUseException) {
					throw (AlreadyInUseException)e;
				}
				LogUtils.warn(e);
				model = getExceptionModel(e);
				
			}
			finally {
				monitor.close();
			}
		} 
		else {
			model = new RecommendationsModel(null);
		}		
		return model;
	}
	
	private static RecommendationsModel getExceptionModel(Exception e) {
		RecommendationsModel model = new RecommendationsModel();
		String message = "";
		if (e instanceof UnknownHostException) {
			message = TextUtils.getText("recommendations.error.no_connection");
		}
		else {
			message = TextUtils.getText("recommendations.error.unknown");
		}
		model.setRoot(RecommendationsModelNode.createNoRecommendationsNode(message));
		return model;
	}
	
	public static Collection<RecommendationEntry> getNewRecommendations(boolean userRequest) throws UnknownHostException, UnexpectedException, AlreadyInUseException {		
		synchronized (mutex ) {
			if(isRequesting) {
				throw new AlreadyInUseException();
			}
			isRequesting = true;
			LogUtils.info("requesting recommendations");
		}
		try {
			String name = CommunicationsController.getController().getUserName();
			if (!CoreUtils.isEmpty(name)) {
				MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
				if(!userRequest) {
					params.add("auto", "true");
				}
				DocearServiceResponse response = CommunicationsController.getController().get("/user/" + name + "/recommendations/documents", params);
	
				if (response.getStatus() == Status.OK) {
					try {
						DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
						IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
						IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
						parser.setBuilder(xmlBuilder);
						parser.setReader(reader);
						parser.parse();
						DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
						Collection<DocearXmlElement> documents = result.findAll("document");
						List<RecommendationEntry> recommendations = new ArrayList<RecommendationEntry>();
						for (DocearXmlElement document : documents) {
							try {
								// exclude reference documents -> may not have a sourceid and the parent does not have a fulltext attribute
								if(!document.hasParent("document")) {
									String title = document.find("title").getContent();
									String url = document.find("sourceid").getContent();
									String click = document.getParent().getAttributeValue("fulltext");
									recommendations.add(new RecommendationEntry(title, url, click));
								}
							}
							catch (Exception e) {
								LogUtils.warn("error while parsing recommendations: " + e.getMessage());
							}
						}
	
						return recommendations;
					}
					catch (Exception e) {
						LogUtils.severe(e);
					}
				}
				else if (response.getStatus() == Status.NO_CONTENT) {
					return null;
				}
				else if (response.getStatus() == Status.UNKNOWN_HOST) {
					throw new UnknownHostException("no connection");
				}			
				else {
					throw new UnexpectedException("unkown");
				}
			}
			else {
				throw new IllegalStateException("no username set");
			}
			return Collections.emptyList();
		}
		finally {
			synchronized (mutex ) {
				isRequesting = false;
			}
			LogUtils.info("finished recommendation request");
		}
	}
	//DOCEAR - todo: close does not work
	public static void closeRecommendationView() {
		RecommendationsView.close();
	}

	
}
