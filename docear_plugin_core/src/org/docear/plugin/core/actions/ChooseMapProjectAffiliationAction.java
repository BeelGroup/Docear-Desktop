package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.MapProjectAffiliationPage;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class ChooseMapProjectAffiliationAction extends AWorkspaceAction {
	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.action.ChooseMapProjectAffiliation";
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public ChooseMapProjectAffiliationAction() {
		super(KEY);
	}

	/***********************************************************************************
	 * METHODS
	 * @param map 
	 **********************************************************************************/

	public static AWorkspaceProject showChooser(MapModel map) {
		AWorkspaceProject project = null;
		Wizard wizard = new Wizard(UITools.getFrame());
		
		WizardPageDescriptor desc = new WizardPageDescriptor("page.affiliate", new MapProjectAffiliationPage());
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		int response = wizard.show();
		if(response == Wizard.OK_OPTION) {
			WorkspaceMapModelExtension ext = WorkspaceController.getMapModelExtension(map);
			ext.setProject(project);
		}
		
		return project;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}
