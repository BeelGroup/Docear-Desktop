package org.freeplane.core.ui.ribbon;


public abstract class ARibbonContributor {
	public abstract String getKey();
	public abstract void contribute(RibbonBuildContext context, ARibbonContributor parent);
	public abstract void addChild(Object child, Object properties);
}