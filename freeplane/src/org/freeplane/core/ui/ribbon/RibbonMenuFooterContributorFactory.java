package org.freeplane.core.ui.ribbon;

import java.awt.event.ActionListener;
import java.util.Properties;

import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;

public class RibbonMenuFooterContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			public String getKey() {
				return attributes.getProperty("action");
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				final String key = attributes.getProperty("action");
				if(key != null) {
					String title = RibbonActionContributorFactory.getActionTitle(getKey());
					ResizableIcon icon = RibbonActionContributorFactory.getActionIcon(getKey());
					ActionListener listener = new RibbonActionContributorFactory.RibbonActionListener(getKey());
					final RibbonApplicationMenuEntryFooter entry = new RibbonApplicationMenuEntryFooter(icon, title, listener);
					parent.addChild(entry, null);
				}
			}

			public void addChild(Object child, Object properties) {
			}
		};
	}
}
