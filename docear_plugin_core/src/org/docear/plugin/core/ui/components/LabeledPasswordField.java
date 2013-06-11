package org.docear.plugin.core.ui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;

public class LabeledPasswordField extends JPasswordField implements ILabeledComponent {
	
	private static final long serialVersionUID = 1L;
	private String labelText;
	
	public LabeledPasswordField(String labelText) {
		super();
		setLabelText(labelText);
		this.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				repaint();
			}
			
			public void focusGained(FocusEvent e) {
				repaint();
			}
		});
	}
	
	@Override
	public void updateUI() {
		if(!echoCharIsSet()) {
			super.updateUI();
		}
		setUI(new LabeledPasswordFieldUI());
		invalidate();
	}

	@Override
	public String getLabelText() {
		return labelText;
	}
	
	public void setLabelText(String text) {
		this.labelText = text == null ? "" : text;
	}
}