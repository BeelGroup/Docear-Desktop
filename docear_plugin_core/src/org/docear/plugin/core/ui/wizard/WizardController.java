package org.docear.plugin.core.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author genzmehr@docear.org
 *
 */
public class WizardController implements ActionListener {

	private final Wizard wizard;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WizardController(Wizard wizard) {
		this.wizard = wizard; 
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(wizard.isCancelControl(e.getSource())) {
			wizard.cancel();
		}
		else {
			if(e.getSource().equals(wizard.getSession().getNextButton())) {
				WizardPageDescriptor desc = wizard.getSession().getCurrentDescriptor().getNextPageDescriptor(wizard.getSession());
				if(desc == null || Wizard.FINISH_PAGE.equals(desc)) {
					wizard.finish();
				}
				else {
					wizard.getSession().getTraversalLog().add(wizard.getSession().getCurrentDescriptor());
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
			else if(e.getSource().equals(wizard.getSession().getBackButton())) {
				WizardPageDescriptor desc = wizard.getSession().getCurrentDescriptor().getBackPageDescriptor(wizard.getSession());
				if(desc == null) {
					wizard.getSession().getBackButton().setEnabled(false);
				}
				else {
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
			else if(e.getSource().equals(wizard.getSession().getSkipButton())) {
				WizardPageDescriptor desc = wizard.getSession().getCurrentDescriptor().getSkipPageDescriptor(wizard.getSession());
				if(desc == null || Wizard.FINISH_PAGE.equals(desc)) {
					wizard.finish();
				}
				else {
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
		}
	}
}
