package org.docear.plugin.services.features.recommendations.view;

import java.util.EventListener;


public interface RecommendationsViewListener extends EventListener {
	public void viewChanged(RecommendationsViewChangedEvent event);
}
