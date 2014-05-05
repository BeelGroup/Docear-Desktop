package org.docear.plugin.core.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/***
 * 
 * @author genzmehr@docear.org
 *
 * @see http://www.oracle.com/technetwork/articles/javase/wizard-136789.html <br>http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
 */
public class Wizard {
	public static final WizardPageDescriptor FINISH_PAGE;
	static {
		FINISH_PAGE = new WizardPageDescriptor("_WIZARD_FINISH_WIZARD_", null);
	}
	private static final int NOT_DEFINED = -1;
	public static final int CANCEL_OPTION = 0;
	public static final int OK_OPTION = 1;
	public static final int SKIP_OPTION = 2;
	
	
	private WizardModel wizardModel;
	private WizardController wizardController;

	private JDialog wizard;
	private JPanel cardPanel;
	private JPanel buttonPanel;
	private CardLayout cardLayout;

	private JButton backButton;
	private JButton skipButton;
	private JButton nextButton;
	private JButton cancelButton;
	private JButton closeButton;

	private volatile int returnCode = NOT_DEFINED;
	private Thread returnCodeObserver;
	private WizardSession context;
	private boolean cancelEnabled = true;
	private boolean cancelButtonEnabled = false;
	private boolean skipEnabled = false;
	private boolean drawButtonBarSeparator = false;
	private JEditorPane titleComponent;
	private WizardPageDescriptor startPageIdentifier;
	private boolean isResizable;
	private IPageKeyBindingProcessor pageKeyBindingProcessor;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public Wizard(Window owner) {
		wizardModel = new WizardModel();
		wizard = new JDialog(owner);
		wizardController = new WizardController(this);
		wizard.setModal(true);
		wizard.setUndecorated(true);
		wizard.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		initReturnCodeObserver();
		initComponents();
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void registerWizardPanel(WizardPageDescriptor page) {
		cardPanel.add(page.getPage(), page.getIdentifier());
		wizardModel.registerPage(page.getIdentifier(), page);
	}

	public void setCurrentPage(Object id) {
		if(id == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(FINISH_PAGE.getIdentifier().equals(id)) {
			finish();
			return;
		}
		resetControls();
		WizardPageDescriptor oldPageDescriptor = wizardModel.getCurrentPageDescriptor();

		if (oldPageDescriptor != null) {
			oldPageDescriptor.aboutToHidePage(getSession());
		}

		wizardModel.setCurrentPage(id);
		wizardModel.getCurrentPageDescriptor().aboutToDisplayPage(getSession());
		if(wizardModel.getCurrentPageDescriptor().getPage().isPageDisplayable()) {
			this.pageKeyBindingProcessor = wizardModel.getCurrentPageDescriptor().getKeyBindingProcessor();
			cardLayout.show(cardPanel, id.toString());
			wizardModel.getCurrentPageDescriptor().displayingPage(getSession());
			if(wizardModel.getCurrentPageDescriptor().resizeWizard()) {
				wizard.pack();
			}
		}
		else {
			nextButton.doClick();
		}
	}
	
	public void setCancelEnabled(boolean enabled) {
		this.cancelEnabled = enabled;
	}
	
	public boolean isCancelEnabled() {
		return cancelEnabled;
	}
	
	public void setSkipEnabled(boolean enabled) {
		this.skipEnabled = enabled;
	}
	
	public boolean isSkipEnabled() {
		return skipEnabled;
	}
	
	public void setCancelButtonEnabled(boolean enabled) {
		this.cancelButtonEnabled = enabled;
	}
	
	public boolean isCancelButtonEnabled() {
		return cancelButtonEnabled;
	}
	
	public boolean isCancelControl(Object source) {
		if((closeButton != null && closeButton.equals(source)) || (cancelButton != null && cancelButton.equals(source))) {
			return true;
		}
		return false;
	}
	
	public void setEnableButtonBarSeparator(boolean enabled) {
		drawButtonBarSeparator = enabled;
	}
	
	public boolean isButtonBarSeparatorEnabled() {
		return drawButtonBarSeparator;
	}
	
	
	public WizardSession getSession() {
		if(context == null) {
			context = new WizardSession() {				
				@Override
				public WizardPageDescriptor getCurrentDescriptor() {
					return wizardModel.getCurrentPageDescriptor();
				}

				@Override
				public WizardModel getModel() {
					return wizardModel;
				}

				@Override
				public AbstractButton getNextButton() {
					return nextButton;
				}

				@Override
				public AbstractButton getBackButton() {
					return backButton;
				}

				@Override
				public AbstractButton getSkipButton() {
					return skipButton;
				}

				@Override
				public void setWizardTitle(String title) {
					setTitle(title);
				}

				@Override
				public void gotoPage(String identifier) {
					Wizard.this.setCurrentPage(identifier);
				}

				@Override
				public void finish() {
					Wizard.this.finish();					
				}

				@Override
				public void cancel() {
					Wizard.this.cancel();
				}

			};
		}
		return context;
	}
	
	public void setStartPage(Object identifier) {
		this.startPageIdentifier = wizardModel.getPage(identifier);
	}
	
	public void setResizable(boolean b) {
		this.isResizable = b;
	}
	
	public boolean isResizable() {
		return this.isResizable;
	}
	
	public WizardPageDescriptor getStartPage() {
		if(this.startPageIdentifier == null) {
			return wizardModel.getFirstPage();
		}
		return this.startPageIdentifier;
	}
	
	public synchronized int show() {
		returnCode = NOT_DEFINED;
		returnCodeObserver.start();
		wizard.setMinimumSize(new Dimension(150, 150));
		wizard.setSize(640, 480);
		
		resetControls();
		if(getStartPage() != null) {
			setCurrentPage(getStartPage().getIdentifier());
		}
		
		SwingUtilities.updateComponentTreeUI(wizard);
		wizard.pack();
		centerOnOwner(wizard);
		wizard.toFront();
		wizard.setVisible(true);
		Window.getWindows();
		wizard.toFront();
		try {
			returnCodeObserver.join();
		} catch (InterruptedException e) {
		}
		wizard.setVisible(false);
		wizard.dispose();
		return returnCode;
	}
	
	public void setTitle(String title) {
		titleComponent.setText(title);
	}
	
	public String getTitle() {
		return titleComponent.getText();
	}

	public void cancel() {
		if(NOT_DEFINED == returnCode) {
			returnCode = CANCEL_OPTION;
			wizard.dispatchEvent(new WindowEvent(wizard, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	public void finish() {
		if(NOT_DEFINED == returnCode) {
			returnCode = OK_OPTION;
			wizard.dispatchEvent(new WindowEvent(wizard, WindowEvent.WINDOW_CLOSING));
		}
	}
	
	public void skipAll() {
		if(NOT_DEFINED == returnCode) {
			returnCode = SKIP_OPTION;
			wizard.dispatchEvent(new WindowEvent(wizard, WindowEvent.WINDOW_CLOSING));
		}
	}
	


	private boolean processPageKeyBindings(KeyStroke ks, KeyEvent e, boolean pressed) {
		if(this.pageKeyBindingProcessor != null) {
			return pageKeyBindingProcessor.processKeyEvent(e);
		}
		return false;
	}
	
	private void initComponents() {
		WizardMouseAdapter mAdapter = new WizardMouseAdapter(this);
		
		final JPanel mainPanel = new JPanel(true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
				if(!processPageKeyBindings(ks, e, pressed)) {
					return super.processKeyBinding(ks, e, condition, pressed);
				}
				return true;
			}
		};
		JPanel contentPanel = new JPanel();
		final JPanel headPanel = new JPanel();
		JPanel headControls = new JPanel();
		final JPanel titlePanel = new JPanel();
		titleComponent = new JEditorPane();
		titleComponent.setBackground(Color.WHITE);
		
		final LineBorder lineBorder = new LineBorder(new Color(0,0,0,196), 1);
		
		buttonPanel = new JPanel();
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);
		//mainPanel.setBorder(lineBorder);
		mainPanel.setBorder(new CompoundBorder(
						new CompoundBorder(
							new CompoundBorder(
								new CompoundBorder(new LineBorder(new Color(0x10000000, true), 1),new LineBorder(new Color(0x20000000, true), 1)),new LineBorder(new Color(0x30000000, true), 1)),new LineBorder(new Color(0x40000000, true), 1))
		, lineBorder));
		
		headPanel.setLayout(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setPreferredSize(new Dimension(0, 60));
		headPanel.setBorder(new EmptyBorder(0, 10, 5, 10));
		headPanel.addMouseMotionListener(mAdapter);
		headPanel.addMouseListener(mAdapter);
		closeButton = new JButton(new ImageIcon(Wizard.class.getResource("/images/window-close.png")));
		closeButton.setPreferredSize(new Dimension(35, 25));
		closeButton.setMargin(new Insets(10, 10, 7, 10));
		
		headControls.setLayout(new BorderLayout());
		headControls.setBorder(new EmptyBorder(-6, 0, 0, 0));
		headControls.setBackground(Color.WHITE);
		headControls.add(closeButton, BorderLayout.NORTH);
		titlePanel.setBackground(Color.WHITE);
		titlePanel.setBorder(null);
		titlePanel.addMouseMotionListener(mAdapter);
		titlePanel.addMouseListener(mAdapter);
		titlePanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("388px:grow"),},
			new RowSpec[] {
				RowSpec.decode("50px:grow"),}));
		
		titleComponent.setEditable(false);
		titleComponent.setFont(titleComponent.getFont().deriveFont(Font.BOLD, 14f));
		titleComponent.addMouseMotionListener(mAdapter);
		titleComponent.addMouseListener(mAdapter);
		titlePanel.add(titleComponent, "1, 1, fill, center");
		
		mAdapter.addComponentDragListener(new ComponentDragListener() {
			
			@Override
			public void componentDragged(ComponentDragEvent event) {
				if(event.getComponent() == titleComponent 
						|| event.getComponent() == headPanel 
						|| event.getComponent() == titlePanel ) {
					Point nuPoint = wizard.getLocation();
					nuPoint.x += event.deltaX;
					nuPoint.y += event.deltaY;
					wizard.setLocation(nuPoint);
					event.consume();
				}
			}
			
			public void componentAdjustResizeCursor(AdjustResizeCursorEvent event) { }
		});
		
		headPanel.add(headControls, BorderLayout.EAST);
		headPanel.add(titlePanel, BorderLayout.CENTER);
		
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(0, 10, 10, 10)), lineBorder));
		
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBackground(Color.WHITE);
		
		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		cardPanel.setBackground(Color.WHITE);

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		
		backButton = new JButton("back");
		backButton.setBackground(Color.WHITE);
		nextButton = new JButton("next");
		nextButton.setBackground(Color.WHITE);
		skipButton = new JButton("skip");
		skipButton.setBackground(Color.WHITE);
		cancelButton = new JButton("cancel");
		cancelButton.setBackground(Color.WHITE);

