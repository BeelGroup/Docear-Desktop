package org.docear.plugin.core.ui.wizard;


/**
 * @author genzmehr@docear.org
 *
 */
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

	public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
		return null;
	}

	public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
		return context.getTraversalLog().getPreviousPage(context);
	}
	
	public WizardPageDescriptor getSkipPageDescriptor(WizardSession context) {
		return null;
	}

	public void aboutToDisplayPage(WizardSession context) {
		getPage().preparePage(context);
	}

	public void displayingPage(WizardSession context) {
		getPage().setVisible(true);
	}

	public void aboutToHidePage(WizardSession context) {
		getPage().setVisible(false);
	}

	public IPageKeyBindingProcessor getKeyBindingProcessor() {
		return this.keyBindingProcessor;
	}
	
	public void setKeyBindingProcessor(IPageKeyBindingProcessor proc) {
		this.keyBindingProcessor = proc;
	}

	public boolean resizeWizard() {		
		return getPage().forceResize();
	}
	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
