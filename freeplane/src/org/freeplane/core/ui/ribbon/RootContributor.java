package org.freeplane.core.ui.ribbon;

import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RootContributor extends ARibbonContributor {
	private final JRibbon ribbon; 
	
	public RootContributor(JRibbon ribbon) {
		this.ribbon = ribbon;
	}

	public String getKey() {
		return "/";
	}

	public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
		ribbon.removeAllTaskbarComponents();
		ribbon.removeAllTasks();
		context.processChildren(context.getCurrentPath(), this);
	}

	public void addChild(Object child, Object properties) {
		if(child instanceof RibbonTask) {
			this.ribbon.addTask((RibbonTask) child);
		}
		else if(child instanceof RibbonApplicationMenu) {
			this.ribbon.setApplicationMenu((RibbonApplicationMenu) child);
		}
		else if(child instanceof RibbonTaskBarComponent) {
			this.ribbon.addTaskbarComponent(((RibbonTaskBarComponent) child).getComponent());
		}
	}
}