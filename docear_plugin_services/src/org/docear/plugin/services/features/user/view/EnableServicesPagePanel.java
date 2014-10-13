package org.docear.plugin.services.features.user.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class EnableServicesPagePanel extends AWizardPage {
	
	private static final long serialVersionUID = 1L;
	private JCheckBox chckbxCollaboration;
	private JCheckBox chckbxOnlineBackup;
	private JCheckBox chckbxSynchronization;
	private JCheckBox chckbxRecommendations;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private JLabel lblReclabel;
	private JLabel lblIconRecommendations;
	private JLabel lblBackup;
	private JLabel lblIconBackup;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public EnableServicesPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.UNRELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("12dlu:grow"),},
			new RowSpec[] {
				FormFactory.PARAGRAPH_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblServicesoptional = new JLabel(TextUtils.getText("docear.setup.wizard.register.services"));
		lblServicesoptional.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblServicesoptional, "2, 2, 4, 1");
		
		panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		add(panel_1, "3, 4, 3, 1, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				ColumnSpec.decode("20dlu"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chckbxRecommendations = new JCheckBox();
		panel_1.add(chckbxRecommendations, "1, 1, default, top");
		chckbxRecommendations.setSelected(true);
		chckbxRecommendations.setBackground(Color.WHITE);
		
		panel_2 = new JPanel();
		panel_2.setBackground(Color.WHITE);
		panel_1.add(panel_2, "3, 1, fill, fill");
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("default:grow"),}));
		
		lblReclabel = new JLabel(TextUtils.getText("docear.setup.wizard.register.feature.recommendations"));
		panel_2.add(lblReclabel, "1, 1");
		
		lblIconRecommendations = new JLabel(new ImageIcon(DocearController.class.getResource("/images/question_16.png")));
		lblIconRecommendations.addMouseListener(getHelpMouseListener());
		lblIconRecommendations.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//panel_2.add(lblIconRecommendations, "3, 1");
		
		chckbxSynchronization = new JCheckBox(TextUtils.getText("docear.setup.wizard.register.feature.synchronization"));
		panel_1.add(chckbxSynchronization, "5, 1, default, top");
		chckbxSynchronization.setBackground(Color.WHITE);
		chckbxSynchronization.setSelected(true);
		
		chckbxOnlineBackup = new JCheckBox();
		panel_1.add(chckbxOnlineBackup, "1, 2, default, top");
		chckbxOnlineBackup.setSelected(true);
		chckbxOnlineBackup.setBackground(Color.WHITE);
		
		panel_3 = new JPanel();
		panel_3.setBackground(Color.WHITE);
		panel_1.add(panel_3, "3, 2, fill, fill");
		panel_3.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("default:grow"),}));
		
		lblBackup = new JLabel(TextUtils.getText("docear.setup.wizard.register.feature.backup"));
		panel_3.add(lblBackup, "1, 1");
		
		lblIconBackup = new JLabel(new ImageIcon(DocearController.class.getResource("/images/question_16.png")));
		lblIconBackup.setBackground(Color.WHITE);
		lblIconBackup.addMouseListener(getHelpMouseListener());
		lblIconBackup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//panel_3.add(lblIconBackup, "3, 1");
		
		chckbxCollaboration = new JCheckBox(TextUtils.getText("docear.setup.wizard.register.feature.collaboration"));
		panel_1.add(chckbxCollaboration, "5, 2, default, top");
		chckbxCollaboration.setBackground(Color.WHITE);
		chckbxCollaboration.setSelected(true);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private MouseListener getHelpMouseListener() {
		return new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {}
			
			public void mouseEntered(MouseEvent e) {}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == lblIconRecommendations) {
					//DOCEAR - todo: show recommendations help
				}
				else if(e.getSource() == lblIconBackup) {
					//DOCEAR - todo: show backup help
				} 
			}
		};
	}
	
	private void initFields(DocearUser user) {
		if(user != null) {
			chckbxCollaboration.setSelected(user.isCollaborationEnabled());
			chckbxRecommendations.setSelected(user.isRecommendationsEnabled());
			chckbxOnlineBackup.setSelected(user.isBackupEnabled());
			chckbxSynchronization.setSelected(user.isSynchronizationEnabled());
		}
		else {
			chckbxCollaboration.setSelected(true);
			chckbxRecommendations.setSelected(true);
			chckbxOnlineBackup.setSelected(true);
			chckbxSynchronization.setSelected(true);
		}
		
	}
	
	public boolean isOnlineBackupEnabled() {
		return chckbxOnlineBackup.isSelected();
	}
	
	public boolean isRecommendationsEnabled() {
		return chckbxRecommendations.isSelected();
	}
	
	public boolean isCollaborationEnabled() {
		return chckbxCollaboration.isSelected();
	}
	
	public boolean isSynchronizationEnabled() {
		return chckbxSynchronization.isSelected();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.wizard.services.title");
	}

	@Override
	public void preparePage(WizardSession context) {
		initFields(context.get(DocearUser.class));
		context.setWizardTitle(getTitle());
		context.getBackButton().setVisible(false);
		context.getNextButton().setText(TextUtils.getRawText("docear.setup.wizard.controls.finish"));
	}
}
