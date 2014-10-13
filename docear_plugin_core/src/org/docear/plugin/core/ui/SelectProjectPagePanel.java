package org.docear.plugin.core.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SelectProjectPagePanel extends AWizardPage {
	/**
	 * 
	 */
		
	private static final long serialVersionUID = 1L;	
	private JComboBox comboBox;
	private MapModel map;
	private JEditorPane lblNewLabel;
	private boolean showCloseButton;

	public SelectProjectPagePanel(boolean showCloseButton) {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("190px"),
				ColumnSpec.decode("328px"),},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		setBackground(Color.WHITE);
		
		this.showCloseButton = showCloseButton;
		
		List<String> projectNames = new ArrayList<String>();		
		for (AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			projectNames.add(project.getProjectName());
		}
		
		lblNewLabel = new JEditorPane();
		lblNewLabel.setEditable(false);
		lblNewLabel.setContentType("text/html");
		add(lblNewLabel, "1, 2, 2, 1");
		
		JEditorPane lblNewLabel_1 = new JEditorPane();
		lblNewLabel_1.setEditable(false);
		lblNewLabel_1.setContentType("text/html");
		lblNewLabel_1.setText(TextUtils.getText("docear.wizard.select.mindmap.project.select"));
		add(lblNewLabel_1, "1, 8, 1, 1");
		
		comboBox = new JComboBox(projectNames.toArray());
		AWorkspaceProject project = WorkspaceController.getSelectedProject();
		if(project != null) {
			comboBox.setSelectedItem(project.getProjectName());
		}
		add(comboBox, "2, 8, left, top");
		
	}
	
	
	
	public AWorkspaceProject getSelectedProject() {
		String projectName = (String) comboBox.getSelectedItem();
		for (AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			if (project.getProjectName().equals(projectName)) {
				return project;
			}
		}
		
		return null;
	}
	
	@Override
	public void preparePage(WizardSession context) {
		context.setWizardTitle(getTitle());
		context.getBackButton().setText(TextUtils.getText("docear.setup.wizard.second.select.label"));
		getRootPane().setDefaultButton((JButton) context.getBackButton());
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.second.create.label"));
		
		context.getSkipButton().setText(TextUtils.getText("docear.setup.wizard.second.create.close"));
		context.getSkipButton().setEnabled(showCloseButton);
		context.getSkipButton().setVisible(showCloseButton);
	
		map = context.get(MapModel.class);
		lblNewLabel.setText(TextUtils.format("docear.wizard.select.mindmap.project.mindmapname", map.getTitle()));
	}

	@Override
	public String getTitle() {
		return TextUtils.getText("workspace.action.node.select.project.dialog.title");		
	}

}
