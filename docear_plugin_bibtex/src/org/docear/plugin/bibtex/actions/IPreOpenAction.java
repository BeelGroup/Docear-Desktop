package org.docear.plugin.bibtex.actions;

import java.io.File;

public interface IPreOpenAction {
	public boolean isActionNecessary(File file);
	
	public void performAction(File file);
}
