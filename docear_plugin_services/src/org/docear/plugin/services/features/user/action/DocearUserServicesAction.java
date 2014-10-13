package org.docear.plugin.services.features.user.action;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.view.EnableServicesPagePanel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;

public class DocearUserServicesAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.action.user.services";
	
	public DocearUserServicesAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		DocearUser user = DocearUserController.getActiveUser();
		
		showServicesWizard(user);
	}
	
	private static void initWizard(Wizard wizard) {
		WizardPageDescriptor desc = new WizardPageDescriptor("page.services", new EnableServicesPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.get(DocearUser.class).setBackupEnabled(((EnableServicesPagePanel) getPage()).isOnlineBackupEnabled());
				context.get(DocearUser.class).setSynchronizationEnabled(((EnableServicesPagePanel) getPage()).isSynchronizationEnabled());
				context.get(DocearUser.class).setRecommendationsEnabled(((EnableServicesPagePanel) getPage()).isRecommendationsEnabled());
				context.get(DocearUser.class).setCollaborationEnabled(((EnableServicesPagePanel) getPage()).isCollaborationEnabled());
				return Wizard.FINISH_PAGE;
			}
		};
		desc.getPage().setPreferredSize(new Dimension(640,240));
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
	}
	
	public static void showServicesWizard(DocearUser user) {
		if(user == null) {
			throw new IllegalArgumentException("NULL");
		}
		
		if(user instanceof DocearLocalUser) {
			DocearUserRegistrationAction.showRegistrationWizard();
			return;
		}
		
		final Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		wiz.getSession().set(DocearUser.class, user);
		new Thread(new Runnable() {
			public void run() {
				int ret = wiz.show();
				if(ret == Wizard.OK_OPTION) {
					WorkspaceController.save();
					
					if (DocumentRetrievalController.getController() != null && DocumentRetrievalController.getView() != null) {
						DocumentRetrievalController.getController().refreshDocuments();
					}
				}
			}
		}).start();
	}
}
