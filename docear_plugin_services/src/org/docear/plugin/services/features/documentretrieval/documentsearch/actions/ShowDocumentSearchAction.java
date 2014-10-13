package org.docear.plugin.services.features.documentretrieval.documentsearch.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.DocumentSearchController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
public class ShowDocumentSearchAction extends AFreeplaneAction {
	public final static String TYPE = "ShowDocumentSearchAction";

	private static final long serialVersionUID = 1L;

	public ShowDocumentSearchAction() {
		super(TYPE);
	}

	public void setEnabled() {
		if (ServiceController.getCurrentUser().isRecommendationsEnabled() && ServiceController.getCurrentUser().isValid()) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		DocumentSearchController.getController().refreshDocuments();
	}

}
