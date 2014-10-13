package org.docear.plugin.services.features.user.view;

import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JEditorPane;

public class KeepWorkspaceSettingsPagePanel extends AWizardPage {
	private static final long serialVersionUID = 1L;
	private JRadioButton ckbxKeepSettings;
	private MultiLineActionLabel keepLabel;
	private MultiLineActionLabel freshLabel;
	private JEditorPane editorPane;

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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		ckbxKeepSettings = new JRadioButton();
		ckbxKeepSettings.setBackground(Color.WHITE);
		ckbxKeepSettings.setSelected(true);
		add(ckbxKeepSettings, "2, 4, default, top");
		
		keepLabel = new MultiLineActionLabel(TextUtils.format("docear.wizard.registration.keep_workspace", ""));
		keepLabel.setBackground(Color.WHITE);
		add(keepLabel, "4, 4, fill, fill");
		
		JRadioButton rdbtnStartFresh = new JRadioButton();
		rdbtnStartFresh.setBackground(Color.WHITE);
		add(rdbtnStartFresh, "2, 6, default, top");
		
		freshLabel = new MultiLineActionLabel(TextUtils.format("docear.wizard.registration.start_fresh", ""));
		freshLabel.setBackground(Color.WHITE);
		add(freshLabel, "4, 6, fill, fill");
		
		ButtonGroup group = new ButtonGroup();
		group.add(ckbxKeepSettings);
		group.add(rdbtnStartFresh);
		
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setText(TextUtils.getRawText("docear.wizard.registration.keep.info"));
		add(editorPane, "2, 10, 3, 1, fill, fill");
		
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
	public void preparePage(WizardSession context) {
		DocearUser user = context.get(DocearUser.class);
		if(user != null) {
			keepLabel.setText(TextUtils.format("docear.wizard.registration.keep_workspace", user.getName()));
			freshLabel.setText(TextUtils.format("docear.wizard.registration.start_fresh", user.getName()));
		}
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
		context.getBackButton().setVisible(false);
	}
}
