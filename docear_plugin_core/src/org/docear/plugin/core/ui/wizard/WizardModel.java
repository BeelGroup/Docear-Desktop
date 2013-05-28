package org.docear.plugin.core.ui.wizard;

import java.util.HashMap;
import java.util.Map;

public class WizardModel {
	private Map<Object, WizardPageDescriptor> pages = new HashMap<Object, WizardPageDescriptor>();
	private Object currenrId = null;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setCurrentPage(Object id) {
		if(pages.containsKey(id)) {
			this.currenrId = id;
		}
	}
	
	public WizardPageDescriptor getCurrentPageDescriptor() {
		if(this.currenrId != null) {
			return pages.get(currenrId);
		}
		return null;
	}

	public void registerPage(Object id, WizardPageDescriptor panel) {
		if(pages.containsKey(id)) {
			throw new RuntimeException("id already used");
		}
		pages.put(id, panel);
	}
	
	public WizardPageDescriptor getPage(Object id) {
		return pages.get(id);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
