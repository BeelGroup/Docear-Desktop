package org.docear.plugin.bibtex.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.dialogs.MetaDataExtractorPage;
import org.docear.plugin.bibtex.dialogs.MetaDataOptionsPage;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.freeplane.core.ui.components.UITools;

public final class MetaDataAction {
	
	public static MetaDataActionObject showDialog(MetaDataActionObject result) {		
		if(result.getResult().size() <= 0) return result;		
		
		final Wizard wiz = new Wizard(UITools.getFrame());
		wiz.setResizable(true);
		wiz.getSession().set(result.getClass(), result);
				
		WizardPageDescriptor metadataDescriptor = new WizardPageDescriptor("metadata", new MetaDataExtractorPage()) {

			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				MetaDataActionObject data = context.get(MetaDataActionObject.class);
				while(data.getResultIterator().hasNext()){
					data.setCurrentPDF(data.getResultIterator().next());
					if(data.getResult().get(data.getCurrentPDF()).isDuplicatePdf()){
						MetaDataDuplicatePage.showDuplicateMessage(context);
					}
					else{
						return context.getModel().getPage("metadata");
					}
				}
				return Wizard.FINISH_PAGE;				
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				wiz.cancel();
				return Wizard.FINISH_PAGE;
			}		
			
		};	
		
		WizardPageDescriptor optionsDescriptor = new WizardPageDescriptor("metadataOptions", new MetaDataOptionsPage()) {
			
			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				return context.getModel().getPage("metadata");
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {				
				return context.getModel().getPage("metadata");
			}
		};
					
		
		wiz.registerWizardPanel(metadataDescriptor);
		wiz.registerWizardPanel(optionsDescriptor);		
		
		MetaDataActionObject data = wiz.getSession().get(MetaDataActionObject.class);
		while(data.getResultIterator().hasNext()){
			data.setCurrentPDF(data.getResultIterator().next());
			if(data.getResult().get(data.getCurrentPDF()).isDuplicatePdf()){
				MetaDataDuplicatePage.showDuplicateMessage(wiz.getSession());
			}
			else{
				wiz.setStartPage(metadataDescriptor.getIdentifier());
				wiz.show();
				break;
			}
		}				
		return wiz.getSession().get(MetaDataActionObject.class);
	}

	public static class MetaDataActionObject{
		
		private HashMap<URI, MetaDataActionResult> result = new HashMap<URI, MetaDataActionResult>();
		private ArrayList<String> unhandledFiles = new ArrayList<String>();
		private Iterator<URI> iterator;
		private URI currentPDF;
				
		public ArrayList<String> getUnhandledFiles() {
			return unhandledFiles;
		}
		
		public HashMap<URI, MetaDataActionResult> getResult() {
			return result;
		}
		
		public Iterator<URI> getResultIterator(){
			if(this.iterator == null){
				this.iterator = result.keySet().iterator();
			}			
			return this.iterator;
		}

		public URI getCurrentPDF() {
			if(currentPDF == null){
				currentPDF = this.getResultIterator().next();
			}
			return currentPDF;
		}

		public void setCurrentPDF(URI currentPDF) {
			this.currentPDF = currentPDF;
		}
		
	}
	
	public static class MetaDataActionResult {
		
		private BibtexEntry entryToUpdate;
		private boolean duplicatePdf;
		private boolean showattachOnlyOption;
		
		private BibtexEntry resultEntry;
		private boolean selectedBlank;
		private boolean selectedFetched;
		private boolean selectedXmp;
		private boolean attachOnly;
		private boolean selectedCancel;
		public BibtexEntry getEntryToUpdate() {
			return entryToUpdate;
		}
		public void setEntryToUpdate(BibtexEntry entryToUpdate) {
			this.entryToUpdate = entryToUpdate;
		}
		public boolean isDuplicatePdf() {
			return duplicatePdf;
		}
		public void setDuplicatePdf(boolean duplicatePdf) {
			this.duplicatePdf = duplicatePdf;
		}
		public boolean isShowattachOnlyOption() {
			return showattachOnlyOption;
		}
		public void setShowattachOnlyOption(boolean showattachOnlyOption) {
			this.showattachOnlyOption = showattachOnlyOption;
		}
		public BibtexEntry getResultEntry() {
			return resultEntry;
		}
		public void setResultEntry(BibtexEntry resultEntry) {
			this.resultEntry = resultEntry;
		}
		public boolean isSelectedBlank() {
			return selectedBlank;
		}
		public void setSelectedBlank(boolean selectedBlank) {
			this.selectedBlank = selectedBlank;
		}
		public boolean isSelectedFetched() {
			return selectedFetched;
		}
		public void setSelectedFetched(boolean selectedFetched) {
			this.selectedFetched = selectedFetched;
		}
		public boolean isSelectedXmp() {
			return selectedXmp;
		}
		public void setSelectedXmp(boolean selectedXmp) {
			this.selectedXmp = selectedXmp;
		}
		public boolean isAttachOnly() {
			return attachOnly;
		}
		public void setAttachOnly(boolean attachOnly) {
			this.attachOnly = attachOnly;
		}
		public boolean isSelectedCancel() {
			return selectedCancel;
		}
		public void setSelectedCancel(boolean selectedCancel) {
			this.selectedCancel = selectedCancel;
		}		
		
	}

}
