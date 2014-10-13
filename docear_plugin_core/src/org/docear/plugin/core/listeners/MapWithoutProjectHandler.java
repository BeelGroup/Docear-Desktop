package org.docear.plugin.core.listeners;

import java.awt.Dimension;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.features.DocearInternallyLoadedMap;
import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.SelectProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.workspace.actions.DocearNewProjectAction;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class MapWithoutProjectHandler {
	
	public static AWorkspaceProject showProjectSelectionWizard(MapModel map) {
		return showProjectSelectionWizard(map, true);
	}
	
	public static AWorkspaceProject showProjectSelectionWizard(MapModel map, boolean showCloseButton) {		
		final Wizard wizard = new Wizard(UITools.getFrame());		
		wizard.getSession().set(MapModel.class, map);		
		initWizard(wizard, showCloseButton);
		
		int ret = wizard.show();
		if(ret == Wizard.OK_OPTION) {
			DocearWorkspaceProject project = wizard.getSession().get(DocearWorkspaceProject.class);
			Boolean contextObject = wizard.getSession().get(Boolean.class);			
			if (wizard.getSession().get(Boolean.class) != null) {
				if (contextObject) {
					DocearNewProjectAction.createProject(project);
				}
				else {
					//skip action means close map, hoewever, do not close internally opened maps
					if (map.getExtension(DocearInternallyLoadedMap.class) == null) {
    					SwingUtilities.invokeLater(new Runnable() {    						
    						@Override
    						public void run() {
    							Controller.getCurrentController().close(true);
    						}
    					});
					}
					return null;
				}
			}
			if(project != null) {
    			WorkspaceController.getMapModelExtension(map).setProject(project);
    			try {
    				LogUtils.info("set project \""+project+"\" for map: \""+map.getTitle());
    			}
    			catch (Exception e) {					
    			}
			}
			
			return project;
		}
		
		return null;
	}

	private static void initWizard(Wizard wizard, boolean showCloseButton) {
		WizardPageDescriptor desc = new WizardPageDescriptor("page.project.select", new SelectProjectPagePanel(showCloseButton)) {
			
    		public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {    			
    			AWorkspaceProject project = ((SelectProjectPagePanel)getPage()).getSelectedProject();
    			context.set(DocearWorkspaceProject.class, project);
    			return Wizard.FINISH_PAGE;
    		}
    		
    		public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {    			
    			return context.getModel().getPage("page.project.create");
    		}
    		
    		public WizardPageDescriptor getSkipPageDescriptor(WizardSession context) {
    			context.set(Boolean.class, false);
    			return Wizard.FINISH_PAGE;
    		}
		};		
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		wizard.setCancelEnabled(false);
		
		//new project page
		desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.set(DocearWorkspaceProject.class, ((CreateProjectPagePanel) getPage()).getProject());
				context.set(Boolean.class, true);
				return Wizard.FINISH_PAGE;
			}
			
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				return context.getModel().getPage("page.project.select");
			}
			
			@Override
			public void aboutToDisplayPage(WizardSession context) {
				super.aboutToDisplayPage(context);
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

			@Override
			public void displayingPage(WizardSession context) {
				super.displayingPage(context);
				context.setWizardTitle(TextUtils.getText("workspace.action.node.new.project.dialog.title"));
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

		};
		desc.getPage().setPreferredSize(new Dimension(640, 450));
		wizard.registerWizardPanel(desc);
			
	}
}
