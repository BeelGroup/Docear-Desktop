package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class UpdateReferencesCurrentMapAction extends AWorkspaceAction implements IDocearEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "UpdateReferencesCurrentMapAction";

	public UpdateReferencesCurrentMapAction() {
		super(KEY);
		DocearController.getController().getEventQueue().addEventListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		perform(true);
	}
	
	public void perform(boolean showDialog) {
		new SaveAll().actionPerformed(null);
		
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(showDialog);
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
		mindmapUpdateController.updateCurrentMindmap();
	}

	@Override
	public void handleEvent(DocearEvent event) {
		if (DocearEventType.UPDATE_MAP.equals(event.getType())) {
			ReferenceUpdater updater = new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps"));
			updater.updateMindmap((MapModel) event.getEventObject());
		}
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
