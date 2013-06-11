package org.docear.plugin.services.features.user.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.setup.DocearServiceTestTask;
import org.docear.plugin.services.features.setup.view.VerifyServicePagePanel;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.view.LoginPagePanel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;

public class DocearUserLoginAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.user.login";
	
	public DocearUserLoginAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		final Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		
		new Thread(new Runnable() {
			public void run() {
				int ret = wiz.show();
				if(ret == Wizard.OK_OPTION) {
					DocearUser user;
					if(wiz.getContext().get(DocearLocalUser.class) != null) {
						user = DocearUserController.LOCAL_USER;
					}
					else {
						user = wiz.getContext().get(DocearUser.class);
					}
					if(!user.equals(DocearUserController.getActiveUser())) {
						user.activate();
					}
					try {
						WorkspaceController.getCurrentModeExtension().getView().refreshView();
					}
					catch (Exception e) {
					}
				}
			}
		}).start();
	}
	
	private void initWizard(Wizard wizard) {
		//registration page
		WizardPageDescriptor desc = new WizardPageDescriptor("page.login", new LoginPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.verify.login");
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,320));
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		wizard.getContext().set(DocearUser.class, DocearUserController.getActiveUser());
		
		//login verification
		desc = new WizardPageDescriptor("page.verify.login", new VerifyServicePagePanel("Log-In", getLoginVerificationTask(), true)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				return Wizard.FINISH_PAGE;
			}
		};
		desc.getPage().setSkipOnBack(true);
		desc.getPage().setPreferredSize(new Dimension(640,480));
		wizard.registerWizardPanel(desc);
		
	}
	
	public static DocearServiceTestTask getLoginVerificationTask() {
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
								ServiceController.getFeature(DocearUserController.class).loginUser(user);
							} catch (DocearServiceException e) {
								if(DocearServiceExceptionType.NO_CONNECTION.equals(e.getType()) && user.getAccessToken() != null) {
									user.setEnabled(true);
									user.setAccessToken(user.getAccessToken());
								}
								else {
									ex = e;
								}
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
