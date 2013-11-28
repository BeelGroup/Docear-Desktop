package org.docear.plugin.pdfutilities.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JCheckBox;

public class ViewerSettingsChangeErrorDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public ViewerSettingsChangeErrorDialog(final String readerCommand) {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		

		JLabel message = new JLabel(TextUtils.getText("docear.validate_pdf_xchange.settings_change_error"));
		add(message, "2, 2");
		
		MultiLineActionLabel link = new MultiLineActionLabel(TextUtils.getText("docear.validate_pdf_xchange.settings_change_error.link"));
		add(link, "2, 4");
				 
		// Do not send the settings - they don't help anyway
//		MultiLineActionLabel lbl = new MultiLineActionLabel("<action cmd=\"perform_action\">"+TextUtils.getText("DocearSendPdfxcRegistryAction.text")+"</action>");
//		lbl.addActionListener(new ActionListener() {
//			
//			public void actionPerformed(ActionEvent e) {
//				if ("perform_action".equals(e.getActionCommand())) {
//					Controller.getCurrentController().getAction("DocearSendPdfxcRegistryAction").actionPerformed(e);
//				}
//			}
//		});
//		
//		add(lbl, "2, 6");
		
		link.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if ("open_url".equals(e.getActionCommand())) {
					String anchor = "#PDF_Editors";
					PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
					if (readerFilter.isPdfXChange(readerCommand)) {
						anchor = "#PDF-XChange_Viewer";
					}
					else if (readerFilter.isAcrobat(readerCommand)) {
						anchor = "#Adobe_Acrobat";
					}
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/support/user-manual/"+anchor));
					}
					catch (IOException ex) {
						LogUtils.warn(ex);
					}
				}
			}
		});
		
		final JCheckBox chckbxNeverAskAgain = new JCheckBox(TextUtils.getText("docear.validate_pdf_xchange.settings_change_error.never_show_again"));
		add(chckbxNeverAskAgain, "2, 8");
		chckbxNeverAskAgain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ResourceController.getResourceController().setProperty("docear.pdfxc.settings.error.never_show_again", chckbxNeverAskAgain.isSelected());
			}
		});
	}
	
	public static boolean showWarningEnabled() {
		return !Boolean.parseBoolean(ResourceController.getResourceController().getProperty("docear.pdfxc.settings.error.never_show_again", "false"));
	}
}
