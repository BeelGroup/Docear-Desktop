package org.docear.plugin.core.ui.wizard;

import java.awt.Component;

/**
 * @author genzmehr@docear.org
 *
 */
public class ComponentDragEvent {
	
	public static final int DIRECTION_HORIZONTAL = 0x1;
	public static final int DIRECTION_VERTICAL = 0x2;

	private boolean consumed = false;
	private final Component component;
	private final int resizeSensor;
	public final int deltaX;
	public final int deltaY;
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public ComponentDragEvent(Component component, int dx, int dy, int resizeSensor) {
		this.component = component;
		this.deltaX = dx;
		this.deltaY = dy;
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

	public boolean isResizeEvent() {
		return resizeSensor > 0;
	}
	
	public boolean resizeDirection(int dir) {
		dir = dir & ( DIRECTION_HORIZONTAL | DIRECTION_VERTICAL);
		return (resizeSensor & dir) > 0;
	}
	
	public String toString() {
		return this.getClass().getName() + "["+deltaX+";"+deltaY+";"+resizeSensor+";"+component.getClass().getName()+"]";
	}
}