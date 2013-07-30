package org.docear.plugin.core.ui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.view.swing.map.MapView;

public class OverlayViewport extends JViewport {

	private static final long serialVersionUID = 1L;
	private final OverlayLayoutManager layoutManager;
	private final List<Component> overlayComponents = new ArrayList<Component>();
	private MouseAdapter mouseAdapter;
	private JDialog d;
	
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
				if(e.getChild() instanceof MapView) {
					e.getChild().removeMouseMotionListener(getMouseAdapter());
				}
			}
			
			@Override
			public void componentAdded(ContainerEvent e) {
				if(e.getChild() instanceof MapView) {
					e.getChild().addMouseMotionListener(getMouseAdapter());
					AWorkspaceProject project = WorkspaceController.getMapProject(((MapView)e.getChild()).getModel());
					if(project == null || !project.isLoaded()) {
						enableOverlay(true);
					}
					else {
						enableOverlay(false);
					}
				}
			}
		});
		super.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				if(d != null) {
					d.setVisible(false);
				}
			}
			
		});
	}

	protected void enableOverlay(boolean b) {
		synchronized (overlayComponents) {
			for (Component comp : overlayComponents) {
				comp.setVisible(b);
			}
		}
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void addOverlay(Component comp, Object contraints) {
		synchronized (overlayComponents) {
			this.overlayComponents.add(comp);
			this.layoutManager.addLayoutComponent(String.valueOf(contraints), comp);
			comp.addMouseListener(getMouseAdapter());
			fireStateChanged();
		}
	}
	
	public void removeOverlay(Component comp) {
		synchronized (overlayComponents) {
			this.overlayComponents.remove(comp);
			this.layoutManager.removeLayoutComponent(comp);
			comp.removeMouseListener(getMouseAdapter());
			fireStateChanged();
		}
	}
	
	public Component[] getOverlays() {
		synchronized (overlayComponents) {
			return overlayComponents.toArray(new Component[0]);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}
	
	private MouseAdapter getMouseAdapter() {
		if(mouseAdapter == null) {
			mouseAdapter = new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					Point origin = getViewPosition();
					Point p = toViewCoordinates(e.getPoint());
					p.translate(-origin.x, -origin.y);
					JComponent comp = getIntersectingOverlay(p);
					if(comp != null) {
						handlePopup(comp, e);
					}
					else {
						if(d != null && d.isVisible()) {
							d.setVisible(false);
							d = null;
						}
					}
				}

				private void handlePopup(JComponent comp, MouseEvent e) {
					if(d == null) {
						final Frame frame = UITools.getFrame();
						d = new JDialog(frame, comp.getName());
						d.setUndecorated(true);
						d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						d.setModal(false);
						d.add(new JLabel(comp.getToolTipText()));
						d.pack();
						d.addWindowFocusListener(new WindowFocusListener() {
							public void windowLostFocus(WindowEvent e) {
							}
	
							public void windowGainedFocus(WindowEvent e) {
								frame.addWindowFocusListener(new WindowFocusListener() {
									public void windowLostFocus(WindowEvent e) {
										if(d != null) {
											d.setVisible(false);
										}
									}
	
									public void windowGainedFocus(WindowEvent e) {
										if(d != null) {
											d.setVisible(false);
										}
										frame.removeWindowFocusListener(this);
									}
								});
								if(d != null) {
									d.removeWindowFocusListener(this);
								}
							}
						});
						
					}
					d.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y+24);
					d.setVisible(true);
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(d != null) {
						d.setVisible(false);
						d = null;
					}
				}
				
				
			};
		}
		return mouseAdapter;
	}

	protected JComponent getIntersectingOverlay(Point point) {
		synchronized (overlayComponents) {
			for (Component comp : overlayComponents) {
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
				
				
			for (Component comp : getOverlays()) {
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
