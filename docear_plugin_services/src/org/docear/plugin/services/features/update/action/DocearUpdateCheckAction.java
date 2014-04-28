package org.docear.plugin.services.features.update.action;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.Version;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.update.UpdateCheck;
import org.docear.plugin.services.features.update.view.UpdateCheckerDialogPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class DocearUpdateCheckAction extends AFreeplaneAction {
	
	public static final String KEY = "UpdateCheckAction";
	
	private static final long serialVersionUID = 1L;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearUpdateCheckAction() {
		super(KEY);
	}
	/***********************************************************************************
	 * METHODS
	 * @param latestVersionString 
	 **********************************************************************************/
	
	public static void showDialog(final Version runningVersion, final Version latestVersion) {
		final String latestVersionString = latestVersion.toString();
		new Thread(new Runnable() {
			public void run() {
				try {
					UpdateCheckerDialogPanel updateAvailablePage = new UpdateCheckerDialogPanel("", runningVersion.toString(), latestVersionString, latestVersion.getStatus());
					Wizard wizard = initWizard(updateAvailablePage);
					//JOptionPane.showMessageDialog(UITools.getFrame(), dialogPanel, TextUtils.getText("docear.version.check.title"), JOptionPane.INFORMATION_MESSAGE);
					//DocearController.getPropertiesController().setProperty("docear.update_checker.options", dialogPanel.getChoice());
					int choice = wizard.show();
					if(choice == Wizard.OK_OPTION) {
						String uri = null;
						if (latestVersion.getStatus().equals(Version.StatusName.devel.name())) {
							uri = "http://www.docear.org/support/forums/docear-support-forums-group3/experimental-releases-forum8/";
						}
						else {
							uri = "http://www.docear.org/software/download/";
						}
						Controller.getCurrentController().getViewController().openDocument(new URI(uri));
					}
					
					if(choice == Wizard.SKIP_OPTION) {
						//don't show the same version again
						DocearController.getPropertiesController().setProperty("docer.update_checker.savedLatestVersion", latestVersionString);
					}
				}
				catch (Exception e) {
					LogUtils.warn("org.docear.plugin.services.features.update.action.DocearUpdateCheckAction.showDialog(...).new Runnable() {...}.run(): "+e.getMessage());
				}
			}
		}).start();
	}
	
	private static Wizard initWizard(UpdateCheckerDialogPanel page) {
		final Wizard wizard = new Wizard(UITools.getFrame());
		
		WizardPageDescriptor desc = new WizardPageDescriptor("page", page) {

			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				return Wizard.FINISH_PAGE;
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				wizard.skipAll();
				return null;
			}

			@Override
			public WizardPageDescriptor getSkipPageDescriptor(WizardSession context) {
				wizard.cancel();
				return null;
			}
			
		};
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		
		return wizard;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void actionPerformed(ActionEvent e) {
		DocearController.getController().getEventQueue().invoke(new Runnable() {
			public void run() {
				ServiceController.getFeature(UpdateCheck.class).checkForUpdates(true);
			}
		});
	}	
}
