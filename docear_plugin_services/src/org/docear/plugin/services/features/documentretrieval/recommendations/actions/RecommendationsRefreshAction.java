package org.docear.plugin.services.features.documentretrieval.recommendations.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.recommendations.RecommendationsController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
public class RecommendationsRefreshAction extends AFreeplaneAction {
	public final static String TYPE = "RecommendationsRefreshAction";

	private static final long serialVersionUID = 1L;

	public RecommendationsRefreshAction() {
		super(TYPE, TextUtils.getText("recommendations.refresh.title"), new ImageIcon(RecommendationsRefreshAction.class.getResource("/icons/view-refresh-7_16x16.png")));
	}

	public void setEnabled() {
		if (ServiceController.getCurrentUser().isRecommendationsEnabled() && ServiceController.getCurrentUser().isValid()) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
	}
	@Override
	public void setSelected() {
		setEnabled();
	}

	public void actionPerformed(ActionEvent e) {		
		RecommendationsController.getController().refreshDocuments();
	}

}
