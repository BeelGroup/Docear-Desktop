package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;


import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class UpdateReferencesInLibrary extends AFreeplaneAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "UpdateReferencesInLibraryAction";

	public UpdateReferencesInLibrary() {
		super(KEY);		
	}
	
	public void setEnabled() {
		setEnabled(DocearWorkspaceProject.isCompatible(WorkspaceController.getMapProject()));
	}

	public void actionPerformed(ActionEvent e) {		
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_library_mindmaps")));
		ArrayList<AWorkspaceProject> projects = new ArrayList<AWorkspaceProject>();
		projects.add(WorkspaceController.getMapProject());
		mindmapUpdateController.updateRegisteredMindmapsInProject(projects);			
		
	}

}
