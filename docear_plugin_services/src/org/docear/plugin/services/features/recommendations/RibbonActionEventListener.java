package org.docear.plugin.services.features.recommendations;

import org.freeplane.core.ui.ribbon.event.AboutToPerformEvent;
import org.freeplane.core.ui.ribbon.event.IActionEventListener;

public class RibbonActionEventListener implements IActionEventListener {

	public void aboutToPerform(AboutToPerformEvent event) {
		RecommendationsController.closeRecommendationView();		
	}
	
}
