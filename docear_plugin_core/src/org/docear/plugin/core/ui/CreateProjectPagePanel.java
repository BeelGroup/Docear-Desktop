package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class CreateProjectPagePanel extends AWizardPage {

	private static final long serialVersionUID = 1L;
	private JTextField txtProjectName;
	private JTextField txtProjectHome;
	private JTextField txtProjectName_1;
	private JTextField txtProjectHome_1;
	private JTextField txtBibFile;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param settings 
	 **********************************************************************************/

	public CreateProjectPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JRadioButton rdbtnStartFromScratch = new JRadioButton("Start from Scratch");
		rdbtnStartFromScratch.setSelected(true);
		rdbtnStartFromScratch.setBackground(Color.WHITE);
		rdbtnStartFromScratch.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(rdbtnStartFromScratch, "2, 2, 9, 1");
		
		JLabel lblProjectName = new JLabel("Project Name");
		add(lblProjectName, "4, 4, right, default");
		
		txtProjectName = new JTextField();
		txtProjectName.setText("Project Name");
		add(txtProjectName, "6, 4, fill, default");
		txtProjectName.setColumns(10);
		
		JLabel lblProjectHome = new JLabel("Project Home");
		add(lblProjectHome, "4, 6, right, default");
		
		txtProjectHome = new JTextField();
		txtProjectHome.setText("Project Home");
		add(txtProjectHome, "6, 6, fill, default");
		txtProjectHome.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse...");
		add(btnBrowse, "8, 6");
		
		JRadioButton rdbtnUseExistingPdfs = new JRadioButton("Use existing PDFs, references (BibTeX), and other files e.g. from Mendeley or Zotero");
		rdbtnUseExistingPdfs.setBackground(Color.WHITE);
		rdbtnUseExistingPdfs.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(rdbtnUseExistingPdfs, "2, 8, 9, 1");
		
		JEditorPane dtrpnWeHighlyRecommended = new JEditorPane();
		dtrpnWeHighlyRecommended.setForeground(new Color(192, 192, 192));
		dtrpnWeHighlyRecommended.setEditable(false);
		dtrpnWeHighlyRecommended.setText("We highly recommended that your PDFs and BibTeX files are in a subfolder of the\r\nProject home and not outside the project home (although it’s not a requirement).");
		add(dtrpnWeHighlyRecommended, "4, 9, 7, 1, fill, fill");
		
		MultiLineActionLabel multiLineActionLabel = new MultiLineActionLabel("test");
		multiLineActionLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
		multiLineActionLabel.setForeground(Color.LIGHT_GRAY);
		multiLineActionLabel.setBackground(Color.WHITE);
		add(multiLineActionLabel, "4, 10, 7, 1, fill, fill");
		
		JLabel lblProjectName_1 = new JLabel("Project Name");
		lblProjectName_1.setEnabled(false);
		add(lblProjectName_1, "4, 12, right, top");
		
		txtProjectName_1 = new JTextField();
		txtProjectName_1.setEnabled(false);
		txtProjectName_1.setText("Project Name");
		add(txtProjectName_1, "6, 12, fill, default");
		txtProjectName_1.setColumns(10);
		
		JLabel lblProjectHome_1 = new JLabel("Project Home");
		lblProjectHome_1.setEnabled(false);
		add(lblProjectHome_1, "4, 14, right, top");
		
		txtProjectHome_1 = new JTextField();
		txtProjectHome_1.setEnabled(false);
		txtProjectHome_1.setText("Project Home");
		add(txtProjectHome_1, "6, 14, fill, default");
		txtProjectHome_1.setColumns(10);
		
		JButton btnBrowse_2 = new JButton("Browse...");
		btnBrowse_2.setEnabled(false);
		add(btnBrowse_2, "8, 14");
		
		JLabel lblBibtexFile = new JLabel("BibTeX File");
		lblBibtexFile.setEnabled(false);
		add(lblBibtexFile, "4, 16, right, top");
		
		txtBibFile = new JTextField();
		txtBibFile.setEnabled(false);
		txtBibFile.setText("Bib File");
		add(txtBibFile, "6, 16, fill, default");
		txtBibFile.setColumns(10);
		
		JButton btnBrowse_1 = new JButton("Browse...");
		btnBrowse_1.setEnabled(false);
		add(btnBrowse_1, "8, 16");
		
		JLabel lblPdfsLiterature = new JLabel("PDFs / Literature");
		lblPdfsLiterature.setEnabled(false);
		add(lblPdfsLiterature, "4, 18, right, top");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setEnabled(false);
		add(scrollPane, "6, 18, 1, 5, fill, fill");
		
		JList list = new JList();
		list.setEnabled(false);
		scrollPane.setViewportView(list);
		
		JButton btnAdd = new JButton("Add...");
		btnAdd.setEnabled(false);
		add(btnAdd, "8, 18");
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		add(btnRemove, "8, 20");
		
		JRadioButton rdbtnImportFromMendeley = new JRadioButton("Import from Mendeley (not yet available)");
		rdbtnImportFromMendeley.setEnabled(false);
		rdbtnImportFromMendeley.setBackground(Color.WHITE);
		add(rdbtnImportFromMendeley, "2, 24, 9, 1");
		
		JCheckBox chckbxIncludeDemoFiles = new JCheckBox("Include demo files");
		chckbxIncludeDemoFiles.setSelected(true);
		chckbxIncludeDemoFiles.setBackground(Color.WHITE);
		add(chckbxIncludeDemoFiles, "2, 26, 9, 1");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.create.title");
	}

	@Override
	public void preparePage(WizardContext context) {
		// TODO Auto-generated method stub
		
	}
}
