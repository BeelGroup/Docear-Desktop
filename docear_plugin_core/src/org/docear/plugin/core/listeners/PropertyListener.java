package org.docear.plugin.core.listeners;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.ui.LinkTypeChangedPage;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.ui.components.UITools;

public class PropertyListener implements IFreeplanePropertyListener {

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		// we should not update any links as long as we can't update maps over all projects (currently maps of other projects are ignored)
		// but we should show a message to the user with a link to the post
		if (propertyName.equals("links") && (!newValue.equals(oldValue))) {
			Wizard wizard = new Wizard(UITools.getFrame());			
			initWizard(wizard);
			
			wizard.show();			
		}
		if (propertyName.equals("links") && (!newValue.equals(oldValue))) {
			if(SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(new Runnable() {					
					public void run() {
						doUpdateLinks();						
					}
				});								
			}
			else {
				doUpdateLinks();
			}
		}
	}
	
	private void initWizard(Wizard wizard) {
		WizardPageDescriptor desc = new WizardPageDescriptor("page.linktype_changed", new LinkTypeChangedPage()) {
			
		};
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		
	}

	private final void doUpdateLinks() {
//		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
//		mindmapUpdateController.addMindmapUpdater(new MindmapLinkTypeUpdater(TextUtils.getText("updating_link_types")));
//		mindmapUpdateController.updateRegisteredMindmapsInWorkspace(true);
	}

}
