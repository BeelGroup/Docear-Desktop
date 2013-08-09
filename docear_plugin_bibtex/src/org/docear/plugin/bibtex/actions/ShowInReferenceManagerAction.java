package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.bibtex.JabRefProjectExtension;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class ShowInReferenceManagerAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "ShowInRefManagerAction";
	
	
	public ShowInReferenceManagerAction() {
		super(KEY);
	}
	
	public void setEnabled() {
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if (node == null) {
			setEnabled(false);
			return;
		}
		AWorkspaceProject project = WorkspaceController.getMapProject(node.getMap());
		if(project == null || !project.isLoaded()) {
			setEnabled(false);
		}
		else {
			final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
			
			if (bibtexKey != null && bibtexKey.length()>0) {
				setEnabled(true);
			}
			else {
				setEnabled(false);
			}
		}
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if (node == null) {
			return;
		}
		WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
		if(modelExt != null) {
			AWorkspaceProject project = modelExt.getProject();
			JabRefProjectExtension ext = (JabRefProjectExtension) project.getExtensions(JabRefProjectExtension.class);
			ext.selectBasePanel();
		}
		if(ReferencesController.getController().getJabrefWrapper().getBasePanel().getSelectedEntries().length <= 1) {
			final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
			JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
			OneTouchCollapseResizer resizer = wrapper.getResizer();
			if(resizer != null && !resizer.isExpanded()) {
				resizer.setExpanded(true);
			}
			JabRefCommons.showInReferenceManager(bibtexKey);		
		}
		
	}
	
	

}
