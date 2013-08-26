package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class UpdateReferencesAllOpenMapsAction extends AWorkspaceAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "UpdateReferencesAllOpenMapsAction";

	public UpdateReferencesAllOpenMapsAction() {
		super(KEY);		
	}

	
	public void actionPerformed(ActionEvent e) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		ArrayList<AWorkspaceProject> projects = new ArrayList<AWorkspaceProject>();
		projects.add(WorkspaceController.getMapProject());
		mindmapUpdateController.updateOpenMindmaps(projects);
	}
	
	@Override
	public void setEnabled() {
		try {	
    		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
    		WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
    		setEnabled(modelExt.getProject() != null && modelExt.getProject().isLoaded());
		}
		catch (Exception e) {
			setEnabled(false);
		}
	}

}
