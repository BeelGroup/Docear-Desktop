package org.docear.plugin.core.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.freeplane.core.util.TextUtils;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

/***
 * 
 * @author mag
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

	private volatile int returnCode = NOT_DEFINED;
	private Thread returnCodeObserver;
	private WizardContext context;
	private boolean cancelEnabled = true;
	private boolean cancelButtonEnabled = false;
	private boolean drawButtonBarSeparator = false;
	private JButton closeButton;
	private JEditorPane titleComponent;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public Wizard(Frame owner) {
		wizardModel = new WizardModel();
		wizard = new JDialog(owner);
		wizardController = new WizardController(this);
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
		resetControls();
		WizardPageDescriptor oldPageDescriptor = wizardModel.getCurrentPageDescriptor();

		if (oldPageDescriptor != null) {
			oldPageDescriptor.aboutToHidePage(getContext());
		}

		wizardModel.setCurrentPage(id);
		wizardModel.getCurrentPageDescriptor().aboutToDisplayPage(getContext());
		if(wizardModel.getCurrentPageDescriptor().getPage().isPageDisplayable()) {
			cardLayout.show(cardPanel, id.toString());
			wizardModel.getCurrentPageDescriptor().displayingPage(getContext());
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
	
	
	public WizardContext getContext() {
		if(context == null) {
			context = new WizardContext() {				
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
			};
		}
		return context;
	}
	
	public synchronized int show() {		
		returnCode = NOT_DEFINED;
		returnCodeObserver.start();
		wizard.setSize(640, 480);
		centerOnOwner(wizard);
		
		SwingUtilities.updateComponentTreeUI(wizard);
		wizard.pack();
		wizard.setVisible(true);
		
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
		}
	}
	
	public void finish() {
		if(NOT_DEFINED == returnCode) {
			returnCode = OK_OPTION;
		}
	}
	
	private void initComponents() {
		JPanel mainPanel = new JPanel(true);
		JPanel contentPanel = new JPanel();
		JPanel headPanel = new JPanel();
		JPanel headControls = new JPanel();
		JPanel titlePanel = new JPanel();
		titleComponent = new JEditorPane();
		titleComponent.setBackground(Color.WHITE);
		
		final LineBorder lineBorder = new LineBorder(new Color(0,0,0,196), 1);
		
		buttonPanel = new JPanel();
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorder(lineBorder);
		
		headPanel.setLayout(new BorderLayout());
		headPanel.setBackground(Color.WHITE);
		headPanel.setPreferredSize(new Dimension(0, 60));
		headPanel.setBorder(new EmptyBorder(0, 10, 5, 10));		
		
		closeButton = new JButton(new ImageIcon(Wizard.class.getResource("/images/window-close.png")));
		closeButton.setPreferredSize(new Dimension(35, 25));
		closeButton.setMargin(new Insets(10, 10, 7, 10));
		
		headControls.setLayout(new BorderLayout());
		headControls.setBorder(new EmptyBorder(-6, 0, 0, 0));
		headControls.setBackground(Color.WHITE);
		headControls.add(closeButton, BorderLayout.NORTH);
		titlePanel.setBackground(Color.WHITE);
		titlePanel.setBorder(null);
		titlePanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("388px:grow"),},
			new RowSpec[] {
				RowSpec.decode("50px:grow"),}));
		
		titleComponent.setEditable(false);
		titleComponent.setFont(titleComponent.getFont().deriveFont(Font.BOLD, 14f));
		titlePanel.add(titleComponent, "1, 1, fill, center");
		
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
		nextButton = new JButton("next");
		cancelButton = new JButton("cancel");

		backButton.addActionListener(wizardController);
		nextButton.addActionListener(wizardController);
		cancelButton.addActionListener(wizardController);
		closeButton.addActionListener(wizardController);

		buildButtonBar();
		wizard.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(headPanel, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		contentPanel.add(cardPanel, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void buildButtonBar() {
		buttonPanel.removeAll();
		if(isButtonBarSeparatorEnabled()) {
			buttonPanel.add(new JSeparator(), BorderLayout.NORTH);
		}
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
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
			backButton.setVisible(true);
			backButton.setEnabled(true);
			backButton.setText(TextUtils.getText("docear.setup.wizard.controls.back"));
		}
		if(skipButton != null) {
			skipButton.setVisible(true);
			skipButton.setEnabled(true);
			skipButton.setText(TextUtils.getText("docear.setup.wizard.controls.skip"));
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
