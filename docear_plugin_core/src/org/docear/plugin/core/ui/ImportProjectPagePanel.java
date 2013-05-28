package org.docear.plugin.core.ui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImportProjectPagePanel extends AWizardPage {
	
	private static final long serialVersionUID = 1L;
	private JTextField txtImportHome;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ImportProjectPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JEditorPane dtrpnInfo = new JEditorPane();
		dtrpnInfo.setBackground(Color.WHITE);
		dtrpnInfo.setEnabled(true);
		dtrpnInfo.setEditable(false);
		dtrpnInfo.setText("Ensure that your project is already stored at the location you want it to be stored. Docear will neither copy nor move your data.");
		add(dtrpnInfo, "2, 2, 5, 1, fill, fill");
		
		JLabel lblSelectTheHome = new JLabel("Select the home folder of your project");
		add(lblSelectTheHome, "2, 6, 5, 1");
		
		txtImportHome = new JTextField();
		txtImportHome.setText("import home");
		add(txtImportHome, "2, 8, fill, default");
		txtImportHome.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		add(btnBrowse, "4, 8");
		
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/



	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.import.title");
	}

	@Override
	public void preparePage(WizardContext context) {
		// TODO Auto-generated method stub
		
	}
}
