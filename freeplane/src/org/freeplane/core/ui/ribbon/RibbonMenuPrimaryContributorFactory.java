package org.freeplane.core.ui.ribbon;

import java.awt.Dimension;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
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

	public static RibbonApplicationMenuEntryPrimary createMenuEntry(final String key, CommandButtonKind kind) {
		String title = TextUtils.getText(key+".text");
		if(title == null || title.isEmpty()) {
			title = key;
		}
		String resource = ResourceController.getResourceController().getProperty(key+".icon", null);
		ResizableIcon icon = null;
		if (resource != null) {
			URL location = ResourceController.getResourceController().getResource(resource);
			icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(16, 16));
		}
		
		RibbonApplicationMenuEntryPrimary entry = new RibbonApplicationMenuEntryPrimary(icon, title, new RibbonActionContributorFactory.RibbonActionListener(key), kind);
		
		return entry;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			RibbonApplicationMenuEntryPrimary entry;
			public String getKey() {
				String key = attributes.getProperty("action", null);
				if(key == null) {
					key = attributes.getProperty("name", null);
				}
				return key;
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				entry = null;
				String key = (String) structure.getKeyByUserObject(this);
				if(key != null) {
					Node n = (Node) structure.get(key);
					if(n.getChildCount() > 0) {
						entry = createMenuEntry(getKey(), (attributes.get("action") == null ? CommandButtonKind.POPUP_ONLY : CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION));
						Enumeration<?> children = n.children();
						while(children.hasMoreElements()) {
							IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
							((IRibbonContributor)node.getUserObject()).contribute(structure, this);
						}
					}
					else {
						entry = createMenuEntry(getKey(), CommandButtonKind.ACTION_ONLY);
					}
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
