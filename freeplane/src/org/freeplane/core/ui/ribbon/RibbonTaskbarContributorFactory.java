package org.freeplane.core.ui.ribbon;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;

public class RibbonTaskbarContributorFactory implements IRibbonContributorFactory {

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
			private IRibbonContributor delegator = null;
			
			public String getKey() {
				return "taskbar";
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				delegator = parent;
				String key = (String) structure.getKeyByUserObject(this);
				Enumeration<?> children = structure.get(key).children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(structure, this);
				}
			}
			
			public void addChild(Object child, Object properties) {
				if(child instanceof Component) {
					if(delegator != null) {
						delegator.addChild(new RibbonTaskBarComponent((Component) child), null);
					}
				}
				
			}
		};
	}
}
