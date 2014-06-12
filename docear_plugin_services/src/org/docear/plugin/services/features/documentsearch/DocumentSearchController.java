package org.docear.plugin.services.features.documentsearch;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.apache.commons.lang.NullArgumentException;
import org.docear.plugin.services.features.documentsearch.model.view.DocumentSearchView;
import org.docear.plugin.services.features.recommendations.AlreadyInUseException;
import org.docear.plugin.services.features.recommendations.RecommendationsController;
import org.docear.plugin.services.features.recommendations.model.RecommendationEntry;
import org.docear.plugin.services.features.recommendations.model.RecommendationsModel;
import org.freeplane.core.util.LogUtils;

public class DocumentSearchController extends RecommendationsController {
	public static void refreshRecommendations() {
		DocumentSearchController.refreshRecommendations(null);
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
			DocumentSearchView view = DocumentSearchView.getView();
			view.setModel(model);
		} catch (NoSuchElementException e) {
			LogUtils.severe(e);
		}
	}
}
