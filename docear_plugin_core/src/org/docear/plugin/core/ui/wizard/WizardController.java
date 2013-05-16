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
		
	}
}
