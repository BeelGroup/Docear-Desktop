package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.core.ui.components.DocearLicencePanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class LicencesPanelAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String title;
	private final String licenceText; 

	public LicencesPanelAction(final String key, final String title, final String text) {
		super(key);
		this.title = title;
		this.licenceText = text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final DocearLicencePanel licencePanel = new DocearLicencePanel();
		licencePanel.setLicenceText(licenceText);
		JOptionPane.showConfirmDialog(UITools.getFrame(), licencePanel, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
	}

}
