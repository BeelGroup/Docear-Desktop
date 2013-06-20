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
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				menu = new RibbonApplicationMenu();
				String key = (String) structure.getKeyByUserObject(this);
				if(key != null) {
					Enumeration<?> children = structure.get(key).children();
					while(children.hasMoreElements()) {
						IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
						((IRibbonContributor)node.getUserObject()).contribute(structure, this);
					}
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
