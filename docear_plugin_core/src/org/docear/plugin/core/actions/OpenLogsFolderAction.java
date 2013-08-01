package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class OpenLogsFolderAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String KEY = "OpenLogsFolderAction";
	
	public OpenLogsFolderAction() {
		super(KEY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separatorChar + "logs");
		if (file != null && file.exists()) {
			try {
				Controller.getCurrentController().getViewController().openDocument(file.toURI());
			}
			catch (IOException e1) {
				LogUtils.warn("OpenLogsFolderAction.actionPerformed()");
			}
		}
	}

}
