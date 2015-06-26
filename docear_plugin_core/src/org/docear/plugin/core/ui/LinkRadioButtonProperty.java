package org.docear.plugin.core.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JRadioButton;

import org.freeplane.core.resources.components.PropertyBean;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class LinkRadioButtonProperty extends PropertyBean {	
	
	JRadioButton button = new JRadioButton();
	LinkLabel htmlLabel;

	public LinkRadioButtonProperty(String name, String text) {
		super(name);
		htmlLabel = new LinkLabel(text);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				if(!button.isSelected()){
					button.setSelected(true);
				}
				else{
					firePropertyChangeEvent();
				}
			}
		});		
	}

	

	@Override
	public void layout(DefaultFormBuilder builder) {
		builder.append(htmlLabel);
		builder.append(button);
	}

	@Override
	public void setEnabled(boolean pEnabled) {
		htmlLabel.setEnabled(pEnabled);
		button.setEnabled(pEnabled);
	}



	@Override
	public String getValue() {
		return button.isSelected() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}



	@Override
	public void setValue(String value) {
		final boolean booleanValue = Boolean.parseBoolean(value);
		setValue(booleanValue);
	}
	
	public void setValue(final boolean booleanValue) {
		button.setSelected(booleanValue);
	}

	public boolean getBooleanValue() {
		return button.isSelected();
	}



	@Override
	protected Component[] getComponents() {
		return new Component[]{button, htmlLabel};
	}

}
