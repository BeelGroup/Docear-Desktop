package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.components.DocearHTMLPanel;
import org.docear.plugin.core.ui.components.DocearLicensePanel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SecondPagePanel extends AWizardPage {
	public enum DATA_OPTION {
		CREATE, IMPORT, EMPTY, SYNCH
	}
	
	private static final long serialVersionUID = 1L;
	private JCheckBox chckbxAcceptTOS;
	private WizardContext cachedContext;
	private JCheckBox chckbxAcceptUsageTerms;
	private JRadioButton rdbtnDataFromDocear;
	private JRadioButton rdbtnImportFromHarddisk;
	private JRadioButton rdbtnStartWithEmpty;
	private JRadioButton rdbtnCreateNewProject;
	private boolean localUser;
	private JLabel lblWhatNext;
	private JLabel lblTermsChapter;
	private MultiLineActionLabel lblProcessingTerms;
	private MultiLineActionLabel lblToS;

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
				ColumnSpec.decode("center:default"),
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
		
		lblWhatNext = new JLabel(" ");
		lblWhatNext.setFont(new Font("Tahoma", Font.BOLD, 14));
		add(lblWhatNext, "2, 2, 4, 1");
		
		rdbtnDataFromDocear = new JRadioButton(TextUtils.getText("docear.setup.wizard.second.fromcloud.label"));
		rdbtnDataFromDocear.setBackground(Color.WHITE);
		rdbtnDataFromDocear.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				updateOption();
			}
		});
		add(rdbtnDataFromDocear, "4, 4, 2, 1");
		rdbtnDataFromDocear.setEnabled(false);
		
		rdbtnImportFromHarddisk = new JRadioButton(TextUtils.getText("docear.setup.wizard.second.import.label"));
		rdbtnImportFromHarddisk.setBackground(Color.WHITE);
		rdbtnImportFromHarddisk.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				updateOption();
			}
		});
		add(rdbtnImportFromHarddisk, "4, 5, 2, 1");
		
		rdbtnStartWithEmpty = new JRadioButton(TextUtils.getText("docear.setup.wizard.second.empty.label"));
		rdbtnStartWithEmpty.setBackground(Color.WHITE);
		rdbtnStartWithEmpty.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				updateOption();
			}
		});
		add(rdbtnStartWithEmpty, "4, 6, 2, 1");
		
		rdbtnCreateNewProject = new JRadioButton(TextUtils.getText("docear.setup.wizard.second.create.label"));
		rdbtnCreateNewProject.setBackground(Color.WHITE);
		rdbtnCreateNewProject.setSelected(true);
		rdbtnCreateNewProject.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateOption();
			}
		});
		add(rdbtnCreateNewProject, "4, 7, 2, 1");
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnCreateNewProject);
		group.add(rdbtnStartWithEmpty);
		group.add(rdbtnImportFromHarddisk);
		group.add(rdbtnDataFromDocear);
		
		
		lblTermsChapter = new JLabel(TextUtils.getText("docear.setup.wizard.docear.terms.title"));
		lblTermsChapter.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblTermsChapter, "4, 11, 2, 1");
		
		chckbxAcceptUsageTerms = new JCheckBox();
		chckbxAcceptUsageTerms.setBackground(Color.WHITE);
		chckbxAcceptUsageTerms.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		add(chckbxAcceptUsageTerms, "4, 13, default, top");
		
		final DocearLicensePanel licenseText = new DocearLicensePanel();
		
		lblProcessingTerms = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.docear.terms.processing"));
		lblProcessingTerms.setBackground(Color.WHITE);
		lblProcessingTerms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("top".equals(e.getActionCommand())) {
					licenseText.setLicenseText(DocearController.getController().getDataProcessingTerms());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.data_processing.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
				}
				else if("gpl".equals(e.getActionCommand())) {
					DocearHTMLPanel gplPanel = new DocearHTMLPanel();
					String text = DocearController.getController().getGPLv2Terms();
					gplPanel.setText(text);
					JOptionPane.showConfirmDialog(getRootPane(), gplPanel, TextUtils.getText("docear.license.data_processing.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
				}
			}
		});
		add(lblProcessingTerms, "5, 13, fill, top");
		
		chckbxAcceptTOS = new JCheckBox();
		chckbxAcceptTOS.setBackground(Color.WHITE);
		chckbxAcceptTOS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		add(chckbxAcceptTOS, "4, 14, default, top");
		
		lblToS = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.docear.terms.service"));
		lblToS.setBackground(Color.WHITE);
		lblToS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("tos".equals(e.getActionCommand())) {
					licenseText.setLicenseText(DocearController.getController().getTermsOfService());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.terms_of_use.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
				if("dps".equals(e.getActionCommand())) {
					licenseText.setLicenseText(DocearController.getController().getDataPrivacyTerms());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.data_privacy.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
			}
		});
		add(lblToS, "5, 14, fill, top");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void updateOption() {
		if (cachedContext != null) {
			enableControls(cachedContext);
			if(isCreateNewOption()) {
				cachedContext.set(DATA_OPTION.class, DATA_OPTION.CREATE);
			}
			else if (isEmptyOption()) {
				cachedContext.set(DATA_OPTION.class, DATA_OPTION.EMPTY);
			}
			else if (isImportOption()) {
				cachedContext.set(DATA_OPTION.class, DATA_OPTION.IMPORT);
			}
			else if (isFromCloudOption()) {
				cachedContext.set(DATA_OPTION.class, DATA_OPTION.SYNCH);
			}
		}
	}
	
	private void enableControls(WizardContext context) {
		if(context != null) {
			context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.next"));
			if(isTermsAccepted() && getOption() != null) {
				context.getNextButton().setEnabled(true);
				getRootPane().setDefaultButton((JButton) context.getNextButton());
				if(isEmptyOption()) {
					context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
				}
			}
			else {
				getRootPane().setDefaultButton((JButton) context.getBackButton());
				context.getNextButton().setEnabled(false);				
			}
		}
		
	}
	
	public boolean isFromCloudOption() {
		return rdbtnDataFromDocear.isSelected();
	}
	
	public boolean isCreateNewOption() {
		return rdbtnCreateNewProject.isSelected();
	}
	
	public boolean isImportOption() {
		return rdbtnImportFromHarddisk.isSelected();
	}
	
	public boolean isEmptyOption() {
		return rdbtnStartWithEmpty.isSelected();
	}
	
	public DATA_OPTION getOption() {
		if(isFromCloudOption()) {
			return DATA_OPTION.SYNCH;
		}
		if(isCreateNewOption()) {
			return DATA_OPTION.CREATE;
		}
		if(isEmptyOption()) {
			return DATA_OPTION.EMPTY;
		}
		if(isImportOption()) {
			return DATA_OPTION.IMPORT;
		}
		return null;
	}
	
	public boolean isTermsAccepted() {
		return (localUser && chckbxAcceptUsageTerms.isSelected()) || (chckbxAcceptTOS.isSelected() && chckbxAcceptUsageTerms.isSelected());
	}
	
	private void prepareForOnlineUser() {
		this.localUser = false;
		chckbxAcceptTOS.setVisible(true);
		chckbxAcceptUsageTerms.setVisible(true);
		lblTermsChapter.setVisible(true);
		lblToS.setVisible(true);
		lblProcessingTerms.setText(TextUtils.getText("docear.setup.wizard.docear.terms.processing"));
		lblProcessingTerms.setVisible(true);
		lblWhatNext.setText(TextUtils.getText("docear.setup.wizard.second.title.login"));
	}
	private void prepareForLocalUser() {
		this.localUser = true;
		chckbxAcceptTOS.setVisible(false);
		chckbxAcceptUsageTerms.setVisible(true);
		lblTermsChapter.setVisible(false);
		lblToS.setVisible(false);
		lblProcessingTerms.setText(TextUtils.getText("docear.setup.wizard.docear.terms.gpl"));
		lblProcessingTerms.setVisible(true);
		lblWhatNext.setText(TextUtils.getText("docear.setup.wizard.second.title.local"));
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.second.title");
	}



	@Override
	public void preparePage(WizardContext context) {
		this.cachedContext = context;
		context.setWizardTitle(getTitle());
		if(context.get(DocearLocalUser.class) != null) {
			prepareForLocalUser();
		}
		else {
			prepareForOnlineUser();
		}
		updateOption();
	}
}
