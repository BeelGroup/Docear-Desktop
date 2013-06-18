package org.freeplane.core.ui.ribbon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;

public class RibbonActionContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			
			public String getKey() {
				return attributes.getProperty("name");
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				final String key = attributes.getProperty("action");
				if(key != null) {
					String title = TextUtils.getRawText(key+".text");
					if(title == null || title.isEmpty()) {
						title = key;
					}
					final AbstractCommandButton button = new JCommandButton(title, null);
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
							if(action == null) {
								return;
							}
							action.actionPerformed(e);
						}
					});
					parent.addChild(button);
				}
			}
			
			public void addChild(Object child) {
			}
		};
	}
}
