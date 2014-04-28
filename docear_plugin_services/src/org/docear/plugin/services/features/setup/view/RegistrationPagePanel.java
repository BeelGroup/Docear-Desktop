package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.components.DocearLicencePanel;
import org.docear.plugin.core.ui.components.LabeledPasswordField;
import org.docear.plugin.core.ui.components.LabeledTextField;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.components.dialog.NewProjectDialogPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class RegistrationPagePanel extends AWizardPage {
	
	private static final long serialVersionUID = 1L;
	private JTextField txtUsername;
	private JTextField txtEmail;
	private JPasswordField pwdPassword;
	private JPasswordField pwdRetypedPassword;
	private JCheckBox chckbxAcceptTOS;
	private JCheckBox chckbxNewsletter;
	private JCheckBox chckbxAcceptUsageTerms;
	private WizardSession cachedContext;
	private JLabel lblPwdWarning;
	private JLabel lblRetypeWarning;
	private JCheckBox chckbxCollaboration;
	private JCheckBox chckbxOnlineBackup;
	private JCheckBox chckbxSynchronization;
	private JCheckBox chckbxRecommendations;
	private JPanel lblProcessingTerms;
	private JPanel lblToS;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private JLabel lblReclabel;
	private JLabel lblIconRecommendations;
	private JLabel lblBackup;
	private JLabel lblIconBackup;
	private MultiLineActionLabel multiLineActionLabel;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RegistrationPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.UNRELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("12dlu:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.PARAGRAPH_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.PARAGRAPH_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default"),
				RowSpec.decode("top:default"),
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		URL url = NewProjectDialogPanel.class.getResource("/images/16x16/dialog-warning-4.png");
		
		JLabel lblRequiredInformation = new JLabel(TextUtils.getText("docear.setup.wizard.register.required"));
		lblRequiredInformation.setFont(lblRequiredInformation.getFont().deriveFont(Font.BOLD, 11));
		add(lblRequiredInformation, "2, 2, 4, 1");
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "3, 4, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("left:12dlu"),
				ColumnSpec.decode("max(109dlu;default):grow"),
				ColumnSpec.decode("left:40dlu"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		txtUsername = new LabeledTextField(TextUtils.getText("docear.setup.wizard.register.input.label.user"));
		txtUsername.setColumns(15);
		txtUsername.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		txtUsername.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setUsername(getUsername());
					enableControls(cachedContext);
				}
			}
			
			public void keyPressed(KeyEvent e) {}
		});
		panel.add(txtUsername, "1, 1, fill, default");
		
		txtEmail = new LabeledTextField(TextUtils.getText("docear.setup.wizard.register.input.label.email"));
		txtEmail.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		txtEmail.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setEmail(getEmail());
					enableControls(cachedContext);
				}
			}
			
			public void keyPressed(KeyEvent e) {}
		});
		panel.add(txtEmail, "3, 1, fill, default");
		
		pwdPassword = new LabeledPasswordField(TextUtils.getText("docear.setup.wizard.register.input.label.passwd"));
		pwdPassword.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		pwdPassword.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setPassword(getPassword());
					enableControls(cachedContext);
				}
			}
			
			public void keyPressed(KeyEvent e) {}
		});
		pwdPassword.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
			
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		panel.add(pwdPassword, "1, 3, fill, default");
		
		lblPwdWarning = new JLabel();
		if(url != null) {
			lblPwdWarning.setIcon(new ImageIcon(url));
		}
		panel.add(lblPwdWarning, "2, 3, right, default");
		lblPwdWarning.setVisible(false);
		lblPwdWarning.setToolTipText(TextUtils.getText("docear.setup.wizard.register.input.warn.passwd"));
		
		pwdRetypedPassword = new LabeledPasswordField(TextUtils.getText("docear.setup.wizard.register.input.label.passwdretype"));
		pwdRetypedPassword.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		pwdRetypedPassword.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
			
			public void keyPressed(KeyEvent e) {}
		});
		panel.add(pwdRetypedPassword, "3, 3, fill, default");
		
		lblRetypeWarning = new JLabel();		
		if(url != null) {
			lblRetypeWarning.setIcon(new ImageIcon(url));
		}
		panel.add(lblRetypeWarning, "4, 3");
		lblRetypeWarning.setVisible(false);
		lblRetypeWarning.setToolTipText(TextUtils.getText("docear.setup.wizard.register.input.warn.passwd"));
		
		JLabel lblServicesoptional = new JLabel(TextUtils.getText("docear.setup.wizard.register.services"));
		lblServicesoptional.setFont(lblServicesoptional.getFont().deriveFont(Font.BOLD, 11));
		add(lblServicesoptional, "2, 6, 4, 1");
		
		panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		add(panel_1, "3, 8, 3, 1, fill, fill");
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
		chckbxRecommendations.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setRecommendationsEnabled(chckbxRecommendations.isSelected());
				}
			}
		});
		
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
		chckbxSynchronization.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setSynchronizationEnabled(chckbxSynchronization.isSelected());
				}
			}
		});
		
		chckbxOnlineBackup = new JCheckBox();
		panel_1.add(chckbxOnlineBackup, "1, 2, default, top");
		chckbxOnlineBackup.setSelected(true);
		chckbxOnlineBackup.setBackground(Color.WHITE);
		chckbxOnlineBackup.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setBackupEnabled(chckbxOnlineBackup.isSelected());
				}
			}
		});
		
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
		chckbxCollaboration.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setCollaborationEnabled(chckbxCollaboration.isSelected());
				}
			}
		});
		
		JLabel lblAndTheFine = new JLabel(TextUtils.getText("docear.setup.wizard.docear.terms.title"));
		lblAndTheFine.setFont(lblAndTheFine.getFont().deriveFont(Font.BOLD, 11));
		add(lblAndTheFine, "2, 11, 4, 1");
		
		chckbxAcceptUsageTerms = new JCheckBox();
		chckbxAcceptUsageTerms.setBackground(Color.WHITE);
		chckbxAcceptUsageTerms.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		add(chckbxAcceptUsageTerms, "3, 13, default, top");
		
		final DocearLicencePanel licenseText = new DocearLicencePanel();
		
		lblProcessingTerms = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.docear.terms.processing"));
		lblProcessingTerms.setBackground(Color.WHITE);
		((MultiLineActionLabel) lblProcessingTerms).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("top".equals(e.getActionCommand())) {
					licenseText.setLicenceText(DocearController.getController().getDataProcessingTerms());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.data_processing.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
				}
			}
		});
		add(lblProcessingTerms, "5, 13, fill, fill");
		
		chckbxAcceptTOS = new JCheckBox();
		chckbxAcceptTOS.setBackground(Color.WHITE);
		chckbxAcceptTOS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		
		
		add(chckbxAcceptTOS, "3, 14, default, top");
		
		lblToS = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.docear.terms.service"));
		lblToS.setBackground(Color.WHITE);
		((MultiLineActionLabel) lblToS).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("tos".equals(e.getActionCommand())) {
					licenseText.setLicenceText(DocearController.getController().getTermsOfService());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.terms_of_use.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
				if("dps".equals(e.getActionCommand())) {
					licenseText.setLicenceText(DocearController.getController().getDataPrivacyTerms());
					JOptionPane.showConfirmDialog(getRootPane(), licenseText, TextUtils.getText("docear.license.data_privacy.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
			}
		});
		add(lblToS, "5, 14, fill, fill");
		
		chckbxNewsletter = new JCheckBox();
		chckbxNewsletter.setBackground(Color.WHITE);
		chckbxNewsletter.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					DocearUser settings = cachedContext.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						cachedContext.set(DocearUser.class, settings);
					}
					settings.setNewsletterEnabled(chckbxNewsletter.isSelected());
				}
			}
		});
		add(chckbxNewsletter, "3, 16, default, top");
		
		multiLineActionLabel = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.register.newsletter"));
		multiLineActionLabel.setBackground(Color.WHITE);
		add(multiLineActionLabel, "5, 16, fill, fill");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void enableControls(WizardSession context) {
		if(context != null) {
			if(comparePasswords() 
					&& chckbxAcceptUsageTerms.isSelected() 
					&& chckbxAcceptTOS.isSelected() 
					&& txtUsername.getText().trim().length() > 0 
					&& pwdPassword.getPassword().length > 0 
					&& txtEmail.getText().trim().length() > 0 
					&& pwdRetypedPassword.getPassword().length > 0) {
				
				context.getNextButton().setEnabled(true);
			}
			else {
				context.getNextButton().setEnabled(false);
			}
		}
		
	}
	
	private boolean comparePasswords() {
		lblPwdWarning.setVisible(false);
		lblRetypeWarning.setVisible(false);
		if(getPassword() == null && getComparePassword() == null) {
			return true;
		}
		else if(getPassword() != null && getPassword().equals(getComparePassword())) {	
			return true;
		}
		//mark both input fields with warning and tooltip text
		lblPwdWarning.setVisible(true);
		lblRetypeWarning.setVisible(true);
		return false;
	}
	
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
		txtEmail.setText("");
		txtUsername.setText("");
		pwdPassword.setText("");
		pwdRetypedPassword.setText("");
		
		if(user != null) {
			txtUsername.setText(user.getUsername() == null ? "" : user.getUsername());
			txtEmail.setText(user.getEmail() == null ? "" : user.getEmail());
		}
		else {
			chckbxAcceptTOS.setSelected(false);
			chckbxAcceptUsageTerms.setSelected(false);
			chckbxNewsletter.setSelected(false);
			chckbxCollaboration.setSelected(true);
			chckbxRecommendations.setSelected(true);
			chckbxOnlineBackup.setSelected(true);
			chckbxSynchronization.setSelected(true);
		}
		
	}
	
	public String getUsername() {
		String name = txtUsername.getText();
		if (name.isEmpty()) {
			return null;
		}
		return name;
	}

	public String getPassword() {
		String pw = String.copyValueOf(pwdPassword.getPassword());
		if (pw.isEmpty()) {
			return null;
		}
		return pw;
	}
	
	private String getComparePassword() {
		String pw = String.copyValueOf(pwdRetypedPassword.getPassword());
		if (pw.isEmpty()) {
			return null;
		}
		return pw;
	}
	
	public String getEmail() {
		String mail = txtEmail.getText();
		if (mail.isEmpty()) {
			return null;
		}
		return mail;
	}
	
	public boolean isNewsletterEnabled() {
		return chckbxNewsletter.isSelected();
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
	
	public boolean isTermsAccepted() {
		return chckbxAcceptTOS.isSelected() && chckbxAcceptUsageTerms.isSelected();
	}
	
	public DocearUser getUser() {
		DocearUser user = cachedContext.get(DocearUser.class);
		user.setBackupEnabled(isOnlineBackupEnabled());
		user.setCollaborationEnabled(isCollaborationEnabled());
		user.setRecommendationsEnabled(isRecommendationsEnabled());
		user.setSynchronizationEnabled(isSynchronizationEnabled());
		return user;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.registration.title");
	}

	@Override
	public void preparePage(WizardSession context) {
		this.cachedContext = context;
		initFields(context.get(DocearUser.class));
		setSkipOnBack(false);
		context.setWizardTitle(getTitle());
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.register.button"));
		enableControls(context);
	}
}
