package org.freeplane.core.ui.ribbon;

import org.freeplane.core.ui.IndexedTree;

public interface IRibbonContributor {
	public String getKey();
	public void contribute(IndexedTree structure, IRibbonContributor parent);
	public void addChild(Object child, Object properties);
}