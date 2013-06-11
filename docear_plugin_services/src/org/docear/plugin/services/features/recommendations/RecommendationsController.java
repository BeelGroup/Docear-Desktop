package org.docear.plugin.services.features.recommendations;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
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
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;
import org.docear.plugin.services.features.recommendations.model.RecommendationEntry;
import org.docear.plugin.services.features.recommendations.model.RecommendationsModel;
import org.docear.plugin.services.features.recommendations.model.RecommendationsModelNode;
import org.docear.plugin.services.features.recommendations.view.RecommendationsView;
import org.docear.plugin.services.features.recommendations.view.ServiceWindowListener;
import org.docear.plugin.services.features.recommendations.workspace.DownloadFolderNode;
import org.docear.plugin.services.features.recommendations.workspace.ShowRecommendationsNode;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.user.IUserAccountChangeListener;
import org.freeplane.core.user.UserAccountChangeEvent;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.mindmapmode.FileFolderDropHandler;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class RecommendationsController extends ADocearServiceFeature {

	private static Object mutex = new Object();
	private static boolean isRequesting;
	
	public static final long RECOMMENDATIONS_AUTOSHOW_INTERVAL = 1000*60*60*24*7; // every 7 days in milliseconds

	private File downloadsFolder;
	private FolderLinkNode downloadsNode;
	
	private Collection<RecommendationEntry> autoRecommendations;
	private Boolean AUTO_RECOMMENDATIONS_LOCK = false;

	public static void refreshRecommendations() {
		refreshRecommendations(null);
	}
	
	public static void refreshRecommendations(Collection<RecommendationEntry> recommendations) {
		RecommendationsModel model = null;
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
	}
	
	private static RecommendationsModel requestRecommendations() throws AlreadyInUseException {
		RecommendationsModel model = null;		
		if (ServiceController.getCurrentUser().isRecommendationsEnabled()) {
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
				model = task.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
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
			String name = ServiceController.getCurrentUser().getUsername();
			if (!CoreUtils.isEmpty(name)) {
				MultivaluedMap<String,String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
				if(!userRequest) {
					params.add("auto", "true");
				}
				DocearServiceResponse response = ServiceController.getConnectionController().get("/user/" + name + "/recommendations/documents", params);
	
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
									DocearXmlElement recommendationElement = document.getParent();
									String prefix = recommendationElement.getAttributeValue("prefix");
									String click = recommendationElement.getAttributeValue("fulltext");
									boolean highlighted = ("true".equals(recommendationElement.getAttributeValue("highlighted")) ? true:false);
									recommendations.add(new RecommendationEntry(prefix, title, url, click, highlighted));
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
	
	public static void closeRecommendationView() {
		RecommendationsView.close();
	}
	
	
	private void startRecommendationsRequest() {
		long lastShowTime = Controller.getCurrentController().getResourceController().getLongProperty("docear.recommendations.last_auto_show", 0);
		DocearUser user = ServiceController.getCurrentUser();
		if(((System.currentTimeMillis()-lastShowTime) > RECOMMENDATIONS_AUTOSHOW_INTERVAL)
				&& user.isValid()
				&& user.isRecommendationsEnabled()) {
			LogUtils.info("automatically requesting recommendations");
			UITools.getFrame().addWindowListener(new ServiceWindowListener());
						
			
			synchronized (AUTO_RECOMMENDATIONS_LOCK) {
				AUTO_RECOMMENDATIONS_LOCK = true;
			}
			new Thread() {
				public void run() {	
					try {
						Collection<RecommendationEntry> recommendations = RecommendationsController.getNewRecommendations(false);	
						if(recommendations.isEmpty()) {
							setAutoRecommendations(null);
						}
						else {
							setAutoRecommendations(recommendations);
						}						
						Controller.getCurrentController().getResourceController().setProperty("docear.recommendations.last_auto_show", Long.toString(System.currentTimeMillis()));
					
					} 
					catch (Exception e) {				
						DocearLogger.warn("org.docear.plugin.services.ServiceController.startRecommendationsMode(): " + e.getMessage());
						setAutoRecommendations(null);
					}
					synchronized (AUTO_RECOMMENDATIONS_LOCK) {
						AUTO_RECOMMENDATIONS_LOCK = false;
					}					
				}
			}.start();
		} 
		else {
			setAutoRecommendations(null);
		}
	}
	
	public void setAutoRecommendations(Collection<RecommendationEntry> autoRecommendations) {
		this.autoRecommendations = autoRecommendations;
	}

	public Collection<RecommendationEntry> getAutoRecommendations() {		
			while(isLocked()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}		
			return autoRecommendations;
	}

	private boolean isLocked() {
		synchronized (AUTO_RECOMMENDATIONS_LOCK ) {
			return AUTO_RECOMMENDATIONS_LOCK;
		}
	}

	public boolean isAutoRecommending() {
		synchronized (AUTO_RECOMMENDATIONS_LOCK ) {
			return AUTO_RECOMMENDATIONS_LOCK;
		}
	}
	
	public URI getDownloadsFolder() {
		return downloadsFolder.toURI();
	}
	
	public void refreshDownloadsFolder() {
		WorkspaceController.getCurrentModeExtension().getView().expandPath(downloadsNode.getTreePath());
		downloadsNode.refresh();
	}

	@Override
	protected void installDefaults(ModeController modeController) {
		AWorkspaceTreeNode wsRoot = WorkspaceController.getModeExtension(modeController).getModel().getRoot();
		wsRoot.insertChildNode(new ShowRecommendationsNode(), 0);
		downloadsNode = new DownloadFolderNode();
		updateDownloadNode();
		wsRoot.insertChildNode(downloadsNode, 1);
		UserAccountController.getController().addUserAccountChangeListener(new IUserAccountChangeListener() {
			
			public void activated(UserAccountChangeEvent event) {
				if(event.getUser() instanceof DocearUser) {
					try {
					updateDownloadNode();
					downloadsNode.refresh();
					}
					catch (Exception e) {
						LogUtils.warn("Could not switch download folder node: "+ e.getMessage());
					}
				}
			}
			
			public void aboutToDeactivate(UserAccountChangeEvent event) {}
		});
		WorkspaceController.getModeExtension(modeController).getView().getTransferHandler().registerNodeDropHandler(DownloadFolderNode.class, new FileFolderDropHandler());
		startRecommendationsRequest();
	}

	private void updateDownloadNode() {
		downloadsFolder = new File( URIUtils.getFile(ServiceController.getController().getUserSettingsHome()),"downloads");
		if(!downloadsFolder.exists()) {
			try {
				downloadsFolder.mkdirs();
			}
			catch (Exception e) {
				LogUtils.warn("Exception in org.docear.plugin.services.features.recommendations.RecommendationsController.updateDownloadNode():"+ e.getMessage());
			}
		}
		downloadsNode.setPath(downloadsFolder.toURI());
	}

	@Override
	public void shutdown() {
	}

	
}
