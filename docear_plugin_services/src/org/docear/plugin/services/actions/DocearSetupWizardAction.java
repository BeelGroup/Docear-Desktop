package org.docear.plugin.services.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.ImportProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.communications.features.DocearUserController;
import org.docear.plugin.services.components.setup.RegistrationPagePanel;
import org.docear.plugin.services.components.setup.SecondPagePanel;
import org.docear.plugin.services.components.setup.StartPagePanel;
import org.docear.plugin.services.components.setup.VerifyServicePagePanel;
import org.docear.plugin.services.features.DocearServiceTestTask;
import org.docear.plugin.services.user.DocearUser;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.user.LocalUser;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;

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
		startWizard(true);
	}
	
	public static void startWizard(boolean exitOnCancel) {
		DocearUser settings = ServiceController.getUser();
		
		Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		wiz.getContext().set(DocearUser.class, settings);
		int ret = wiz.show();
		wiz.getContext();
		if(ret == Wizard.OK_OPTION) {
			if(wiz.getContext().get(LocalUser.class) != null) {
				UserAccountController.getController().setActiveUser(wiz.getContext().get(LocalUser.class));
			}
			else {
				
			}
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
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				if(StartPagePanel.OPTION.LOGIN.equals(context.get(StartPagePanel.OPTION.class))) {
					return context.getModel().getPage("page.verify.login");
				}
				return context.getModel().getPage("page.registration");
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {
				context.set(LocalUser.class, new LocalUser("local"));
				wizard.finish();
				return Wizard.FINISH_PAGE;
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		wizard.setCurrentPage(desc.getIdentifier());
		
		//login verification
		desc = new WizardPageDescriptor("page.verify.login", new VerifyServicePagePanel("Log-In", getLoginVerificationTask())) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.second");
			}
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		
		//registration page
		desc = new WizardPageDescriptor("page.registration", new RegistrationPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.verify.registration");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//registration verification
		desc = new WizardPageDescriptor("page.verify.registration", new VerifyServicePagePanel("Registration", null)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
					return context.getModel().getPage("page.project.create");
			}
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//choose further actions page
		desc = new WizardPageDescriptor("page.second", new SecondPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.project.import");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//new project page
		desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return Wizard.FINISH_PAGE;
			}

			@Override
			public void aboutToDisplayPage(WizardContext context) {
				context.getNextButton().setText("Finish");
				super.aboutToDisplayPage(context);
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
		//import project page
		desc = new WizardPageDescriptor("page.project.import", new ImportProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				context.getNextButton().setText("Finish");
				return Wizard.FINISH_PAGE;
			}

			@Override
			public void aboutToDisplayPage(WizardContext context) {
				context.getNextButton().setText("Finish");
				super.aboutToDisplayPage(context);
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
	}

	private static DocearServiceTestTask getLoginVerificationTask() {
		return new DocearServiceTestTask() {
			private boolean success = false;
			private DocearServiceException ex = null;
			public boolean isSuccessful() {
				return success;
			}

			public void run(final DocearUser user) throws DocearServiceException {
				ex = null;
				success = false;
				
				if(!user.isValid()) {
					Thread task = new Thread() {
						public void run() {
							try {
								DocearUserController.getController().loginUser(user);
							} catch (DocearServiceException e) {
								ex = e;
							}						
						}
					};
					task.start();
					try {
						task.join();
					} catch (InterruptedException e) {
						LogUtils.warn(e);
					}
					if(ex != null) {
						throw ex;
					}
				}
				
				if(user.isValid()) {
					success = true;
				}
			}
		};
	}
}
