package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.components.DocearHTMLPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class GPLPanelAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GPLPanelAction() {
		super("GPLPanelAction");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		DocearHTMLPanel gplPanel = new DocearHTMLPanel();
		String text = DocearController.getController().getGPLv2Terms();
		gplPanel.setText(text);
		JOptionPane.showConfirmDialog(UITools.getFrame(), gplPanel, TextUtils.getText("docear.license.data_processing.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
	}

}
