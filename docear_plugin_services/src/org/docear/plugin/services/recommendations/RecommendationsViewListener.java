package org.docear.plugin.services.recommendations;

import java.util.EventListener;

public interface RecommendationsViewListener extends EventListener {
	public void viewChanged(RecommendationsViewChangedEvent event);
}
