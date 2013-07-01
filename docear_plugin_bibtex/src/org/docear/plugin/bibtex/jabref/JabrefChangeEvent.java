package org.docear.plugin.bibtex.jabref;

import java.util.EventObject;

import net.sf.jabref.BasePanel;

public class JabrefChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	private final BasePanel basePanel;

	public JabrefChangeEvent(Object source, BasePanel basePanel) {
		super(source);
		this.basePanel = basePanel;
	}

	public BasePanel getBasePanel() {
		return basePanel;
	}

	
}
