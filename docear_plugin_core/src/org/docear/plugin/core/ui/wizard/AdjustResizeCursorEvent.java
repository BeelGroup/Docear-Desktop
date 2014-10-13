package org.docear.plugin.core.ui.wizard;

import java.awt.Component;

/**
 * @author genzmehr@docear.org
 *
 */
public class AdjustResizeCursorEvent {
	private boolean consumed = false;
	private final Component component;
	private final int resizeSensor;
	
	public AdjustResizeCursorEvent(Component component, int resizeSensor) {
		this.component = component;
		this.resizeSensor = resizeSensor;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public boolean consumed() {
		return this.consumed ;
	}
	
	public void consume() {
		this.consumed = true;
	}
	
	public Component getComponent() {
		return this.component;
	}

	public int getResizeSensor() {
		return resizeSensor;
	}
	
	public String toString() {
		return this.getClass().getName() + "["+resizeSensor+";"+component.getClass().getName()+"]";
	}
}
