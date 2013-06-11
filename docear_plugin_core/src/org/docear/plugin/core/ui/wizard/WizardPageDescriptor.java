package org.docear.plugin.core.ui.wizard;



public class WizardPageDescriptor {

	private final AWizardPage targetPage;
	private final Object pageIdentifier;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WizardPageDescriptor(Object identifier, AWizardPage page) {
		this.targetPage = page;
		this.pageIdentifier = identifier;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public final AWizardPage getPage() {
		return targetPage;
	}

	public final Object getIdentifier() {
		return pageIdentifier;
	}

	public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
		return null;
	}

	public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {
		return context.getTraversalLog().getPreviousPage(context);
	}

	public void aboutToDisplayPage(WizardContext context) {
		getPage().preparePage(context);
	}

	public void displayingPage(WizardContext context) {
		getPage().setVisible(true);
	}

	public void aboutToHidePage(WizardContext context) {
		getPage().setVisible(false);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
