package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
public class UpdateReferencesAllMapsAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "UpdateReferencesAllMapsAction";

	public UpdateReferencesAllMapsAction() {
		super(KEY);		
	}
	
	public void setEnabled() {
		setEnabled(DocearWorkspaceProject.isCompatible(WorkspaceController.getCurrentProject()));
	}

	
	public void actionPerformed(ActionEvent e) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_all_mindmaps")));
		mindmapUpdateController.updateAllMindmapsInProject();
	}

}
