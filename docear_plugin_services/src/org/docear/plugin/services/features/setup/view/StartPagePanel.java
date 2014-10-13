package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.plugin.core.ui.components.LabeledPasswordField;
import org.docear.plugin.core.ui.components.LabeledTextField;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.services.features.user.DocearLocalUser;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.TextUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class StartPagePanel extends AWizardPage {
	public enum START_OPTION {
		LOGIN, REGISTRATION
	}

	private static final long serialVersionUID = 1L;

	private LabeledTextField txtUsername;
	private LabeledPasswordField pwdPasswd;

	private JRadioButton rdbtnLogin;

	private JRadioButton rdbtnRegister;

	private WizardSession context;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public StartPagePanel() {
		this(null);
	}

	public StartPagePanel(DocearUser startSettings) {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("top:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JLabel lblRegister = new JLabel(TextUtils.getText("docear.setup.wizard.option.register"));
		lblRegister.setFont(lblRegister.getFont().deriveFont(Font.BOLD, 14));
		add(lblRegister, "2, 2, 9, 1");

		rdbtnRegister = new JRadioButton(TextUtils.getText("docear.setup.wizard.info.register"));
		rdbtnRegister.setBackground(Color.WHITE);
		rdbtnRegister.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				updateOption();
				enableButtons();
			}
		});
		add(rdbtnRegister, "4, 4, 7, 1");

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "6, 5, 5, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
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
				FormFactory.RELATED_GAP_ROWSPEC,}));

		JLabel label = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.1"));
		label.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(label, "2, 1");

		JLabel lbl2 = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.2"));
		lbl2.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(lbl2, "2, 3");

		JLabel label_1 = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.3"));
		label_1.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(label_1, "2, 5");

		JLabel label_2 = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.4"));
		label_2.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(label_2, "2, 7");

		JLabel label_3 = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.5"));
		label_3.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(label_3, "2, 9");

		JLabel label_4 = new JLabel(TextUtils.getText("docear.setup.wizard.feature.text.6"));
		label_4.setIcon(new ImageIcon(StartPagePanel.class.getResource("/icons/emblem-default.png")));
		panel.add(label_4, "2, 11");

		JLabel lblLogin = new JLabel(TextUtils.getText("docear.setup.wizard.option.login"));
		lblLogin.setFont(lblLogin.getFont().deriveFont(Font.BOLD, 14));
		add(lblLogin, "2, 7, 9, 1");

		rdbtnLogin = new JRadioButton("");
		rdbtnLogin.setBackground(Color.WHITE);
		rdbtnLogin.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				updateOption();
				enableButtons();
			}
		});
		add(rdbtnLogin, "4, 9");

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnRegister);
		group.add(rdbtnLogin);

		
		txtUsername = new LabeledTextField(TextUtils.getText("docear.setup.wizard.login.input.label.user")); //new JTextField();
		if (startSettings != null && startSettings.getName() != null) {
			txtUsername.setText(startSettings.getName());
		}
		txtUsername.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		txtUsername.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (context != null) {
					DocearUser settings = context.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						context.set(DocearUser.class, settings);
					}
					settings.setUsername(getUsername());
					enableButtons();
				}
			}
			
			public void keyPressed(KeyEvent e) {
				rdbtnLogin.setSelected(true);
			}
		});
		add(txtUsername, "6, 9, 3, 1, fill, default");
		txtUsername.setColumns(10);

		pwdPasswd = new LabeledPasswordField(TextUtils.getText("docear.setup.wizard.login.input.label.passwd"));
		pwdPasswd.setBorder(new LineBorder(Color.DARK_GRAY, 1));
		pwdPasswd.setText("");
		pwdPasswd.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {}
			
			public void keyReleased(KeyEvent e) {
				if (context != null) {
					DocearUser settings = context.get(DocearUser.class);
					if (settings == null) {
						settings = new DocearUser();
						context.set(DocearUser.class, settings);
					}
					settings.setPassword(getPassword());
					enableButtons();
				}
			}
			
			public void keyPressed(KeyEvent e) {
				rdbtnLogin.setSelected(true);				
			}
		});
		if (startSettings != null && startSettings.getAccessToken() != null) {
			pwdPasswd.setEnabled(false);
		}
		add(pwdPasswd, "6, 11, 3, 1, fill, default");

		JHyperlink lblForgotPasswd = new JHyperlink(TextUtils.getText("docear.setup.wizard.login.forgot.passwd"), "http://www.docear.org/faqs/i-forgot-my-password-andor-username-what-can-i-do/");
		lblForgotPasswd.setHorizontalAlignment(SwingConstants.RIGHT);
		lblForgotPasswd.setUnderlinedWhenHovered(false);
		add(lblForgotPasswd, "8, 13");
		
		prepareFields(startSettings);
	}

	public boolean isRegistrationOption() {
		return rdbtnRegister.isSelected();
	}

	public boolean isLoginOption() {
		return rdbtnLogin.isSelected();
	}

	public String getUsername() {
		String name = txtUsername.getText();
		if (name.isEmpty()) {
			return null;
		}
		return name;
	}

	public String getPassword() {
		String pw = String.copyValueOf(pwdPasswd.getPassword());
		if (pw.isEmpty()) {
			return null;
		}
		return pw;
	}
	
	private void prepareFields(DocearUser user) {
		txtUsername.setText("");
		pwdPasswd.setText("");
		
		if(user == null) {
			rdbtnRegister.setSelected(true);
		}
		else if(user.isValid() || (user.isNew() && user.getPassword() != null)) {
			txtUsername.setText(user.getName() == null ? "" : user.getName());
			pwdPasswd.setText(user.getPassword() == null ? "" : user.getPassword());
			rdbtnLogin.setSelected(user.getName() != null);
		}
		
	}
	
	private void updateOption() {
		if (context != null) {
			if(isRegistrationOption()) {
				context.set(START_OPTION.class, START_OPTION.REGISTRATION);
			}
			else if (isLoginOption()) {
				context.set(START_OPTION.class, START_OPTION.LOGIN);
			}
		}
	}
	
	private void enableButtons() {
		if(context != null) {
			DocearUser settings = context.get(DocearUser.class);
			if(isLoginOption() && (getUsername() == null || (getPassword() == null && (settings == null || settings.getAccessToken() == null))) ) {
				context.getNextButton().setEnabled(false);
				getRootPane().setDefaultButton((JButton) context.getBackButton());
			}
			else {
				context.getNextButton().setEnabled(true);
				getRootPane().setDefaultButton((JButton) context.getNextButton());
			}
		}
	}
	
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.start.title");
	}

	@Override
	public void preparePage(WizardSession context) {
		this.context = context;
		context.getBackButton().setText(TextUtils.getText("docear.setup.wizard.controls.skip2local"));
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.next"));
		context.setWizardTitle(getTitle());
		context.set(DocearLocalUser.class, null);
		DocearUser user = context.get(DocearUser.class);
		if(user instanceof DocearLocalUser) {
			user = new DocearUser();
			context.set(DocearUser.class, user);
		}
//		else {
//			String token = user.getAccessToken();
//			user = new DocearUser(user);
//			context.set(DocearUser.class, user);
//			user.setAccessToken(token);
//		}
		prepareFields(user);
		updateOption();
		enableButtons();
	}

}
