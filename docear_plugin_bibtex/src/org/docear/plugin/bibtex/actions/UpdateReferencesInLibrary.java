package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;


import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;

public class UpdateReferencesInLibrary extends AFreeplaneAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "menu_update_references_in_library";

	public UpdateReferencesInLibrary() {
		super(KEY);		
	}
	
	public void setEnabled() {
		setEnabled(DocearWorkspaceProject.isCompatible(WorkspaceController.getCurrentProject()));
	}

	public void actionPerformed(ActionEvent e) {		
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_library_mindmaps")));
		mindmapUpdateController.updateRegisteredMindmapsInProject();			
		
	}

}
