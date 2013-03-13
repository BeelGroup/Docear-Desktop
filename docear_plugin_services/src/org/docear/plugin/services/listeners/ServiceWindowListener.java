package org.docear.plugin.services.listeners;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.SwingUtilities;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.RecommendationsController;
import org.freeplane.core.ui.components.UITools;

public class ServiceWindowListener implements WindowListener {

	public void windowOpened(WindowEvent e) {
		final WindowListener wl = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(ServiceController.getController().isAutoRecommending()) {
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
		if (ServiceController.getController().getAutoRecommendations() != null) {
			UITools.getFrame().removeWindowListener(wl);
			RecommendationsController.refreshRecommendations(ServiceController.getController().getAutoRecommendations());
			ServiceController.getController().setAutoRecommendations(null);
		}
	}

}