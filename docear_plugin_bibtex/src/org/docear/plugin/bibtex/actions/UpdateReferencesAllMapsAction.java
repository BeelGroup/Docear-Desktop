package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
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
		try {	
    		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
    		WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
    		setEnabled(DocearWorkspaceProject.isCompatible(modelExt.getProject()) && modelExt.getProject().isLoaded());
		}
		catch (Exception e) {
			setEnabled(false);
		}
	}

	
	public void actionPerformed(ActionEvent e) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_all_mindmaps")));
		mindmapUpdateController.updateAllMindmapsInCurrentMapsProject();
	}
}
