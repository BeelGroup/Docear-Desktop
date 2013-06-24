package org.freeplane.core.ui.ribbon;

import java.util.Enumeration;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;

public class RibbonMenuContributorFactory implements IRibbonContributorFactory {

	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			private RibbonApplicationMenu menu;

			public String getKey() {
				return "app_menu";
			}
			
			public void contribute(RibbonBuildContext context, IRibbonContributor parent) {
				menu = new RibbonApplicationMenu();
				Enumeration<?> children = context.getStructureNode(this).children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(context, this);
				}				
				parent.addChild(menu, null);
			}

			public void addChild(Object child, Object properties) {
				if(child instanceof RibbonApplicationMenuEntryFooter) {
					menu.addFooterEntry((RibbonApplicationMenuEntryFooter) child);
				}
				else if(child instanceof RibbonApplicationMenuEntryPrimary) {
					menu.addMenuEntry((RibbonApplicationMenuEntryPrimary) child);
				}
			}
		};
	}

}
