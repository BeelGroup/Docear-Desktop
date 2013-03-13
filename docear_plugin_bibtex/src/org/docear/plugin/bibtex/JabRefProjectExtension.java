package org.docear.plugin.bibtex;

import org.docear.plugin.bibtex.jabref.JabRefBaseHandle;
import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;

public class JabRefProjectExtension implements IWorkspaceProjectExtension {
	private JabRefBaseHandle handle = null;
	
	public JabRefProjectExtension(JabRefBaseHandle handle) {
		setBaseHandle(handle);
	}

	public void setBaseHandle(JabRefBaseHandle handle) {
		this.handle = handle;
	}
	
	public JabRefBaseHandle getBaseHandle() {
		return this.handle;
	}

}