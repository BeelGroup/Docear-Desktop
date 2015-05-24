package org.docear.plugin.services.features.setup.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.ImportProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.workspace.actions.DocearImportProjectAction;
import org.docear.plugin.core.workspace.actions.DocearNewProjectAction;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.setup.view.RegistrationPagePanel;
import org.docear.plugin.services.features.setup.view.SecondPagePanel;
import org.docear.plugin.services.features.setup.view.SecondPagePanel.DATA_OPTION;
import org.docear.plugin.services.features.setup.view.StartPagePanel;
import org.docear.plugin.services.features.setup.view.StartPagePanel.START_OPTION;
import org.docear.plugin.services.features.setup.view.VerifyServicePagePanel;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.action.DocearUserLoginAction;
import org.docear.plugin.services.features.user.action.DocearUserRegistrationAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;

public class DocearSetupWizardAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.setup.wizard.action";

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearSetupWizardAction() {
		super(KEY);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void actionPerformed(ActionEvent e) {
		startWizard(false);
	}
	
	public static void startWizard(boolean exitOnCancel) {
		Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		if(!Compat.isWindowsOS()) {
			UITools.backOtherWindows();
		}		
		int ret = wiz.show();
		if(ret == Wizard.OK_OPTION) {
			if(wiz.getSession().get(DocearLocalUser.class) != null) {
				DocearUserController.LOCAL_USER.activate();
			}
			else {
				wiz.getSession().get(DocearUser.class).activate();
			}
			DocearWorkspaceProject project = wiz.getSession().get(DocearWorkspaceProject.class);
			if(project != null) {
				if(wiz.getSession().get(START_OPTION.class) == START_OPTION.REGISTRATION || wiz.getSession().get(DATA_OPTION.class) == DATA_OPTION.CREATE) {
					DocearNewProjectAction.createProject(project);
				}
				else if(wiz.getSession().get(DATA_OPTION.class) == DATA_OPTION.IMPORT) {
					DocearImportProjectAction.importProject(project);
				}
			}
			WorkspaceController.save();
			DocearController.getPropertiesController().saveProperties();
		}
		else {
			if(exitOnCancel) {
				System.exit(0);
			}
		}
	}

	private static void initWizard(final Wizard wizard) {
		//first page
		WizardPageDescriptor desc = new WizardPageDescriptor("page.first", new StartPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				if(StartPagePanel.START_OPTION.LOGIN.equals(context.get(StartPagePanel.START_OPTION.class))) {
					return context.getModel().getPage("page.verify.login");
				}
				context.set(DocearUser.class, new DocearUser());
				return context.getModel().getPage("page.registration");
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				context.set(DocearLocalUser.class, DocearUserController.LOCAL_USER);
				context.getTraversalLog().add(this);
				return context.getModel().getPage("page.second");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		DocearUser user = ServiceController.getCurrentUser();
		wizard.getSession().set(DocearUser.class, user);
		wizard.setStartPage(desc.getIdentifier());
		
		//login verification
		desc = new WizardPageDescriptor("page.verify.login", new VerifyServicePagePanel("Log-In", DocearUserLoginAction.getLoginVerificationTask(), true)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				return context.getModel().getPage("page.second");
			}
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		
		//registration page
		desc = new WizardPageDescriptor("page.registration", new RegistrationPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				((RegistrationPagePanel)getPage()).getUser();
				return context.getModel().getPage("page.verify.registration");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//registration verification
		desc = new WizardPageDescriptor("page.verify.registration", new VerifyServicePagePanel("Registration", DocearUserRegistrationAction.getRegistrationVerificationTask(), false)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
					context.getTraversalLog().getPreviousPage(context);
					return context.getModel().getPage("page.project.create");
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				if(context.get(DocearUser.class).isNew()) {
					context.getTraversalLog().getPreviousPage(context);
				}
				return super.getBackPageDescriptor(context);
			}
			
			
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//choose further actions page
		desc = new WizardPageDescriptor("page.second", new SecondPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				((SecondPagePanel)getPage()).getUser();
				if(DATA_OPTION.SYNCH.equals(context.get(DATA_OPTION.class))) {
					return context.getModel().getPage("page.project.synch");
				}
				if(DATA_OPTION.CREATE.equals(context.get(DATA_OPTION.class))) {
					return context.getModel().getPage("page.project.create");
				}
				if(DATA_OPTION.EMPTY.equals(context.get(DATA_OPTION.class))) {
					return Wizard.FINISH_PAGE;
				}
				return context.getModel().getPage("page.project.import");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		//TODO Service
		if(!DocearController.getController().isServiceAvailable()){
			wizard.getSession().set(DocearLocalUser.class, DocearUserController.LOCAL_USER);		
			wizard.setStartPage(desc.getIdentifier());
		}
		
		//new project page
		desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.set(DocearWorkspaceProject.class, ((CreateProjectPagePanel)getPage()).getProject());
				return Wizard.FINISH_PAGE;
			}

			@Override
			public void aboutToDisplayPage(WizardSession context) {
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
				super.aboutToDisplayPage(context);
			}
			
			
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);		
		
		//import project page
		desc = new WizardPageDescriptor("page.project.import", new ImportProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.set(DocearWorkspaceProject.class, ((ImportProjectPagePanel)getPage()).getProject());
				return Wizard.FINISH_PAGE;
			}

			@Override
			public void aboutToDisplayPage(WizardSession context) {
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
				super.aboutToDisplayPage(context);
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);		
	}
	
}
