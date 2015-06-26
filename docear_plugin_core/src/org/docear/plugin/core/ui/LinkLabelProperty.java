package org.docear.plugin.core.ui;

import org.freeplane.core.resources.components.IPropertyControl;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class LinkLabelProperty implements IPropertyControl {	
	
	LinkLabel htmlLabel;
	
	public LinkLabelProperty(String html){
		htmlLabel = new LinkLabel(html);		
	}

	@Override
	public String getDescription() {		
		return null;
	}

	@Override
	public String getName() {		
		return null;
	}

	@Override
	public void layout(DefaultFormBuilder builder) {		
		builder.append(htmlLabel);		
		builder.nextLine();
	}

	@Override
	public void setEnabled(boolean pEnabled) {
		htmlLabel.setEnabled(pEnabled);
	}
}
