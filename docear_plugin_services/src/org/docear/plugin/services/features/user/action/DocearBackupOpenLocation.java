package org.docear.plugin.services.features.user.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.docear.plugin.core.features.DocearFileBackupController;
import org.docear.plugin.core.features.IFileBackupHandler;
import org.docear.plugin.services.features.user.UserFileBackupHandler;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnNodeChange=true)
public class DocearBackupOpenLocation extends AFreeplaneAction {
	final static String KEY = "DocearBackupOpenLocation";

	public DocearBackupOpenLocation() {
		super(KEY);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void actionPerformed(ActionEvent arg0) {
		IFileBackupHandler handler = DocearFileBackupController.getFileBackupHandler();
		if (handler instanceof UserFileBackupHandler) {
			File file = ((UserFileBackupHandler) handler).getFolder();
			if (file != null && file.exists()) {
				try {
					Controller.getCurrentController().getViewController().openDocument(file.toURI());
				}
				catch (IOException e) {
					LogUtils.warn("DocearBackupOpenLocation.actionPerformed(): "+e.getMessage());
				}
			}
		}
	}

	@Override
	public void setEnabled() {
		setEnabled(false);
		IFileBackupHandler handler = DocearFileBackupController.getFileBackupHandler();
		if (handler instanceof UserFileBackupHandler) {
			File file = ((UserFileBackupHandler) handler).getFolder();
			if (file != null && file.exists()) {
				setEnabled(true);
			}
		}
	}
	
	

}
