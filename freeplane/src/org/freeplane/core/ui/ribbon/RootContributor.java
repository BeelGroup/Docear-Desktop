package org.freeplane.core.ui.ribbon;

import java.util.Enumeration;

import org.freeplane.core.ui.IndexedTree;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RootContributor implements IRibbonContributor {
	private final JRibbon ribbon; 
	
	public RootContributor(JRibbon ribbon) {
		this.ribbon = ribbon;
	}

	public String getKey() {
		return "/";
	}

	public void contribute(IndexedTree structure, IRibbonContributor parent) {
		ribbon.removeAllTaskbarComponents();
		ribbon.removeAllTasks();
		Enumeration<?> children = structure.getRoot().children();
		while(children.hasMoreElements()) {
			IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
			((IRibbonContributor)node.getUserObject()).contribute(structure, this);
		}

	}

	public void addChild(Object child, Object properties) {
		if(child instanceof RibbonTask) {
			this.ribbon.addTask((RibbonTask) child);
		}
		else if(child instanceof RibbonApplicationMenu) {
			this.ribbon.setApplicationMenu((RibbonApplicationMenu) child);
		}

	}

}