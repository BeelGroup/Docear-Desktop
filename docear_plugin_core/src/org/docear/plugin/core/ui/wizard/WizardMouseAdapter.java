package org.docear.plugin.core.ui.wizard;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * @author genzmehr@docear.org
 *
 */
public class WizardMouseAdapter extends MouseAdapter {
	
	public static final int BORDER_RIGHT = 0x1;
	public static final int BORDER_BOTTOM = 0x2;
	public static final int BORDER_LEFT = 0x4;
	public static final int BORDER_TOP = 0x8;

	private static final int RESIZE_SENSE_WIDTH = 5;
	
	private int resizeSensor = 0;
	private final Wizard wizard;
	private Point point;
	private List<ComponentDragListener> dragListeners;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WizardMouseAdapter(Wizard wizard) {
		this.wizard = wizard;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	@Override
	public void mouseDragged(MouseEvent e) {
		Point tempPoint = e.getPoint();
		SwingUtilities.convertPointToScreen(tempPoint, e.getComponent());
		if(point != null) {
			int dx = tempPoint.x-point.x;
			int dy = tempPoint.y-point.y;
			ComponentDragEvent event = new ComponentDragEvent(e.getComponent(), dx, dy, getDirectionSensor()); 
			dispatchDragEvent(event);
		}
		point = tempPoint;
		e.consume();
	}

	public void addComponentDragListener(ComponentDragListener listener) {
		if(this.dragListeners == null) {
			this.dragListeners = new ArrayList<ComponentDragListener>();
		}
		this.dragListeners.add(listener);
	}
	
	public void removeComponentDragListener(ComponentDragListener listener) {
		if(this.dragListeners != null) {
			this.dragListeners.remove(listener);
			if(this.dragListeners.size() == 0) {
				this.dragListeners = null;
			}
		}
	}

	private void dispatchDragEvent(ComponentDragEvent event) {
		if(dragListeners != null) {
			for (int i=dragListeners.size()-1; i >= 0; i--) {
				if(event.consumed()) {
					break;
				}
				dragListeners.get(i).componentDragged(event);
			}
		}
		
	}
	
	private void dispatchResizeCursorEvent(AdjustResizeCursorEvent event) {
		if(dragListeners != null) {
			for (int i=dragListeners.size()-1; i >= 0; i--) {
				if(event.consumed()) {
					break;
				}
				dragListeners.get(i).componentAdjustResizeCursor(event);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point tempPoint = e.getPoint();
		SwingUtilities.convertPointToScreen(tempPoint, e.getComponent());
		point = tempPoint;
		
		if(wizard.isResizable()) {
			resizeSensor = 0;
//			if(e.getPoint().x < RESIZE_SENSE_WIDTH) {
//				resizeSensor |= BORDER_LEFT;
//			}
//			if(e.getPoint().y < RESIZE_SENSE_WIDTH) {
//				resizeSensor |= BORDER_TOP;
//			}
			if(e.getPoint().x > (e.getComponent().getWidth()-RESIZE_SENSE_WIDTH)) {
				resizeSensor |= BORDER_RIGHT;
			}
			if(e.getPoint().y > (e.getComponent().getHeight()-RESIZE_SENSE_WIDTH)) {
				resizeSensor |= BORDER_BOTTOM;
			}
			
			dispatchResizeCursorEvent(new AdjustResizeCursorEvent(e.getComponent(), resizeSensor));
			e.consume();
		}
	}
	
	

	private int getDirectionSensor() {
		int sensor = 0;
		if((resizeSensor & (BORDER_TOP | BORDER_BOTTOM)) > 0) {
			sensor |= ComponentDragEvent.DIRECTION_VERTICAL;
		}
		if((resizeSensor & (BORDER_LEFT | BORDER_RIGHT)) > 0) {
			sensor |= ComponentDragEvent.DIRECTION_HORIZONTAL;
		}
		return sensor;
	}
	
	public boolean isResizeArea() {
		return resizeSensor > 0;
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
