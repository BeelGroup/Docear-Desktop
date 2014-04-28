package org.docear.plugin.services.features.user.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.setup.DocearServiceTestTask;
import org.docear.plugin.services.features.setup.view.RegistrationPagePanel;
import org.docear.plugin.services.features.setup.view.VerifyServicePagePanel;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.view.KeepWorkspaceSettingsPagePanel;
import org.docear.plugin.services.features.user.workspace.DocearWorkspaceSettings;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;

public class DocearUserRegistrationAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.action.user.register";
	
	public DocearUserRegistrationAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		showRegistrationWizard();
	}

	public static Object addRegistrationPages(Wizard wizard) {
		Object startId;
		//registration page
		WizardPageDescriptor desc = new WizardPageDescriptor("page.registration", new RegistrationPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				((RegistrationPagePanel)getPage()).getUser();
				return context.getModel().getPage("page.verify.registration");
			}

			@Override
			public void aboutToDisplayPage(WizardSession context) {
				super.aboutToDisplayPage(context);
				context.getBackButton().setVisible(false);
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,380));
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		startId = desc.getIdentifier();
		
		//registration verification
		final DocearServiceTestTask task = getRegistrationVerificationTask();
		desc = new WizardPageDescriptor("page.verify.registration", new VerifyServicePagePanel("Registration", task, false)) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
					context.getTraversalLog().getPreviousPage(context);
					if(DocearUserController.getActiveUser() instanceof DocearLocalUser) {
						return context.getModel().getPage("page.registration.keep_workspace");
					}
					else {
						return Wizard.FINISH_PAGE;
					}
			}

			@Override
			public void aboutToDisplayPage(WizardSession context) {
				super.aboutToDisplayPage(context);
				if(DocearUserController.getActiveUser() instanceof DocearLocalUser) {
					context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.next"));
				}
				else {
					context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
				}
				if(task.isSuccessful()) {
					context.getBackButton().setEnabled(false);
				}
			}
			
			
		};
		desc.getPage().setSkipOnBack(true);
		wizard.registerWizardPanel(desc);
		
		desc = new WizardPageDescriptor("page.registration.keep_workspace", new KeepWorkspaceSettingsPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
					WorkspaceController.save();
					if(((KeepWorkspaceSettingsPagePanel) getPage()).isKeepSettingsEnabled()) {
						try {
							moveUserSettings(DocearUserController.getActiveUser(), context.get(DocearUser.class));
						}
						catch (Exception e) {
							LogUtils.warn("org.docear.plugin.services.features.user.action.DocearUserRegistrationAction.initWizard(wizard): "+ e.getMessage());
						}
					}
					return Wizard.FINISH_PAGE;
			}
			
		};
		desc.getPage().setSkipOnBack(true);
		wizard.registerWizardPanel(desc);
		
		return startId;
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
	
	public static void moveUserSettings(DocearUser srcUser, DocearUser destUser) throws IOException {
		if(srcUser == null || destUser == null) {
			return;
		}
		DocearWorkspaceSettings settings = ServiceController.getFeature(DocearWorkspaceSettings.class);
		
		File srcDir = new File(settings.getSettingsPath(srcUser));
		File destDir = new File(settings.getSettingsPath(destUser));
		if(srcDir.exists()) {
			FileUtils.moveDirectory(srcDir, destDir);
		}
	}
	
	public static void useRegisteredUser(final Wizard wizard) {
		DocearUser user = wizard.getSession().get(DocearUser.class);
		DocearUser clone = user.clone();
		user.activate();
		user.setBackupEnabled(clone.isBackupEnabled());
		user.setCollaborationEnabled(clone.isCollaborationEnabled());
		user.setSynchronizationEnabled(clone.isSynchronizationEnabled());
		user.setRecommendationsEnabled(clone.isRecommendationsEnabled());
		DocearController.getController().getEventQueue().invoke(new Runnable() {
			public void run() {
				WorkspaceController.save();
			}
		});
	}

	public static void showRegistrationWizard() {
		final Wizard wiz = new Wizard(UITools.getFrame());
		addRegistrationPages(wiz);
		
		new Thread(new Runnable() {
			public void run() {
				int ret = wiz.show();
				if(ret == Wizard.OK_OPTION) {
					useRegisteredUser(wiz);
				}
			}
		}).start();
	}

}
