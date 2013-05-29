package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.features.setup.DocearServiceTestTask;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class VerifyServicePagePanel extends AWizardPage {
	private static final long serialVersionUID = 1L;
	private JLabel lblMessage;
	private final DocearServiceTestTask test;
	private final String title;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param settings 
	 **********************************************************************************/
	public VerifyServicePagePanel(String title, DocearServiceTestTask task) {
		this.title = title;
		this.test = task;
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		lblMessage = new JLabel();
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessage.setVerticalAlignment(SwingConstants.TOP);
		lblMessage.setText("<html><body><center>Your password and/or username or email was not correct. Please try again.</center></body></html>");
		add(lblMessage, "2, 2, fill, fill");
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	@Override
	public String getTitle() {
		return TextUtils.format(("docear.setup.wizard.verification."+title).toLowerCase(Locale.ENGLISH), TextUtils.getText(("docear.setup.wizard.verification."+title+".result."+test.isSuccessful()).toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public void preparePage(WizardContext context) {
		DocearUser settings = context.get(DocearUser.class);
		try {
			test.run(settings);
			context.getNextButton().setEnabled(true);
			lblMessage.setText(TextUtils.getText(("docear.setup.wizard.verification."+title+".msg").toLowerCase(Locale.ENGLISH)));
		} catch (DocearServiceException e) {
			LogUtils.warn(e);
			context.getNextButton().setEnabled(false);
			lblMessage.setText(e.getMessage());
		}
		context.setWizardTitle(getTitle());
	}

}
