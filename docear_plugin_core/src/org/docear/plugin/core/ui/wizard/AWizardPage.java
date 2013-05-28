package org.docear.plugin.core.ui.wizard;

import javax.swing.JPanel;

public abstract class AWizardPage extends JPanel {
	private static final long serialVersionUID = 1L;
	private boolean invisibleExec = false;
	private boolean skipOnBack = false;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setPageInvisible(boolean enabled) {
		this.invisibleExec  = enabled;
	}
	
	public boolean isInvisiblePage() {
		return this.invisibleExec;
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
