package org.docear.plugin.core.ui.wizard;

import javax.swing.JPanel;

public abstract class AWizardPage extends JPanel {
	private static final long serialVersionUID = 1L;
	private boolean displayable = true;
	private boolean skipOnBack = false;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setPageDisplayable(boolean enabled) {
		this.displayable  = enabled;
	}
	
	public boolean isPageDisplayable() {
		return this.displayable;
	}
	
	public void setSkipOnBack(boolean enabled) {
		this.skipOnBack   = enabled;
	}
	
	public boolean skipOnBack() {
		return this.skipOnBack;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public abstract String getTitle();
	
	public abstract void preparePage(WizardContext context);
}
