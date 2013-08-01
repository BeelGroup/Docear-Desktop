package org.docear.plugin.core.ui;

import java.awt.Component;
import java.awt.event.ContainerEvent;

public interface IViewportOverlay {
	public enum VIEW_CHANGE {
		ADD, REMOVE
	};
	public void setParent(OverlayViewport parent);
	public Component getComponent();
	public boolean isVisible();
	public void viewChanged(VIEW_CHANGE type, ContainerEvent event);
	public String[] getPositionConstraints();

}
