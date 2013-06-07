package org.docear.plugin.services.features.user.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.setup.DocearServiceTestTask;
import org.docear.plugin.services.features.setup.view.RegistrationPagePanel;
import org.docear.plugin.services.features.setup.view.VerifyServicePagePanel;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;

public class DocearUserRegistrationAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.user.register";
	
	public DocearUserRegistrationAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		final Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		
		new Thread(new Runnable() {
			public void run() {
				int ret = wiz.show();
				if(ret == Wizard.OK_OPTION) {
					
				}
			}
		}).start();
	}
	
	private void initWizard(Wizard wizard) {
		//registration page
		WizardPageDescriptor desc = new WizardPageDescriptor("page.registration", new RegistrationPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.verify.registration");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		
		//registration verification
		desc = new WizardPageDescriptor("page.verify.registration", new VerifyServicePagePanel("Registration", getRegistrationVerificationTask(), false)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
					context.getTraversalLog().getPreviousPage(context);
					return context.getModel().getPage("page.project.create");
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {
				if(context.get(DocearUser.class).isNew()) {
					context.getTraversalLog().getPreviousPage(context);
				}
				return super.getBackPageDescriptor(context);
			}
			
			
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
	}
	
	public static DocearServiceTestTask getRegistrationVerificationTask() {
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
								ServiceController.getFeature(DocearUserController.class).createUserAccount(user);
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
					success = true;
				}
			}
		};
	}

}
