package org.docear.plugin.services.recommendations;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.FreeplaneMain;
import org.freeplane.view.swing.map.MapView;

//DOCEAR - todo: close does not work
public class RecommendationTabListener implements ChangeListener, ContainerListener, MouseListener, RecommendationsViewListener {
		private JTabbedPane mTabbedPane;
		private int lastRecommTabIndex;
		private RecommendationsView view;

		public RecommendationTabListener(JTabbedPane tabPane, RecommendationsView view) {
			this.mTabbedPane = tabPane;
			this.view = view;
			tabPane.addContainerListener(this);
			tabPane.addChangeListener(this);
			tabPane.addMouseListener(this);
			view.addViewChangeListener(this);
			
		}

		public void stateChanged(ChangeEvent event) {
			//consume event
			int selectedIndex = mTabbedPane.getSelectedIndex();
			Component comp = mTabbedPane.getSelectedComponent();
			if(comp instanceof RecommendationsView || lastRecommTabIndex == selectedIndex) {
				lastRecommTabIndex = selectedIndex;
				mTabbedPane.putClientProperty("ChangedEventConsumed", "true");
				mTabbedPane.setComponentAt(selectedIndex, view);
			}
			
		}

		public void componentAdded(ContainerEvent e) {
			if(e.getChild() instanceof RecommendationsView) {
				((RecommendationsView) e.getChild()).setCloseListener(this);
				mTabbedPane.setSelectedIndex(mTabbedPane.getTabCount()-1);
			}
			
		}

		public void componentRemoved(ContainerEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		protected void handlePopup(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				Component popup = buildPopupMenu();
	            Component component = e.getComponent();
	            
//	            final Component popupForModel;
//	            Component target = mTabbedPane.getComponentAt(e.getPoint()); 
//	            
//	            if(target instanceof RecommendationsView) {
//	            	
//	            }
//	            else {
//		            final MapView mapView = (MapView) Controller.getCurrentController().getViewController().getMapView();
//					final ModeController modeController = Controller.getCurrentController().getModeController();
//					if(mapView != null){
//						final java.lang.Object obj = mapView.detectCollision(e.getPoint());
//						popupForModel= LinkController.getController(modeController).getPopupForModel(obj);
//					}
//					else{
//						popupForModel = null;
//					}
//					if (popupForModel != null) {
//						final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
//						popupForModel.addHierarchyListener(popupListener);
//						popup = popupForModel;
//					}
//					else {
//						popup = modeController.getUserInputListenerFactory().getMapPopup();
//					}
//	            }
				
				if(popup instanceof JPopupMenu) {
	                ((JPopupMenu)popup).show(component, e.getX(), e.getY());
	            }
				else {
				    Point locationOnScreen = component.getLocationOnScreen();
				    final Component window;
				    if(popup instanceof Window){
				        window= popup;
				    }
				    else{
	                    final Frame frame = UITools.getFrame();
	                    final JDialog d = new JDialog(frame, popup.getName());
	                    d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	                    d.setModal(false);
	                    d.add(popup);
	                    d.pack();
	                    d.addWindowFocusListener(new WindowFocusListener() {
	                        public void windowLostFocus(WindowEvent e) {
	                        }
	                        
	                        public void windowGainedFocus(WindowEvent e) {
	                            frame.addWindowFocusListener(new WindowFocusListener() {
	                                public void windowLostFocus(WindowEvent e) {
	                                }
	                                
	                                public void windowGainedFocus(WindowEvent e) {
	                                    d.setVisible(false);
	                                    frame.removeWindowFocusListener(this);
	                                }
	                            });
	                            d.removeWindowFocusListener(this);
	                        }
	                    });
				        window = d;
				    }
				    window.setLocation(locationOnScreen.x+e.getX(), locationOnScreen.y + e.getY());
				    window.setVisible(true);
				}
				
			}
		}

		private Component buildPopupMenu() {
			JPopupMenu popup = new JPopupMenu("RecommendationsMenu");
			JMenuItem closeItem = new JMenuItem(TextUtils.getText("recommendations.close.title"), new ImageIcon(FreeplaneMain.class.getResource("/images/close.png")));
			closeItem.addActionListener(new ActionListener() {			
				public void actionPerformed(ActionEvent e) {
					closeView();
				}
			});
			popup.add(closeItem);
			return popup;
		}

		public void mouseClicked(final MouseEvent e) {
			final Object source = e.getSource();
			if(! (source instanceof MapView))
				return;
			final MapView map = (MapView) source;
			final Controller controller = map.getModeController().getController();
			final IMapSelection selection = controller.getSelection();
			if(selection != null){
				final NodeModel selected = selection.getSelected();
				if(selected != null)
					controller.getMapViewManager().getComponent(selected).requestFocusInWindow();
			}
			
		}

		public void mouseEntered(final MouseEvent e) {
		}

		public void mouseExited(final MouseEvent e) {
		}

		public void mouseMoved(final MouseEvent e) {
		}

		public void mousePressed(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				handlePopup(e);
			}
			else if (e.getButton() == MouseEvent.BUTTON1){
				
			}
			e.consume();
		}

		public void mouseReleased(final MouseEvent e) {
			handlePopup(e);
			e.consume();
		}
		
		public void mouseDragged(final MouseEvent e) {
//			final Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
//			final JComponent component = (JComponent) e.getComponent();
//			final MapView mapView = getMapView(component);
//			if(mapView == null)
//				return;
//			final boolean isEventPointVisible = component.getVisibleRect().contains(r);
//			if (!isEventPointVisible) {
//				component.scrollRectToVisible(r);
//			}
//			if (originX >= 0 && isEventPointVisible) {
//				mapView.scrollBy(originX - e.getX(), originY - e.getY());
//			}
			e.consume();
		}

		public void viewChanged(RecommendationsViewChangedEvent event) {
			if(RecommendationsViewChangedEvent.MODEL_CHANGED_TYPE.equals(event.getType())) {
				mTabbedPane.setSelectedIndex(lastRecommTabIndex);
			}			
		}

		public void closeView() {
			SwingUtilities.invokeLater(new  Runnable() {
				public void run() {
					if(lastRecommTabIndex > 0) {
						mTabbedPane.removeTabAt(lastRecommTabIndex);
						lastRecommTabIndex = -1;
					}
				}
			});
			
			
		}
	}