		backButton.addActionListener(wizardController);
		nextButton.addActionListener(wizardController);
		skipButton.addActionListener(wizardController);
		cancelButton.addActionListener(wizardController);
		closeButton.addActionListener(wizardController);

		buildButtonBar();
		wizard.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(headPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		contentPanel.add(cardPanel, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainPanel.addMouseListener(mAdapter);
		mainPanel.addMouseMotionListener(mAdapter);
		
		mAdapter.addComponentDragListener(new ComponentDragListener() {
			
			@Override
			public void componentDragged(ComponentDragEvent event) {
				if(event.getComponent() == mainPanel) {
					if(isResizable() && event.isResizeEvent()) {
						Dimension pageDim = wizardModel.getCurrentPageDescriptor().getPage().getPreferredSize();
						Dimension cardDim = cardPanel.getPreferredSize();
						Dimension wizardDim = wizard.getPreferredSize();
						if(event.resizeDirection(ComponentDragEvent.DIRECTION_HORIZONTAL)) {
							pageDim.width += event.deltaX;
							cardDim.width += event.deltaX;
							wizardDim.width += event.deltaX;
						}
						if(event.resizeDirection(ComponentDragEvent.DIRECTION_VERTICAL)) {
							pageDim.height += event.deltaY;
							cardDim.height += event.deltaY;
							wizardDim.height += event.deltaY;
						}
						wizardModel.getCurrentPageDescriptor().getPage().setPreferredSize(pageDim);
						cardPanel.setPreferredSize(cardDim);
						wizard.setPreferredSize(wizardDim);
						wizard.pack();
						event.consume();
					}
				}
			}

			@Override
			public void componentAdjustResizeCursor(AdjustResizeCursorEvent event) {
				if(event.getComponent() == mainPanel) {
					adjustCursor(mainPanel, event.getResizeSensor());
				}
				
			}
		});
	}
	
	private void adjustCursor(Component component, int resizeSensor) {
		if((resizeSensor & WizardMouseAdapter.BORDER_LEFT) > 0) {
			if((resizeSensor & WizardMouseAdapter.BORDER_TOP) > 0) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
			}
			else if((resizeSensor & WizardMouseAdapter.BORDER_BOTTOM) > 0) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
			}
			else {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			}
		}
		else if((resizeSensor & WizardMouseAdapter.BORDER_RIGHT) > 0) {
			if((resizeSensor & WizardMouseAdapter.BORDER_TOP) > 0) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
			}
			else if((resizeSensor & WizardMouseAdapter.BORDER_BOTTOM) > 0) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			}
			else {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			}
		}
		else if((resizeSensor & WizardMouseAdapter.BORDER_TOP) > 0) {
			component.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			
		}
		else if((resizeSensor & WizardMouseAdapter.BORDER_BOTTOM) > 0) {
			component.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		}
		else {
			component.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	private void buildButtonBar() {
		buttonPanel.removeAll();
		if(isButtonBarSeparatorEnabled()) {
			buttonPanel.add(new JSeparator(), BorderLayout.NORTH);
		}
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		
		buttonBox.add(skipButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(backButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(nextButton);
		if(isCancelButtonEnabled()) {
			buttonBox.add(Box.createHorizontalStrut(30));
			buttonBox.add(cancelButton);
		}
		
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
		
	}

	private void centerOnOwner(JDialog dialog) {
		Dimension dim;
		Point loc;
		if(dialog.getOwner() == null) {
			dim = Toolkit.getDefaultToolkit().getScreenSize();
			loc = new Point(0, 0);
		}
		else {
			dim = dialog.getOwner().getSize();
			loc = dialog.getOwner().getLocation();
		}
		int x = loc.x + ((dim.width/2)-(dialog.getWidth()/2));
		int y = loc.y + ((dim.height/2)-(dialog.getHeight()/2));
		dialog.setLocation(x, y);
	}
	
	private void initReturnCodeObserver() {
		if(returnCodeObserver == null) {
			Runnable observer = new Runnable() {
				@Override
				public void run() {
					try {
						while(returnCode == NOT_DEFINED) {
							Thread.sleep(50);
							Thread.yield();
						}
					} catch (InterruptedException e) {
						returnCode = CANCEL_OPTION;
					}
				}
			};
			returnCodeObserver = new Thread(observer);
		}
	}

	public void resetControls() {
		if(nextButton != null) {
			nextButton.setVisible(true);
			nextButton.setEnabled(true);
			nextButton.setText(TextUtils.getText("docear.setup.wizard.controls.next"));
			nextButton.setDefaultCapable(true);
			wizard.getRootPane().setDefaultButton(nextButton);
		}
		if(backButton != null) {
			backButton.setVisible(wizardModel.getPageCount() > 1);
			backButton.setEnabled(true);
			backButton.setText(TextUtils.getText("docear.setup.wizard.controls.back"));
		}
		if(skipButton != null) {
			skipButton.setVisible(isSkipEnabled());
			skipButton.setEnabled(true);
			skipButton.setText(TextUtils.getText("docear.setup.wizard.controls.skip"));
		}
		closeButton.setVisible(isCancelEnabled());
	}

	
	public static int showConfirmDialog(String message) {
		Wizard wiz = new Wizard(UITools.getFrame());
		try {
			wiz.setResizable(true);
			wiz.registerWizardPanel(new ConfirmDialogPanel(message));
			wiz.setCancelEnabled(true);
			return wiz.show();
		}
		catch (Exception ignore) {
		}
		return CANCEL_OPTION;
	}

	
	static class ConfirmDialogPanel extends WizardPageDescriptor {
		
		//***********************************************************************************
		// CONSTRUCTORS
		//***********************************************************************************

		public ConfirmDialogPanel(final String message) {
			super("confirm_page", new AWizardPage() {
	
				private static final long serialVersionUID = 1L;
				private boolean initialized = false;
				
				@Override
				public void preparePage(WizardSession session) {
					if(!initialized) {
						this.setLayout(new BorderLayout());
						this.setBackground(Color.WHITE);
						this.add(new JLabel(message), BorderLayout.CENTER);
					}
					session.setWizardTitle(getTitle());
					session.getBackButton().setEnabled(true);
					session.getBackButton().setVisible(true);
					session.getNextButton().setEnabled(true);
					session.getBackButton().setText(TextUtils.getText("wizard.dialog.default.confirm.back.text"));
					session.getNextButton().setText(TextUtils.getText("wizard.dialog.default.confirm.next.text"));
				}
				
				@Override
				public String getTitle() {
					return TextUtils.getText("wizard.dialog.default.confirm.title");
				}
			});
		}

		@Override
		public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
			context.finish();
			return Wizard.FINISH_PAGE;
		}

		@Override
		public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
			context.cancel();
			return Wizard.FINISH_PAGE;
		}
		
		//***********************************************************************************
		// METHODS
		//***********************************************************************************

		
		//***********************************************************************************
		// REQUIRED METHODS FOR INTERFACES
		//***********************************************************************************
	}
}
