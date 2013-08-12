package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;

public class ShowJabrefPreferencesAction extends AFreeplaneAction{
	public static final String KEY = "show_jabref_preferences";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShowJabrefPreferencesAction() {
		super(KEY);	
	}
	
	

	public void actionPerformed(ActionEvent e) {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame().preferences();
	}
	
	public void afterMapChange(final Object newMap) {
	}
	
}
