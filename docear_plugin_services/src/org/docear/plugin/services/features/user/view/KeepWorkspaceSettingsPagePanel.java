package org.docear.plugin.services.features.user.view;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.freeplane.core.util.TextUtils;

import javax.swing.JCheckBox;
import java.awt.Color;

public class KeepWorkspaceSettingsPagePanel extends AWizardPage {
	private static final long serialVersionUID = 1L;
	private JCheckBox ckbxKeepSettings;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public KeepWorkspaceSettingsPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		ckbxKeepSettings = new JCheckBox();
		ckbxKeepSettings.setBackground(Color.WHITE);
		ckbxKeepSettings.setSelected(true);
		add(ckbxKeepSettings, "2, 4");
		
		MultiLineActionLabel multiLineActionLabel = new MultiLineActionLabel(TextUtils.getRawText("docear.wizard.registration.keep_workspace"));
		multiLineActionLabel.setBackground(Color.WHITE);
		add(multiLineActionLabel, "4, 4, fill, fill");
		
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean isKeepSettingsEnabled() {
		return ckbxKeepSettings.isSelected();
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public String getTitle() {
		return TextUtils.getRawText("docear.wizard.registration.keep_workspace.title");
	}

	@Override
	public void preparePage(WizardContext context) {
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
		context.getBackButton().setVisible(false);
	}
}
