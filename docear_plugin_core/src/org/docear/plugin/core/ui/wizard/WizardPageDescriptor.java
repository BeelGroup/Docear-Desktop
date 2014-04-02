package org.docear.plugin.core.ui.wizard;

import org.freeplane.core.ui.components.IKeyBindingManager;



public class WizardPageDescriptor {

	private final AWizardPage targetPage;
	private final Object pageIdentifier;
	private IPageKeyBindingProcessor keyBindingProcessor;

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
	
	public WizardPageDescriptor getSkipPageDescriptor(WizardContext context) {
		return null;
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

	public IPageKeyBindingProcessor getKeyBindingProcessor() {
		return this.keyBindingProcessor;
	}
	
	public void setKeyBindingProcessor(IPageKeyBindingProcessor proc) {
		this.keyBindingProcessor = proc;
	}
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
