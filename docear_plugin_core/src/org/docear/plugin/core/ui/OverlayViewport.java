package org.docear.plugin.core.ui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.ui.IViewportOverlay.VIEW_CHANGE;

public class OverlayViewport extends JViewport {

	private static final long serialVersionUID = 1L;
	private final OverlayLayoutManager layoutManager;
	private final List<IViewportOverlay> overlayComponents = new ArrayList<IViewportOverlay>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public OverlayViewport(JViewport viewport) {
		this.layoutManager = new OverlayLayoutManager(viewport.getLayout());
		super.setLayout(this.layoutManager);
		super.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						paintOverlays(getGraphics());
					}
				});
			}
		});
		super.addContainerListener(new ContainerListener() {
			
			@Override
			public void componentRemoved(ContainerEvent e) {
				dispatchChangeEvent(VIEW_CHANGE.REMOVE, e);
			}
			
			@Override
			public void componentAdded(ContainerEvent e) {
				dispatchChangeEvent(VIEW_CHANGE.ADD,e);
			}
		});
	}

	protected void dispatchChangeEvent(VIEW_CHANGE changeType, ContainerEvent e) {
		synchronized (overlayComponents) {
			for (IViewportOverlay overlay : overlayComponents) {
				overlay.viewChanged(changeType, e);
			}
		}
	}

	public void enableOverlays(boolean b) {
		synchronized (overlayComponents) {
			for (IViewportOverlay overlay : overlayComponents) {
				Component comp = overlay.getComponent();
				comp.setVisible(b);
			}
		}
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void addOverlay(IViewportOverlay overlay) {
		synchronized (overlayComponents) {
			this.overlayComponents.add(overlay);
			overlay.setParent(this);
			Component comp = overlay.getComponent();
			if(comp == null) {
				throw new RuntimeException("IViewportOverlay.getComponent() must not return NULL");
			}
			this.layoutManager.addLayoutComponent(overlay.getPositionConstraints(), comp);
			fireStateChanged();
		}
	}
	
	public void removeOverlay(IViewportOverlay overlay) {
		synchronized (overlayComponents) {
			this.overlayComponents.remove(overlay);
			overlay.setParent(null);
			Component comp = overlay.getComponent();
			if(comp == null) {
				throw new RuntimeException("IViewportOverlay.getComponent() must not return NULL");
			}
			this.layoutManager.removeLayoutComponent(comp);
			fireStateChanged();
		}
	}
	
	public IViewportOverlay[] getOverlays() {
		synchronized (overlayComponents) {
			return overlayComponents.toArray(new IViewportOverlay[0]);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}

	public JComponent getIntersectingOverlay(Point point) {
		synchronized (overlayComponents) {
			for (IViewportOverlay overlay : overlayComponents) {
				Component comp = overlay.getComponent();
				if(comp.getBounds().contains(point) && comp.isVisible()) {
					return (JComponent) comp;
				}
			}
		}
		return null;
	}

	private void paintOverlays(Graphics gr) {
		if (this.isShowing()) {
			prepareAreas();
			
			Graphics2D g = (Graphics2D) getGraphics();
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5F);
			g.setComposite(ac);
			
			int width = getWidth();
			int height = getHeight();
	
			if ((width <= 0) || (height <= 0)) {
				return;
			}
				
				
			for (IViewportOverlay overlay : getOverlays()) {
				Component comp = overlay.getComponent();
				if(comp.isVisible()) {
					g.translate(comp.getX(), comp.getY());
					comp.paint(g);
				}
			}
		}
	}
	
	private void prepareAreas() {
		synchronized (overlayComponents) {
			
		}
		
	}

	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		paintOverlays(g);
	}
	
	public void setLayout(LayoutManager layout) {
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
