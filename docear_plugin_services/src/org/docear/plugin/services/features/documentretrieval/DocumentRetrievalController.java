package org.docear.plugin.services.features.documentretrieval;

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

import org.apache.commons.lang.NullArgumentException;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.view.DocumentSearchView;
import org.docear.plugin.services.features.documentretrieval.documentsearch.workspace.ShowDocumentSearchNode;
import org.docear.plugin.services.features.documentretrieval.model.DocumentEntry;
import org.docear.plugin.services.features.documentretrieval.model.DocumentModelNode;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.recommendations.view.RecommendationsView;
import org.docear.plugin.services.features.documentretrieval.recommendations.workspace.ShowRecommendationsNode;
import org.docear.plugin.services.features.documentretrieval.view.DocumentView;
import org.docear.plugin.services.features.documentretrieval.workspace.DownloadFolderNode;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;
import org.docear.plugin.services.features.io.UnauthorizedException;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.ribbon.event.IActionEventListener;
import org.freeplane.core.user.IUserAccountChangeListener;
import org.freeplane.core.user.UserAccountChangeEvent;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
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

public abstract class DocumentRetrievalController extends ADocearServiceFeature {
	private static Object mutex = new Object();
	private static boolean isRequesting;
	
	public static final long RECOMMENDATIONS_AUTOSHOW_INTERVAL = 1000*60*60*24*7; // every 7 days in milliseconds
	
	private static IActionEventListener aeListener = new RibbonActionEventListener(); 

	private File downloadsFolder;
	private FolderLinkNode downloadsNode;
	
	private Collection<DocumentEntry> autoRecommendations;
	protected Boolean AUTO_RECOMMENDATIONS_LOCK = false;
	
	protected static DocumentView view;
	
	protected abstract DocearServiceResponse getRequestResponse(String userName, boolean userRequest);
	public abstract void refreshDocuments();
	
	public static void initializeDocumentSearcher() {
		if(view == null) {
			view = new DocumentSearchView();		
		}
	}
	
	public static void initializeRecommendations() {
		if(view == null) {
			view = new RecommendationsView();
		}
	}
	
	public static void destroyView() {
		view = null;
	}
	
	public static DocumentView getView() {
		return view;
	}
	
	public Collection<DocumentEntry> getNewDocuments(boolean userRequest) throws UnknownHostException, UnauthorizedException, UnexpectedException, AlreadyInUseException {		
		synchronized (mutex ) {
			if(isRequesting) {
				throw new AlreadyInUseException();
			}
			isRequesting = true;
			LogUtils.info("requesting recommendations");
		}
		try {
			DocearUser user = ServiceController.getCurrentUser();
			String userName = user.getUsername();
			if (!CoreUtils.isEmpty(userName)) {
				DocearServiceResponse response = getRequestResponse(userName, userRequest);
				if (response == null) {
					return null;
				}
				if (response.getStatus() == Status.OK) {
					try {
						DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
//						LogUtils.info(response.getContentAsString());
						IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
						IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
						parser.setBuilder(xmlBuilder);
						parser.setReader(reader);
						parser.parse();
						DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
						
						Collection<DocearXmlElement> documents = result.findAll("document");
						List<DocumentEntry> recommendations = new ArrayList<DocumentEntry>();
						
						java.util.Iterator<DocearXmlElement> iterator = documents.iterator();
						int id = 0;
						if (iterator.hasNext()) {
							DocearXmlElement document = iterator.next();
							//"recommendations" element
							DocearXmlElement recommendationsElement = document.getParent().getParent();
							String evaluationLabel = recommendationsElement.getAttributeValue("evaluationLabel");
							evaluationLabel = ((evaluationLabel == null || evaluationLabel.trim().isEmpty()) ? "How good are these recommendations?" : evaluationLabel);
							String strId = recommendationsElement.getAttributeValue("id");
							id = ((strId == null || strId.trim().isEmpty()) ? 0 : Integer.parseInt(strId));
							recommendations.add(new DocumentEntry(id, null, recommendationsElement.getAttributeValue("descriptor"), evaluationLabel, null, null, false));
						}
						
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
									recommendations.add(new DocumentEntry(id, prefix, title, "", url, click, highlighted));
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
				else if (response.getStatus() == Status.UNAUTHORIZED) {
					throw new UnauthorizedException("unauthorized");
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
				
	public void refreshDocuments(Collection<DocumentEntry> recommendations) {
		DocumentsModel model = null;
		if(recommendations == null) {
			try {
				model = requestDocuments();
			} catch (AlreadyInUseException e) {
				return;
			}			
		}
		else {
			model = new DocumentsModel(recommendations);
		}		
		updateDocumentView(model);
	}
	
	public void updateDocumentView(DocumentsModel model) {
		if(model == null) {
			model = getExceptionModel(new NullArgumentException("model is null"));
		}
		
		try {
			view.setModel(model);
		} catch (NoSuchElementException e) {
			LogUtils.severe(e);
		}
	}
	
	protected DocumentsModel requestDocuments() throws AlreadyInUseException {
		DocumentsModel model = null;		
		if (ServiceController.getCurrentUser().isRecommendationsEnabled()) {
			final ProgressMonitor monitor = new ProgressMonitor(UITools.getFrame(), TextUtils.getText("recommendations.request.wait.text"), null, 0, 100);
			monitor.setMillisToDecideToPopup(0);
			monitor.setMillisToPopup(0);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				Future<DocumentsModel> task = executor.submit(new Callable<DocumentsModel>() {
	
					public DocumentsModel call() throws Exception {
						DocumentsModel model = null;	
											
						monitor.setProgress(1);
						((JProgressBar)monitor.getAccessibleContext().getAccessibleChild(1)).setIndeterminate(true);
						long l = System.currentTimeMillis();
						Collection<DocumentEntry> recommendations = getNewDocuments(true);
						LogUtils.info("recommendation request time: "+(System.currentTimeMillis()-l));
						
						model = new DocumentsModel(recommendations);
						return model;
					}
					
				});
				model = task.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
				//handshake -> send receive confirmation
				sendReceiveConfirmation(model);
				
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
			model = new DocumentsModel(null);
		}		
		return model;
	}

	public void sendReceiveConfirmation(final DocumentsModel model) {
		DocearController.getController().getEventQueue().invoke(new Runnable() {
			public void run() {				
				DocearServiceResponse resp = ServiceController.getConnectionController().put("user/"+ServiceController.getCurrentUser().getName()+"/recommendations/"+ String.valueOf(model.getSetId())+"/", null);
				if(resp.getStatus() != Status.OK) {
					DocearLogger.info(resp.getContentAsString());
				}
			}
		});
	}
	
	protected DocumentsModel getExceptionModel(Exception e) {
		DocumentsModel model = new DocumentsModel();
		String message = "";
		if (e instanceof UnknownHostException) {
			message = TextUtils.getText("recommendations.error.no_connection");
		}
		else {
			message = TextUtils.getText("recommendations.error.unknown");
		}
		model.setRoot(DocumentModelNode.createNoRecommendationsNode(message));
		return model;
	}
	
	public void closeRecommendationView() {
		view.close();
	}
	
	public void setAutoRecommendations(Collection<DocumentEntry> autoRecommendations) {
		this.autoRecommendations = autoRecommendations;
	}

	public Collection<DocumentEntry> getAutoRecommendations() {		
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
		wsRoot.insertChildNode(new ShowDocumentSearchNode(), 0);
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
		modeController.getUserInputListenerFactory().getRibbonBuilder().getRibbonActionEventHandler().addListener(aeListener);
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
