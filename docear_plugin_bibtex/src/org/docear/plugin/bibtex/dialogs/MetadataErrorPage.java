package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;

import javax.swing.JLabel;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MetadataErrorPage extends AWizardPage {

	private static final long serialVersionUID = 1L;
	private final boolean showRegistration;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param message 
	 **********************************************************************************/
	public MetadataErrorPage(String message, boolean showRegistration) {
		setBackground(Color.WHITE);
		this.showRegistration = showRegistration;
		
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("top:default:grow"),}));
		
		JLabel lblMessage = new JLabel(message);
		add(lblMessage, "1, 1");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.metadata.import.error");
	}

	@Override
	public void preparePage(WizardContext context) {
		context.setWizardTitle(getTitle());
		
		context.getNextButton().setText(TextUtils.getText("docear.metadata.import.error.close"));
		
		context.getBackButton().setText(TextUtils.getText("docear.metadata.import.error.register"));
		context.getBackButton().setVisible(showRegistration);
		context.getBackButton().setEnabled(showRegistration);
		
		
	}
}
