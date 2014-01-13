package org.docear.plugin.services.features.update.action;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.update.UpdateCheck;
import org.freeplane.core.ui.AFreeplaneAction;

public class DocearCheckForUpdatesAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.update_checker.show";
	
	public DocearCheckForUpdatesAction() {
		super(KEY);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		DocearController.getController().getEventQueue().invoke(new Runnable() {
			public void run() {
				ServiceController.getFeature(UpdateCheck.class).checkForUpdates(true);
			}
		});
		
	}

}
