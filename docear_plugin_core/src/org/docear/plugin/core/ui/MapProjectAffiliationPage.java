package org.docear.plugin.core.ui;

import java.awt.Color;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MapProjectAffiliationPage extends AWizardPage {

	private static final long serialVersionUID = 1L;
	private DefaultComboBoxModel model;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public MapProjectAffiliationPage() {
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
		
		MultiLineActionLabel multiLineActionLabel = new MultiLineActionLabel(TextUtils.getRawText("docear.wizard.map.affiliation.text"));
		multiLineActionLabel.setBackground(Color.WHITE);
		add(multiLineActionLabel, "2, 2, 3, 1, fill, fill");
		
		JLabel lblProject = new JLabel(TextUtils.getRawText("docear.wizard.map.affiliation.label"));
		add(lblProject, "2, 4, right, default");
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(getModel());
		add(comboBox, "4, 4, fill, default");
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private ComboBoxModel getModel() {
		if(model == null) {
			model = new DefaultComboBoxModel();
			String item0 = TextUtils.getRawText("docear.wizard.map.affiliation.item0");
			model.addElement(item0);
			model.setSelectedItem(item0);
			for(AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
				model.addElement(new ProjectItem(project));
			}
		}
		return model;
	}
	
	public AWorkspaceProject getSelectedItem() {
		Object item = getModel().getSelectedItem();
		if(item instanceof ProjectItem) {
			return ((ProjectItem) item).getProject();
		}
		return null;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public String getTitle() {
		return TextUtils.getRawText("docear.wizard.map.affiliation.title");
	}

	@Override
	public void preparePage(WizardContext context) {
		context.setWizardTitle(getTitle());
		context.getNextButton().setText(TextUtils.getRawText("docear.setup.wizard.controls.finish"));
	}
	

	public class ProjectItem {
		private final AWorkspaceProject project;
		public ProjectItem(AWorkspaceProject project) {
			this.project = project;
		}
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		public AWorkspaceProject getProject() {
			return this.project;
		}
		
		public String toString() {
			return project.getProjectName() + " ("+project.getProjectID()+")"; 
		}
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}

}
