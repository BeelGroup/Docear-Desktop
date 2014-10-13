package org.docear.plugin.services.features.user.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

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

public class LoginPagePanel extends AWizardPage {
	private static final long serialVersionUID = 1L;

	private LabeledTextField txtUsername;
	private LabeledPasswordField pwdPasswd;

	private WizardSession context;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public LoginPagePanel() {
		this(null);
	}
	
	public LoginPagePanel(String message) {
		this(null, message);
	}

	public LoginPagePanel(DocearUser startSettings, String message) {
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

		JLabel lblLogin = new JLabel(TextUtils.getText("docear.setup.wizard.option.login"));
		lblLogin.setFont(lblLogin.getFont().deriveFont(Font.BOLD, 14));
		add(lblLogin, "2, 3, 9, 1");
		
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
					enableButtons(context);
				}
			}
			
			public void keyPressed(KeyEvent e) {
			}
		});
		add(txtUsername, "4, 5, 5, 1, fill, default");
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
					enableButtons(context);
				}
			}
			
			public void keyPressed(KeyEvent e) {
			}
		});
		if (startSettings != null && startSettings.getAccessToken() != null) {
			pwdPasswd.setEnabled(false);
		}
		add(pwdPasswd, "4, 7, 5, 1, fill, default");

		JHyperlink lblForgotPasswd = new JHyperlink(TextUtils.getText("docear.setup.wizard.login.forgot.passwd"), "https://www.docear.org/my-docear/change-password/");
		lblForgotPasswd.setHorizontalAlignment(SwingConstants.RIGHT);
		lblForgotPasswd.setUnderlinedWhenHovered(false);
		add(lblForgotPasswd, "8, 9");
		
		if(message != null) {
			JLabel lblMessage = new JLabel(message);
			add(lblMessage, "2, 11, 9, 1");
		}
		
		prepareFields(startSettings);
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
		if(user != null) {
			txtUsername.setText(user.getName() == null ? "" : user.getName());
			pwdPasswd.setText(user.getPassword() == null ? "" : user.getPassword());
		}
		
	}
	
	private void enableButtons(WizardSession ctxt) {
		DocearUser settings = context.get(DocearUser.class);
		if((getUsername() == null || (getPassword() == null && (settings == null || settings.getAccessToken() == null))) ) {
			ctxt.getNextButton().setEnabled(false);
			getRootPane().setDefaultButton((JButton) ctxt.getBackButton());
		}
		else {
			ctxt.getNextButton().setEnabled(true);
			getRootPane().setDefaultButton((JButton) ctxt.getNextButton());
		}
	}
	
	@Override
	public String getTitle() {
		return TextUtils.getText("docear.setup.wizard.login.title");
	}

	@Override
	public void preparePage(WizardSession context) {
		this.context = context;
		context.getBackButton().setText(TextUtils.getText("docear.setup.wizard.controls.skip2local"));
		context.getBackButton().setEnabled(false);
		
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.login.next"));
		context.setWizardTitle(getTitle());
		context.set(DocearLocalUser.class, null);
		DocearUser user = context.get(DocearUser.class);
		if(user instanceof DocearLocalUser) {
			user = new DocearUser();
			context.set(DocearUser.class, user);
		}
		else {
			String token = user.getAccessToken();
			user = new DocearUser(user);
			context.set(DocearUser.class, user);
			user.setAccessToken(token);
		}
		prepareFields(user);
		enableButtons(context);
	}

}
