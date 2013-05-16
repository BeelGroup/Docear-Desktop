package org.docear.plugin.core.ui.ndialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import org.freeplane.core.ui.components.UITools;

import com.sun.awt.AWTUtilities;

public class Wizard {

	WizzardFrame frame = null;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public Wizard(Dimension size) {
		frame = new WizzardFrame(size);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void start(boolean waitFor) {
		if(frame == null) {
			frame = new WizzardFrame(new Dimension(640, 480));
		}
		if(!frame.isVisible()) {
			frame.setLocation(frame.getOwner().getLocation());
			frame.setSize(frame.getOwner().getSize());
			frame.setVisible(true);
		}
	}
	
	private class WizzardFrame extends Window {
		
		private static final long serialVersionUID = 1L;
		private JPanel contentPane;
		private JLabel lblTitle;
		private JScrollPane scrPane;
		private JPanel footLine;
		private JPanel stepMeter;
		private JPanel dimScreen;
		private Dimension internalSize;


		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public WizzardFrame(Dimension size) {
			super(UITools.getFrame());
			internalSize = size;
			UITools.getFrame().addComponentListener(new ComponentListener() {
				@Override
				public void componentShown(ComponentEvent e) {}
				
				@Override
				public void componentResized(ComponentEvent e) {
					WizzardFrame.this.setSize(e.getComponent().getSize());
				}
				
				@Override
				public void componentMoved(ComponentEvent e) {
					WizzardFrame.this.setLocation(e.getComponent().getLocation());
				}
				
				@Override
				public void componentHidden(ComponentEvent e) {
				}
			});
			
			this.addWindowListener(new WindowListener() {
				public void windowOpened(WindowEvent e) {
				}
				
				public void windowIconified(WindowEvent e) {
				}
				
				public void windowDeiconified(WindowEvent e) {
				}
				
				public void windowDeactivated(WindowEvent e) {
				}
				
				public void windowClosing(WindowEvent e) {
				}
				
				public void windowClosed(WindowEvent e) {
					dispose();
				}
				
				public void windowActivated(WindowEvent e) {
					
				}
			});
			
			setAlwaysOnTop(true);
			setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
			AWTUtilities.setWindowOpaque(this, false);
			setLayout(new LayoutManager() {
				public void removeLayoutComponent(Component comp) {
				}

				@Override
				public Dimension preferredLayoutSize(Container parent) {
					return parent.getPreferredSize();
				}

				@Override
				public Dimension minimumLayoutSize(Container parent) {
					return parent.getMinimumSize();
				}

				@Override
				public void layoutContainer(Container parent) {
					Dimension d = parent.getSize();
					for(Component comp : parent.getComponents()) {
						if(comp == contentPane) {						
							int x = ((d.width)/2)-(getInternalSize().width/2);
							int y = ((d.height)/2)-(getInternalSize().height/2);
							contentPane.setLocation(x, y);
							contentPane.setSize(getInternalSize());
						}
						else {
							comp.setSize(d);
							int x = ((d.width)/2)-(comp.getWidth()/2);
							int y = ((d.height)/2)-(comp.getHeight()/2);
							comp.setLocation(x, y);
						}
					}
				}

				@Override
				public void addLayoutComponent(String name, Component comp) {
				}
			});
			
			dimScreen = new JPanel(true);
			dimScreen.setBackground(new Color(0,0,0,10));
			add(dimScreen);
			
			
			contentPane = new JPanel(true);
			contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
			contentPane.setBackground(Color.WHITE);
			add(contentPane);
			
			contentPane.setLayout(new LayoutManager() {
				
				@Override
				public void addLayoutComponent(String name, Component comp) {
				}

				@Override
				public void removeLayoutComponent(Component comp) {
					
				}

				@Override
				public Dimension preferredLayoutSize(Container parent) {
					return parent.getPreferredSize();
				}

				@Override
				public Dimension minimumLayoutSize(Container parent) {
					return parent.getMinimumSize();
				}

				@Override
				public void layoutContainer(Container parent) {
					Insets insets = parent.getInsets();
					if(insets == null) {
						insets = new Insets(1, 1, 1, 1);
					}
					for(Component comp : parent.getComponents()) {
						if(comp == getTitleBar()) {
							comp.setLocation(10, insets.top);
							comp.setSize(parent.getWidth()-10-insets.left-insets.right, 50);
							comp.setPreferredSize(comp.getSize());
						}
						else if(comp == getContentArea()) {
							comp.setLocation(10, insets.top+50);
							comp.setSize(parent.getWidth()-20, Math.max(0, parent.getHeight()-80-insets.top-insets.bottom));
							comp.setPreferredSize(comp.getSize());
						}
						else if(comp == getControlsBar()) {
							comp.setLocation(insets.left, Math.max(0, parent.getHeight()-30-insets.bottom));
							comp.setSize(parent.getWidth()-insets.left-insets.right, 30);
							comp.setPreferredSize(comp.getSize());
						}
						else {
							comp.setSize(0, 0);
							comp.setPreferredSize(comp.getSize());
						}
					}

				}
			});
			
			contentPane.add(getTitleBar());
			contentPane.add(getContentArea());
			contentPane.add(getControlsBar());
		}


		private Component getControlsBar() {
			if(footLine == null) {
				footLine = new JPanel();
				footLine.setBackground(Color.WHITE);
			}
			return footLine;
		}


		private JScrollPane getContentArea() {
			if(scrPane == null) {
				scrPane = new JScrollPane();
				scrPane.setBorder(new LineBorder(new Color(0, 0, 0)));
				scrPane.getViewport().setBackground(Color.WHITE);
			}
			return scrPane;
		}
		
		private JPanel getStepMeter() {
			if(stepMeter == null) {
				stepMeter = new JPanel();
			}
			return stepMeter;
		}
		
		private void setActiveView(Component view) {
			getContentArea().setViewportView(view);
		}
		
		private JLabel getTitleBar() {
			if(lblTitle == null) {
				lblTitle = new JLabel("Title");
				lblTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
			}
			return lblTitle;
		}
		
		private Dimension getInternalSize() {
			return internalSize;
		}
	}
}
