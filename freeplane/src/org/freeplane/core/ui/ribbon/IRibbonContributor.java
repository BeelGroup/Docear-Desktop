package org.freeplane.core.ui.ribbon;


public interface IRibbonContributor {
	public String getKey();
	public void contribute(RibbonBuildContext context, IRibbonContributor parent);
	public void addChild(Object child, Object properties);
}