package org.docear.plugin.services.workspace;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.event.TreeModelEvent;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.IViewportOverlay;
import org.docear.plugin.core.ui.OverlayLayoutManager;
import org.docear.plugin.core.ui.OverlayViewport;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelListener;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.view.swing.map.MapView;

public class MapViewWaringOverlay implements IViewportOverlay {
	private MouseAdapter mouseAdapter;
	private JDialog d;
	private final JLabel labelField = new JLabel();
	private WindowFocusListener focusListener;
	private final JLabel component;
	private OverlayViewport parent;

	public MapViewWaringOverlay() {
		component = new JLabel(new ImageIcon(CoreConfiguration.class.getResource("/images/dialog-warning-64x64.png")));
		component.setToolTipText(TextUtils.getRawText("docear.map.project.missing"));
		WorkspaceController.getCurrentModel().addWorldModelListener(new WorkspaceModelListener() {
			
			public void treeStructureChanged(TreeModelEvent e) {}
			
			public void treeNodesRemoved(TreeModelEvent e) {}
			
			public void treeNodesInserted(TreeModelEvent e) {}
			
			public void treeNodesChanged(TreeModelEvent e) {}
			
			public void projectRemoved(WorkspaceModelEvent event) {
				DocearController.getController().getEventQueue().invoke(new Runnable() {
					public void run() {
						updateWarningState(Controller.getCurrentController().getMap());
						parent.repaint();
					}
				});
			}
			
			public void projectAdded(WorkspaceModelEvent event) {
				DocearController.getController().getEventQueue().invoke(new Runnable() {
					public void run() {
						updateWarningState(Controller.getCurrentController().getMap());
						parent.repaint();
					}
				});
			}
		});
	}

	public boolean isVisible() {
		return component.isVisible();
	}

	public Component getComponent() {
		return component;
	}

	public void viewChanged(VIEW_CHANGE type, ContainerEvent e) {
		if(type == VIEW_CHANGE.ADD) {
			if(e.getChild() instanceof MapView) {
				e.getChild().addMouseMotionListener(getMouseAdapter());
				MapModel map = ((MapView)e.getChild()).getModel();
				updateWarningState(map);
			}
		}
		else if(type == VIEW_CHANGE.REMOVE) {
			if(e.getChild() instanceof MapView) {
				e.getChild().removeMouseMotionListener(getMouseAdapter());
			}
		}
		
	}

	public void updateWarningState(MapModel map) {
		AWorkspaceProject project = WorkspaceController.getMapProject(map);
		if(map != null && (project == null || !project.isLoaded())) {
			component.setVisible(true);
		}
		else {
			component.setVisible(false);
		}
	}
	
	private MouseAdapter getMouseAdapter() {
		if(mouseAdapter == null) {
			mouseAdapter = new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					Point origin = parent.getViewPosition();
					Point p = parent.toViewCoordinates(e.getPoint());
					p.translate(-origin.x, -origin.y);
					JComponent comp = parent.getIntersectingOverlay(p);
					if(comp != null) {
						handlePopup(comp, e);
					}
					else {
						if(d != null && d.isVisible()) {
							d.setVisible(false);
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
						d.add(labelField);
					}
					labelField.setText(comp.getToolTipText());						
					d.setLocation(e.getLocationOnScreen().x, e.getLocationOnScreen().y+24);
					d.removeWindowFocusListener(getWindowFocusListener());
					d.addWindowFocusListener(getWindowFocusListener());
					d.pack();
					d.setVisible(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(d != null) {
						d.setVisible(false);
					}
				}
				
				
			};
		}
		return mouseAdapter;
	}
	
	private WindowFocusListener getWindowFocusListener() {
		if (focusListener == null) {
			final Frame frame = UITools.getFrame();
			focusListener = new WindowFocusListener() {
				public void windowLostFocus(WindowEvent e) {
				}

				public void windowGainedFocus(WindowEvent e) {
					frame.addWindowFocusListener(new WindowFocusListener() {

						public void windowLostFocus(WindowEvent e) {
							if (d != null) {
								d.setVisible(false);
							}
						}

						public void windowGainedFocus(WindowEvent e) {
							if (d != null) {
								d.setVisible(false);
							}
							frame.removeWindowFocusListener(this);
						}
					});
					if (d != null) {
						d.removeWindowFocusListener(this);
					}
				}
			};
		}
		return focusListener;
	}

	public void setParent(OverlayViewport parent) {
		this.parent = parent;
	}

	public String[] getPositionConstraints() {
		return new String[] {OverlayLayoutManager.ALIGN_TOP, OverlayLayoutManager.FLOAT_RIGHT};
	}
}