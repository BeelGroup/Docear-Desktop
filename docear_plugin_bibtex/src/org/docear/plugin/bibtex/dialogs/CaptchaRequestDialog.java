package org.docear.plugin.bibtex.dialogs;

import org.docear.metadata.events.CaptchaEvent;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.freeplane.core.ui.components.UITools;


public class CaptchaRequestDialog {
	
	public static String showDialog(CaptchaEvent event) {
		if(event == null || event.getCaptcha() == null) return null;
		final Wizard wiz = new Wizard(UITools.getFrame());		
		wiz.getSession().set(event.getClass(), event);
		
		WizardPageDescriptor desc = new WizardPageDescriptor("captchaRequest", new CaptchaRequestPage()){

			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {				
				return Wizard.FINISH_PAGE;
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				wiz.cancel();
				return Wizard.FINISH_PAGE;
			}
			
		};
		
		wiz.registerWizardPanel(desc);
		wiz.setStartPage(desc.getIdentifier());
		wiz.show();				
		return event.getSolvedCaptcha();		
	}
}
