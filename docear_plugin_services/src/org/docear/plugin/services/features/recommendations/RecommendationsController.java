package org.docear.plugin.services.features.recommendations;

import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.recommendations.model.RecommendationEntry;
import org.docear.plugin.services.features.recommendations.view.ServiceWindowListener;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class RecommendationsController extends DocumentRetriever {

	private final static RecommendationsController controller = new RecommendationsController();

	public final static RecommendationsController getController() {
		return controller;
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
						Collection<RecommendationEntry> recommendations = RecommendationsController.getController().getNewDocuments(false);
						if (recommendations.isEmpty()) {
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
	protected DocearServiceResponse getRequestResponse(String userName, boolean userRequest) {
		MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		if (!userRequest) {
			params.add("auto", "true");
		}
		return ServiceController.getConnectionController().get("/user/" + userName + "/recommendations/documents", params);
	}

	@Override
	public void refreshRecommendations() {
		initializeRecommendations();
		refreshRecommendations(null);
	}

}
