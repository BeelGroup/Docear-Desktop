package org.docear.plugin.services.features.documentretrieval.view;

import java.util.EventListener;


public interface DocumentViewListener extends EventListener {
	public void viewChanged(DocumentViewChangedEvent event);
}
