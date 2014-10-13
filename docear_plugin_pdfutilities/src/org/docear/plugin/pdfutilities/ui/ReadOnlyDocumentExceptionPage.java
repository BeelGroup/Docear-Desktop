package org.docear.plugin.pdfutilities.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.util.TextUtils;

import de.intarsys.tools.locator.FileLocator;

public class ReadOnlyDocumentExceptionPage extends AWizardPage {
	
	private static final long serialVersionUID = 1L;
	private MultiLineActionLabel textLabel;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ReadOnlyDocumentExceptionPage() {
		super();
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());
		
		textLabel = new MultiLineActionLabel();
		textLabel.setBackground(Color.WHITE);		
		add(textLabel, BorderLayout.CENTER);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preparePage(WizardSession session) {
		try {
			final String file = session.get(FileLocator.class).getCanonicalFile().getAbsolutePath();
			String text = TextUtils.format("docear.pdf.readonly.warning.text", file);
			System.out.println("text: "+text);
			textLabel.setText(text);
			textLabel.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals("open_file")) {
						PdfUtilitiesController.getController().openPdfOnPage(new File(file).toURI(), 1);
					}
				}
			});
		}
		catch (Exception e) {
			textLabel.setText(TextUtils.format("docear.pdf.readonly.warning.text", ""));
		}
		session.setWizardTitle(TextUtils.getText("docear.pdf.readonly.warning.title"));
		session.getBackButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.skipall"));
		session.getBackButton().setVisible(true);
		session.getNextButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.retry"));
		session.getNextButton().setVisible(true);
		session.getSkipButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.skip"));
		session.getSkipButton().setVisible(true);
	}
}
