package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.components.LabeledPasswordField;
import org.docear.plugin.core.ui.components.LabeledTextField;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardContext;
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
	private WizardContext cachedContext;
	private JLabel lblPwdWarning;
	private JLabel lblRetypeWarning;
	private JCheckBox chckbxCollaboration;
	private JCheckBox chckbxOnlineBackup;
	private JCheckBox chckbxSynchronization;
	private JCheckBox chckbxRecommendations;
	private JPanel lblPrivacyTerms;
	private JPanel lblToS;
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

	public RegistrationPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
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
				RowSpec.decode("fill:default"),
				RowSpec.decode("fill:default"),
				FormFactory.UNRELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		URL url = NewProjectDialogPanel.class.getResource("/images/16x16/dialog-warning-4.png");
		
		JLabel lblRequiredInformation = new JLabel(TextUtils.getText("docear.setup.wizard.register.required"));
		lblRequiredInformation.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblRequiredInformation, "2, 2, 3, 1");
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "2, 4, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
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
		panel.add(txtUsername, "2, 1, fill, default");
		
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
		panel.add(txtEmail, "4, 1, fill, default");
		
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
		panel.add(pwdPassword, "2, 3, fill, default");
		
		lblPwdWarning = new JLabel();
		if(url != null) {
			lblPwdWarning.setIcon(new ImageIcon(url));
		}
		panel.add(lblPwdWarning, "3, 3, right, default");
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
		panel.add(pwdRetypedPassword, "4, 3, fill, default");
		
		lblRetypeWarning = new JLabel();		
		if(url != null) {
			lblRetypeWarning.setIcon(new ImageIcon(url));
		}
		panel.add(lblRetypeWarning, "5, 3");
		lblRetypeWarning.setVisible(false);
		lblRetypeWarning.setToolTipText(TextUtils.getText("docear.setup.wizard.register.input.warn.passwd"));
		
		JLabel lblServicesoptional = new JLabel(TextUtils.getText("docear.setup.wizard.register.services"));
		lblServicesoptional.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblServicesoptional, "2, 6, 3, 1");
		
		panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		add(panel_1, "2, 8, 3, 1, fill, fill");
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
		panel_1.add(chckbxRecommendations, "1, 1");
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
					settings.setRecommandationsEnabled(chckbxRecommendations.isSelected());
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
		panel_2.add(lblIconRecommendations, "3, 1");
		
		chckbxSynchronization = new JCheckBox(TextUtils.getText("docear.setup.wizard.register.feature.synchronization"));
		panel_1.add(chckbxSynchronization, "5, 1");
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
		panel_1.add(chckbxOnlineBackup, "1, 2");
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
		panel_3.add(lblIconBackup, "3, 1");
		
		chckbxCollaboration = new JCheckBox(TextUtils.getText("docear.setup.wizard.register.feature.collaboration"));
		panel_1.add(chckbxCollaboration, "5, 2");
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
		
		JLabel lblAndTheFine = new JLabel(TextUtils.getText("docear.setup.wizard.register.terms.title"));
		lblAndTheFine.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblAndTheFine, "2, 11, 3, 1");
		
		chckbxAcceptUsageTerms = new JCheckBox();
		chckbxAcceptUsageTerms.setBackground(Color.WHITE);
		chckbxAcceptUsageTerms.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		add(chckbxAcceptUsageTerms, "2, 13");
		
		lblPrivacyTerms = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.register.terms.privacy"));
		lblPrivacyTerms.setBackground(Color.WHITE);
		add(lblPrivacyTerms, "4, 13, fill, fill");
		
		chckbxAcceptTOS = new JCheckBox();
		chckbxAcceptTOS.setBackground(Color.WHITE);
		chckbxAcceptTOS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (cachedContext != null) {
					enableControls(cachedContext);
				}
			}
		});
		
		
		add(chckbxAcceptTOS, "2, 14");
		
		lblToS = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.register.terms.service"));
		lblToS.setBackground(Color.WHITE);
		add(lblToS, "4, 14, fill, fill");
		
		chckbxNewsletter = new JCheckBox(TextUtils.getText("docear.setup.wizard.register.newsletter"));
		chckbxNewsletter.setBackground(Color.WHITE);
		add(chckbxNewsletter, "2, 16, 3, 1");
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void enableControls(WizardContext context) {
		if(context != null) {
			if(comparePasswords() 
					&& chckbxAcceptUsageTerms.isSelected() 
					&& chckbxAcceptTOS.isSelected() 
					&& txtUsername.getText().trim().length() > 0 
					&& pwdPassword.getPassword().length > 0 
					&& txtEmail.getText().trim().length() > 0 
					&& pwdRetypedPassword.getPassword().length > 0) {
				
				context.getNextButton().setEnabled(true);
				//lblAdvice.setForeground(new Color(0x00000000, true));
			}
			else {
				
				context.getNextButton().setEnabled(false);
				//lblAdvice.setForeground(new Color(0xFFFF0000, true));
			}
		}
		
	}
	
	private boolean comparePasswords() {
		lblPwdWarning.setVisible(false);
		lblRetypeWarning.setVisible(false);
		if(getPassword() == null && getComparePassword() == null) {
			return true;
		}
		else if(getParent() != null && getPassword().equals(getComparePassword())) {	
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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.registration.title");
	}

	@Override
	public void preparePage(WizardContext context) {
		this.cachedContext = context;
		context.setWizardTitle(getTitle());
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.register.button"));
		enableControls(context);
	}
	
}
