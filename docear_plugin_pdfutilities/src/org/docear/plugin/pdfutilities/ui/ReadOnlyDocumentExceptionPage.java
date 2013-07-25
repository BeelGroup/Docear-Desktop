package org.docear.plugin.pdfutilities.ui;

import java.awt.Color;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;

import de.intarsys.tools.locator.FileLocator;
import java.awt.BorderLayout;

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
		textLabel.setText(TextUtils.format("docear.pdf.readonly.warning.text", ""));
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
	public void preparePage(WizardContext context) {
		try {
			String file = context.get(FileLocator.class).getCanonicalFile().getName();
			textLabel.setText(TextUtils.format("docear.pdf.readonly.warning.text", file));
		}catch (Exception e) {
			textLabel.setText(TextUtils.format("docear.pdf.readonly.warning.text", ""));
		}
		context.setWizardTitle(TextUtils.getText("docear.pdf.readonly.warning.title"));
		context.getBackButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.skipall"));
		context.getBackButton().setVisible(true);
		context.getNextButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.retry"));
		context.getNextButton().setVisible(true);
		context.getSkipButton().setText(TextUtils.getText("docear.pdf.readonly.warning.button.skip"));
		context.getSkipButton().setVisible(true);
	}
}
