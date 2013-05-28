package org.docear.plugin.core.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
			wizard.resetControls();
			if(e.getSource().equals(wizard.getContext().getNextButton())) {
				WizardPageDescriptor desc = wizard.getContext().getCurrentDescriptor().getNextPageDescriptor(wizard.getContext());
				if(desc == null || Wizard.FINISH_PAGE.equals(desc)) {
					wizard.finish();
				}
				else {
					wizard.getContext().getTraversalLog().add(wizard.getContext().getCurrentDescriptor());
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
			else if(e.getSource().equals(wizard.getContext().getBackButton())) {
				WizardPageDescriptor desc = wizard.getContext().getCurrentDescriptor().getBackPageDescriptor(wizard.getContext());
				if(desc == null) {
					wizard.getContext().getBackButton().setEnabled(false);
				}
				else {
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
			else if(e.getSource().equals(wizard.getContext().getSkipButton())) {
				WizardPageDescriptor desc = wizard.getContext().getCurrentDescriptor().getNextPageDescriptor(wizard.getContext());
				if(desc == null) {
					wizard.getContext().getBackButton().setEnabled(false);
				}
				else {
					wizard.setCurrentPage(desc.getIdentifier());
				}
			}
		}
	}
}
