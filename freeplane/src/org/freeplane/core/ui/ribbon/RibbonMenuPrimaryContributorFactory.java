package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class RibbonMenuPrimaryContributorFactory implements IRibbonContributorFactory {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static RibbonApplicationMenuEntryPrimary createMenuEntry(final AFreeplaneAction action, CommandButtonKind kind) {
		String title = RibbonActionContributorFactory.getActionTitle(action);
		ResizableIcon icon = RibbonActionContributorFactory.getActionIcon(action);

		RibbonApplicationMenuEntryPrimary entry = new RibbonApplicationMenuEntryPrimary(icon, title, new RibbonActionContributorFactory.RibbonActionListener(action), kind);
		return entry;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			RibbonApplicationMenuEntryPrimary entry;
			public String getKey() {
				String key = attributes.getProperty("action", null);
				if(key == null) {
					key = attributes.getProperty("name", null);
				}
				return key;
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				entry = null;
				Node n = context.getStructureNode(this);
				if(n.getChildCount() > 0) {
					
					if(attributes.get("action") == null) {
						AFreeplaneAction action = RibbonActionContributorFactory.getDummyAction(getKey());
						entry = createMenuEntry(action, CommandButtonKind.POPUP_ONLY);
					}
					else {
						AFreeplaneAction action = context.getBuilder().getMode().getAction(getKey());
						if(action == null) {
							action = RibbonActionContributorFactory.getDummyAction(getKey());
						}
						entry = createMenuEntry(action, CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
					}
					
					Enumeration<?> children = n.children();
					while(children.hasMoreElements()) {
						IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
						((ARibbonContributor)node.getUserObject()).contribute(context, this);
					}
				}
				else {
					AFreeplaneAction action = RibbonActionContributorFactory.getDummyAction(getKey());
					entry = createMenuEntry(action, CommandButtonKind.ACTION_ONLY);
				}
				parent.addChild(entry, null);
			}

			public void addChild(Object child, Object properties) {
				if(child instanceof SecondaryEntryGroup) {
					SecondaryEntryGroup group = (SecondaryEntryGroup) child;
					entry.addSecondaryMenuGroup(group.getTitle(), group.getEntries().toArray(new RibbonApplicationMenuEntrySecondary[0]));
				}
			}
		};
	}
	
	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
	
	public static class SecondaryEntryGroup {
		private final String groupTitle;
		private List<RibbonApplicationMenuEntrySecondary> entries = new ArrayList<RibbonApplicationMenuEntrySecondary>();
		
		public SecondaryEntryGroup(String title) {
			this.groupTitle = title;
		}
		
		public void addEntry(RibbonApplicationMenuEntrySecondary entry) {
			entries.add(entry);
		}
		
		public List<RibbonApplicationMenuEntrySecondary> getEntries() {
			return Collections.unmodifiableList(entries);
		}
		
		public String getTitle() {
			return groupTitle;
		}
	}
}
