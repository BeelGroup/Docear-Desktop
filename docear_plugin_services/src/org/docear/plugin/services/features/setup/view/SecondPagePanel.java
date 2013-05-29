package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SecondPagePanel extends AWizardPage {

	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param settings 
	 **********************************************************************************/

	public SecondPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblWhatUWant = new JLabel("What u want");
		lblWhatUWant.setFont(new Font("Tahoma", Font.BOLD, 14));
		add(lblWhatUWant, "2, 2, 3, 1");
		
		JRadioButton rdbtnDataFromDocear = new JRadioButton("data from docear");
		rdbtnDataFromDocear.setBackground(Color.WHITE);
		add(rdbtnDataFromDocear, "4, 4");
		
		JRadioButton rdbtnImportFromHarddisk = new JRadioButton("Import from Harddisk");
		rdbtnImportFromHarddisk.setBackground(Color.WHITE);
		add(rdbtnImportFromHarddisk, "4, 5");
		
		JRadioButton rdbtnStartWithEmpty = new JRadioButton("Start with empty workspace");
		rdbtnStartWithEmpty.setBackground(Color.WHITE);
		add(rdbtnStartWithEmpty, "4, 6");
		
		JRadioButton rdbtnCreateNewProject = new JRadioButton("Create new project");
		rdbtnCreateNewProject.setBackground(Color.WHITE);
		add(rdbtnCreateNewProject, "4, 7");
		
		JLabel lblAndTheFine = new JLabel("And the fine print");
		lblAndTheFine.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblAndTheFine, "4, 11");
		
		JCheckBox chckbxIAccept = new JCheckBox("I accept");
		chckbxIAccept.setBackground(Color.WHITE);
		add(chckbxIAccept, "4, 13");
		
		JCheckBox chckbxIAcceptThe = new JCheckBox("I accept the TOS");
		chckbxIAcceptThe.setBackground(Color.WHITE);
		add(chckbxIAcceptThe, "4, 14");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.title");
	}



	@Override
	public void preparePage(WizardContext context) {
		// TODO Auto-generated method stub
		
	}
}
