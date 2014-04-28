package org.docear.plugin.core.workspace.actions;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler.LOAD_RETURN_TYPE;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearNewProjectAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.project.new";
	
	public DocearNewProjectAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		
		final Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int ret = wiz.show();
				if(ret == Wizard.OK_OPTION) {
					AWorkspaceProject project = wiz.getSession().get(DocearWorkspaceProject.class);
					createProject(project);
				}
			}
		}).start();
	}

	private void initWizard(Wizard wizard) {
		//new project page
		WizardPageDescriptor desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.set(DocearWorkspaceProject.class, ((CreateProjectPagePanel)getPage()).getProject());
				return Wizard.FINISH_PAGE;
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
				context.getBackButton().setVisible(false);
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}
			
			
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
	}

	public static void createProject(final AWorkspaceProject project) {
		try {
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {

					if(project == null) {
						return;
					}
					File path = URIUtils.getFile(project.getProjectHome());
					
					//WORKSPACE - todo: ask for permission to create the directory or check for always_create setting
					if(!path.exists() ) {
						path.mkdirs();
					}
					
					String projectName = null;		
					DocearProjectSettings settings = project.getExtensions(DocearProjectSettings.class);
					if(settings != null) {
						projectName = settings.getProjectName();
					}
					
					WorkspaceController.getCurrentModel().addProject(project);
					try {
						LOAD_RETURN_TYPE return_type = WorkspaceController.getCurrentModeExtension().getProjectLoader().loadProject(project);
						if(return_type == LOAD_RETURN_TYPE.NEW_PROJECT && projectName != null && projectName.length() > 0) {
							project.getModel().getRoot().setName(projectName);
							project.getModel().nodeChanged(project.getModel().getRoot(), null, projectName);
						}
					} catch (IOException e) {
						DocearLogger.error(e);
					}
					try {
						WorkspaceController.save();
					}
					catch (Exception e) {
						DocearLogger.warn(e);
					}
				}
			};
			
			if (EventQueue.isDispatchThread()) {
				runnable.run();
			}
			else {
    			SwingUtilities.invokeAndWait(runnable);
			}
		}
		catch (Exception ex) {
			DocearLogger.warn(ex);
		}
		
	}

}
