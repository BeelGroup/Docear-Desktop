package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.docear.plugin.core.mindmap.MindmapFileRemovedUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@EnabledAction(checkOnNodeChange=true)
public class DeleteFileAction extends DocearAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeleteFileAction() {
		super("LinkDeleteFileFromDiskAction");
	}

	public void actionPerformed(ActionEvent event) {
		Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		if(selection == null){
			return;
		}
		
		Set<File> deletedFiles = new HashSet<File>();
		MapModel map = null;
		Integer response = null;
		for (NodeModel node : selection) {
			URI uri = URIUtils.getAbsoluteURI(node);
			if (uri == null) {
				continue;
			}
			if(map == null) {
				map = node.getMap();
			}
			if(response == null) {
				response = Wizard.showConfirmDialog(TextUtils.getText("DeleteFileAction.confirm.text"));
			}
			if(response == Wizard.OK_OPTION) {
				File file = URIUtils.getFile(uri);
				if(!file.delete()){
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("DeleteFileAction.DeleteFailed.Message"), TextUtils.getText("DeleteFileAction.DeleteFailed.Title"), JOptionPane.WARNING_MESSAGE);
					return;
				}			
				deletedFiles.add(file);
			}
			else {
				return;
			}
			
		}
				
		MindmapUpdateController ctrl = new MindmapUpdateController();
		ctrl.addMindmapUpdater(new MindmapFileRemovedUpdater(TextUtils.getText("docear.mm_updater.remove_filelinks"), deletedFiles));
		ArrayList<AWorkspaceProject> projects = new ArrayList<AWorkspaceProject>();
		projects.add(WorkspaceController.getMapProject(map));
		ctrl.updateRegisteredMindmapsInProject(projects, true);

	}
	
	@Override
	public void setEnabled(){
		if(Controller.getCurrentController().getSelection() == null) {
			this.setEnabled(false);
			return;
		}
		Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		if(selection == null){
			this.setEnabled(false);			
		}
		else{
			for(NodeModel selected : selection){
				if(MonitoringUtils.isPdfLinkedNode(selected)){
					this.setEnabled(true);
					return;
				}
			}
			this.setEnabled(false);
		}
	}

}
