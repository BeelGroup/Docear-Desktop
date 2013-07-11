package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.components.DocearLicencePanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class DocearShowDataPrivacyStatementAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	public static final String key = "DocearShowDataPrivacyStatement";
	final DocearLicencePanel licenseText = new DocearLicencePanel();

	public DocearShowDataPrivacyStatementAction() {
		super(key);
		licenseText.setLicenceText(DocearController.getController().getDataPrivacyTerms());
	}

	public void actionPerformed(ActionEvent e) {		
		JOptionPane.showConfirmDialog(UITools.getFrame(), licenseText, TextUtils.getText("docear.license.data_privacy.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
	}

}
