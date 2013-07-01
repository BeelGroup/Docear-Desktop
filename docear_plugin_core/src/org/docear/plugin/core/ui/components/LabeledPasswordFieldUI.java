package org.docear.plugin.core.ui.components;

import javax.swing.JPasswordField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.View;

public class LabeledPasswordFieldUI extends LabeledTextFieldUI {

	@Override
	protected boolean isLabelVisible() {
		return (!getComponent().isFocusOwner() && String.copyValueOf(((JPasswordField) getComponent()).getPassword()).trim().length() == 0);
	}

	protected String getPropertyPrefix() {
		return "PasswordField";
	}

	protected void installDefaults() {
		super.installDefaults();
		String prefix = getPropertyPrefix();
		Character echoChar = (Character) UIManager.getDefaults().get(prefix + ".echoChar");
		if (echoChar != null) {
			LookAndFeel.installProperty(getComponent(), "echoChar", echoChar);
		}
	}

	public View create(Element elem) {
		return new PasswordView(elem);
	}
}
