package org.docear.plugin.services.features.documentretrieval.recommendations;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.model.DocumentEntries;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.recommendations.actions.ShowRecommendationsAction;
import org.docear.plugin.services.features.documentretrieval.view.ServiceWindowListener;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class RecommendationsController extends DocumentRetrievalController {

	public final static RecommendationsController getController() {
		if (controller == null) {
			controller = new RecommendationsController();
		}
		
		if (controller instanceof RecommendationsController) {
			return (RecommendationsController) controller;
		}
		else {
			controller.closeDocumentView();
			new ShowRecommendationsAction().actionPerformed(null);
			return (RecommendationsController) controller;
		}
	}

	public void startRecommendationsRequest() {
		long lastShowTime = Controller.getCurrentController().getResourceController().getLongProperty("docear.recommendations.last_auto_show", 0);
		DocearUser user = ServiceController.getCurrentUser();
		if (((System.currentTimeMillis() - lastShowTime) > RECOMMENDATIONS_AUTOSHOW_INTERVAL) && user.isValid() && user.isRecommendationsEnabled()) {
			LogUtils.info("automatically requesting recommendations");
			UITools.getFrame().addWindowListener(new ServiceWindowListener());

			synchronized (AUTO_RECOMMENDATIONS_LOCK) {
				AUTO_RECOMMENDATIONS_LOCK = true;
			}
			new Thread() {
				public void run() {
					try {
						DocumentEntries recommendations = RecommendationsController.getController().getNewDocuments(false);
						if (recommendations.getDocumentEntries().isEmpty()) {
							setAutoRecommendations(null);
						}
						else {
							setAutoRecommendations(recommendations);
						}
						Controller.getCurrentController().getResourceController()
								.setProperty("docear.recommendations.last_auto_show", Long.toString(System.currentTimeMillis()));

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

	@Override
	protected DocearServiceResponse getRequestResponse(boolean userRequest) {
		MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		if (!userRequest) {
			params.add("auto", "true");
		}
		return ServiceController.getConnectionController().get("/user/" + ServiceController.getCurrentUser().getName() + "/recommendations/documents", params);
	}

	@Override
	public void refreshDocuments() {
		initializeRecommendations();
		refreshDocuments(null);
	}

	@Override
	public void shutdown() {
	}

	@Override
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

}
