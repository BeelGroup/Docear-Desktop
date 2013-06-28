package org.docear.plugin.core.ui.ribbons;

import java.util.Properties;

import org.docear.plugin.core.actions.DocearSetNodePrivacyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class DocearNodePrivacyContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	@Override
	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			
			@Override
			public String getKey() {
				return attributes.getProperty("name");
			}
			
			@Override
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if("true".equals(System.getProperty("docear.debug", "false"))) {
					AFreeplaneAction action = context.getBuilder().getMode().getAction(DocearSetNodePrivacyAction.KEY);
					if(action != null) {
						JCommandButton button = RibbonActionContributorFactory.createCommandButton(action);
						RibbonActionContributorFactory.updateRichTooltip(button, action, context.getBuilder().getAcceleratorManager().getAccelerator(action.getKey()));
						ChildProperties childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
						childProps.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
						parent.addChild(button, childProps);
					}
				}
			}
			
			@Override
			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
}
