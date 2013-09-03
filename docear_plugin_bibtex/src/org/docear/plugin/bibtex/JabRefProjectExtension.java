package org.docear.plugin.bibtex;

import javax.swing.SwingUtilities;

import net.sf.jabref.gui.MainTable;

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

	public void selectBasePanel() {
        if (getBaseHandle() != null) {
            try {               
                final MainTable table = getBaseHandle().getBasePanel().getMainTable();
                table.setFocusable(false);
                getBaseHandle().showBasePanel();
                
                SwingUtilities.invokeLater(new Runnable() {					
					@Override
					public void run() {
						table.setFocusable(true);
					}
				});
            }
            catch(Exception ex) {
                
            }
        }		
	}
}