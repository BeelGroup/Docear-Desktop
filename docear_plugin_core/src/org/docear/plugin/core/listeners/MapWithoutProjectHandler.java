package org.docear.plugin.core.listeners;

import java.awt.Dimension;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.features.DocearInternallyLoadedMap;
import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.SelectProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.workspace.actions.DocearNewProjectAction;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class MapWithoutProjectHandler {
	private MapModel map;
	
	public MapWithoutProjectHandler(MapModel map) {
		this.map = map;
	}
	
	void showProjectSelectionWizard() {		
		final Wizard wizard = new Wizard(UITools.getFrame());		
		initWizard(wizard);
		
		int ret = wizard.show();
		if(ret == Wizard.OK_OPTION) {
			DocearWorkspaceProject project = wizard.getContext().get(DocearWorkspaceProject.class);
			Boolean contextObject = wizard.getContext().get(Boolean.class);
			if (wizard.getContext().get(Boolean.class) != null) {
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
					return;
				}
			}
			if (project != null) {
				WorkspaceController.getMapModelExtension(map).setProject(project);
				if (map.getFile() != null) {
					map.setSaved(false);
					((MFileManager) UrlManager.getController()).save(map, true);
				}				
				try {
					LogUtils.info("set project \""+project+"\" for map: \""+map.getTitle());
				}
				catch (Exception e) {					
				}
			}
			
		}
	}

	private void initWizard(Wizard wizard) {
		WizardPageDescriptor desc = new WizardPageDescriptor("page.project.select", new SelectProjectPagePanel(map)) {
			
    		public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {    			
    			AWorkspaceProject project = ((SelectProjectPagePanel)getPage()).getSelectedProject();
    			context.set(DocearWorkspaceProject.class, project);
    			return Wizard.FINISH_PAGE;
    		}
    		
    		public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {    			
    			return context.getModel().getPage("page.project.create");
    		}
    		
    		public WizardPageDescriptor getSkipPageDescriptor(WizardContext context) {
    			context.set(Boolean.class, false);
    			return Wizard.FINISH_PAGE;
    		}
		};		
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		wizard.setCancelEnabled(false);
		
		//new project page
		desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				context.set(DocearWorkspaceProject.class, ((CreateProjectPagePanel) getPage()).getProject());
				context.set(Boolean.class, true);
				return Wizard.FINISH_PAGE;
			}
			
			public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.project.select");
			}
			
			@Override
			public void aboutToDisplayPage(WizardContext context) {
				super.aboutToDisplayPage(context);
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

			@Override
			public void displayingPage(WizardContext context) {
				super.displayingPage(context);
				context.setWizardTitle(TextUtils.getText("workspace.action.node.new.project.dialog.title"));
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

		};
		desc.getPage().setPreferredSize(new Dimension(640, 450));
		wizard.registerWizardPanel(desc);
			
	}
}
