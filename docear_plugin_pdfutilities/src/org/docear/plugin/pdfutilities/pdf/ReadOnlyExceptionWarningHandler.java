package org.docear.plugin.pdfutilities.pdf;

import java.awt.Dimension;
import java.io.File;

import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.pdfutilities.ui.ReadOnlyDocumentExceptionPage;
import org.freeplane.core.ui.components.UITools;

import de.intarsys.tools.locator.FileLocator;

public class ReadOnlyExceptionWarningHandler {
	
	public static enum DIALOG_OPTIONS {
		RETRY, SKIP, SKIP_ALL
	}

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void showDialog(File file) {
		prepare();
		Wizard wizard = new Wizard(UITools.getFrame());
		wizard.setCancelEnabled(false);
		
		wizard.getSession().set(FileLocator.class, new FileLocator(file));
		
		ReadOnlyDocumentExceptionPage page = new ReadOnlyDocumentExceptionPage();
		page.setPreferredSize(new Dimension(480,160));
		WizardPageDescriptor descriptor = new WizardPageDescriptor("docear.pdf.readonly.warning", page) {
			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				context.set(DIALOG_OPTIONS.class, DIALOG_OPTIONS.RETRY);
				return Wizard.FINISH_PAGE;
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				context.set(DIALOG_OPTIONS.class, DIALOG_OPTIONS.SKIP_ALL);
				return Wizard.FINISH_PAGE;
			}
			
			@Override
			public WizardPageDescriptor getSkipPageDescriptor(WizardSession context) {
				context.set(DIALOG_OPTIONS.class, DIALOG_OPTIONS.SKIP);
				return Wizard.FINISH_PAGE;
			}
			
		};
		
		wizard.registerWizardPanel(descriptor);
		wizard.setStartPage(descriptor.getIdentifier());
		int option = wizard.show();
		if(option == Wizard.OK_OPTION) {
			DIALOG_OPTIONS opt = wizard.getSession().get(DIALOG_OPTIONS.class);
			switch(opt) {
			case RETRY: retry = true; skip = false; break;
			case SKIP: retry = false; skip = true; break;
			case SKIP_ALL: retry = false; skipAll = true; break;
			}
		}
		
	}
	private boolean skipAll = false;
	private boolean skip = false;
	private boolean retry = true;
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public boolean retry() {
		return retry;
	}
	public boolean skip() {
		return skipAll || skip;
	}
	public void consume() {
		retry = false;
		
	}
	public void prepare() {
		skip = false;
		retry = true;
	}
}
