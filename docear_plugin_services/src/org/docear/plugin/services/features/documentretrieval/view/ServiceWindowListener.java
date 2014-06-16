package org.docear.plugin.services.features.documentretrieval.view;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.SwingUtilities;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.recommendations.RecommendationsController;
import org.freeplane.core.ui.components.UITools;

public class ServiceWindowListener implements WindowListener {

	public void windowOpened(WindowEvent e) {
		final WindowListener wl = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(ServiceController.getFeature(RecommendationsController.class).isAutoRecommending()) {
					SwingUtilities.invokeLater(this);
					return;
				}
				getRecommendations(wl);
			}
		});

	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {

	}

	private void getRecommendations(final WindowListener wl) {		
		if (ServiceController.getFeature(RecommendationsController.class).getAutoRecommendations() != null) {
			UITools.getFrame().removeWindowListener(wl);
			RecommendationsController.getController().refreshDocuments(ServiceController.getFeature(RecommendationsController.class).getAutoRecommendations());
			ServiceController.getFeature(RecommendationsController.class).setAutoRecommendations(null);
		}
	}

}