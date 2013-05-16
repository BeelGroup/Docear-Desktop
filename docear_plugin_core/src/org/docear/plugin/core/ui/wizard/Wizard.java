package org.docear.plugin.core.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

/***
 * 
 * @author mag
 *
 * @see http://www.oracle.com/technetwork/articles/javase/wizard-136789.html <br>http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
 */
public class Wizard {
	private WizardModel wizardModel;
	private WizardController wizardController;

	private JDialog Wizard;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private JButton backButton;
	private JButton nextButton;
	private JButton cancelButton;

	private int returnCode;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public Wizard(Frame owner) {

		wizardModel = new WizardModel();
		Wizard = new JDialog(owner);
		wizardController = new WizardController(this);

		initComponents();
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void registerWizardPanel(Object id, WizardPanelDescriptor panel) {
		cardPanel.add(panel.getPanelComponent(), id);
		wizardModel.registerPanel(id, panel);
	}

	void setBackButtonEnabled(boolean b) {
		backButton.setEnabled(b);
	}

	void setNextButtonEnabled(boolean b) {
		nextButton.setEnabled(b);
	}

	public void setCurrentPanel(Object id) {		
		WizardPanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();

		if (oldPanelDescriptor != null) {
			oldPanelDescriptor.aboutToHidePanel();
		}

		wizardModel.setCurrentPanel(id);
		wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();
		cardLayout.show(cardPanel, id.toString());
		wizardModel.getCurrentPanelDescriptor().displayingPanel();
	}

	private void initComponents() {
		JPanel buttonPanel = new JPanel();
		Box buttonBox = new Box(BoxLayout.X_AXIS);
		
		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		backButton = new JButton();
		nextButton = new JButton();
		cancelButton = new JButton();

		backButton.addActionListener(wizardController);
		nextButton.addActionListener(wizardController);
		cancelButton.addActionListener(wizardController);

		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		buttonBox.add(backButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(nextButton);
		buttonBox.add(Box.createHorizontalStrut(30));
		buttonBox.add(cancelButton);
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
		Wizard.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		Wizard.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);

	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
