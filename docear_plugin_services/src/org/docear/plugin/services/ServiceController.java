package org.docear.plugin.services;

import java.net.URL;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.actions.DocearCheckForUpdatesAction;
import org.docear.plugin.services.actions.DocearClearUserDataAction;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.features.UpdateCheck;
import org.docear.plugin.services.features.elements.Application;
import org.docear.plugin.services.listeners.DocearEventListener;
import org.docear.plugin.services.listeners.MapLifeCycleListener;
import org.docear.plugin.services.listeners.ServiceWindowListener;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.recommendations.RecommendationsController;
import org.docear.plugin.services.recommendations.actions.ShowRecommendationsAction;
import org.docear.plugin.services.recommendations.workspace.ShowRecommendationsNode;
import org.docear.plugin.services.upload.UploadController;
import org.docear.plugin.services.workspace.DocearWorkspaceModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class ServiceController extends UploadController {
	public static final String DOCEAR_INFORMATION_RETRIEVAL = "docear_information_retrieval";

	public static final String DOCEAR_SAVE_BACKUP = "docear_save_backup";
	public static final long RECOMMENDATIONS_AUTOSHOW_INTERVAL = 1000*60*60*24*7; // every 7 days in milliseconds


	private static ServiceController serviceController;

	private final IMapLifeCycleListener mapLifeCycleListener = new MapLifeCycleListener();
	public static final int ALLOW_RECOMMENDATIONS = 8;
	public static final int ALLOW_USAGE_MINING = 4;
	public static final int ALLOW_INFORMATION_RETRIEVAL = 2;
	public static final int ALLOW_RESEARCH = 1;
	

	private Application application;
	private Collection<RecommendationEntry> autoRecommendations;
	private Boolean AUTO_RECOMMENDATIONS_LOCK = false;
	

	

	private ServiceController(ModeController modeController) {
		WorkspaceController.getModeExtension(modeController).setModel(new DocearWorkspaceModel());
		initListeners(modeController);

		new ServiceConfiguration(modeController);
		new ServicePreferences(modeController);

		addPluginDefaults(modeController);
		addMenuEntries(modeController);
		Controller.getCurrentController().addAction(new DocearClearUserDataAction());
		Controller.getCurrentController().addAction(new DocearAllowUploadChooserAction());
		Controller.getCurrentController().addAction(new DocearCheckForUpdatesAction());
		Controller.getCurrentController().addAction(new ShowRecommendationsAction());
	}

	protected static void initialize(ModeController modeController) {
		if (serviceController == null) {
			serviceController = new ServiceController(modeController);
			if (DocearController.getController().isLicenseDialogNecessary())
			{
				DocearController.getController().dispatchDocearEvent(new DocearEvent(DocearController.getController(), null, DocearEventType.SHOW_LICENSES));
			}			
			serviceController.startRecommendationsMode();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					serviceController.getUploader().start();
					serviceController.getPacker().start();
				}
			});
			new Thread() {
				public void run() {
					new UpdateCheck();
				}
			}.start();
		}
	}

	private void initListeners(ModeController modeController) {
		DocearController.getController().addDocearEventListener(new DocearEventListener());
		modeController.getMapController().addMapLifeCycleListener(mapLifeCycleListener);
	}

	public static ServiceController getController() {
		return serviceController;
	}

	private void addPluginDefaults(ModeController modeController) {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null) throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
		
		AWorkspaceTreeNode wsRoot = WorkspaceController.getModeExtension(modeController).getModel().getRoot();
		wsRoot.insertChildNode(new ShowRecommendationsNode(), 0);	
	}

	public boolean isBackupEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(DOCEAR_SAVE_BACKUP);
	}

	public void setBackupEnabled(boolean b) {
		ResourceController.getResourceController().setProperty(DOCEAR_SAVE_BACKUP, b);
	}

	public int getInformationRetrievalCode() {
		return Integer.parseInt(ResourceController.getResourceController().getProperty(DOCEAR_INFORMATION_RETRIEVAL, "0"));
	}

	public boolean isResearchAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RESEARCH) > 0;
	}

	public boolean isInformationRetrievalSelected() {
		return (getInformationRetrievalCode() & ALLOW_INFORMATION_RETRIEVAL) > 0;
	}

	public boolean isUsageMiningAllowed() {
		return (getInformationRetrievalCode() & ALLOW_USAGE_MINING) > 0;
	}

	public boolean isRecommendationsAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RECOMMENDATIONS) > 0;
	}

	public void setInformationRetrievalCode(int code) {
		ResourceController.getResourceController().setProperty(DOCEAR_INFORMATION_RETRIEVAL, "" + code);
	}

	

	public boolean isBackupAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		return isBackupEnabled() && commCtrl.allowTransmission() && !isEmpty(commCtrl.getRegisteredAccessToken()) && !isEmpty(commCtrl.getRegisteredUserName());
	}

	public boolean isInformationRetrievalAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		boolean needUser = getInformationRetrievalCode() > 0 && commCtrl.allowTransmission();

		return needUser && (!isEmpty(commCtrl.getAccessToken()) || !isEmpty(commCtrl.getUserName()));
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	private void addMenuEntries(ModeController modeController) {

		// modeController.addMenuContributor(new IMenuContributor() {
		// public void updateMenus(ModeController modeController, MenuBuilder
		// builder) { // /EditDetailsInDialogAction
		// builder.addMenuItem("/menu_bar/extras",new
		// JMenu(TextUtils.getText("docear.recommendations.menu")),
		// "/menu_bar/recommendations", MenuBuilder.BEFORE);
		// builder.addAction("/menu_bar/recommendations", new
		// ShowRecommendationsAction(), MenuBuilder.AS_CHILD);
		// builder.addMenuItem("/node_popup",new
		// JMenu(TextUtils.getText("docear.recommendations.menu")),
		// "/node_popup/recommendations", MenuBuilder.PREPEND);
		// builder.addAction("/node_popup/recommendations", new
		// ShowRecommendationsAction(), MenuBuilder.AS_CHILD);
		// }
		// });
	}

	@Override
	public int getUploadInterval() {
		final ResourceController resourceCtrl = Controller.getCurrentController().getResourceController();
		int backupMinutes = resourceCtrl.getIntProperty("save_backup_automcatically", 0);
		if (backupMinutes <= 0) {
			backupMinutes = 30;
		}
		return backupMinutes;
	}
	
	private void startRecommendationsMode() {
		long lastShowTime = Controller.getCurrentController().getResourceController().getLongProperty("docear.recommendations.last_auto_show", 0);
		
		if(((System.currentTimeMillis()-lastShowTime) > RECOMMENDATIONS_AUTOSHOW_INTERVAL) 
				&& isRecommendationsAllowed()
				&& !isEmpty(CommunicationsController.getController().getUserName())) {
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

	
}
