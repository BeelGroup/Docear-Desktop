package org.docear.plugin.services.features.documentretrieval;

import org.freeplane.core.ui.ribbon.event.AboutToPerformEvent;
import org.freeplane.core.ui.ribbon.event.IActionEventListener;

public class RibbonActionEventListener implements IActionEventListener {

	public void aboutToPerform(AboutToPerformEvent event) {
		if (DocumentRetrievalController.getController() != null) {
			DocumentRetrievalController.getController().closeDocumentView();
		}
	}
}
