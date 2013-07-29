package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class DocearAddRepositoryPathAction extends AWorkspaceAction {
	private static final long serialVersionUID = 1L;
	
	public static final String KEY = "workspace.action.node.add.repository";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearAddRepositoryPathAction() {
		super(KEY);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void setEnabled() {
		setEnabled(DocearWorkspaceProject.isCompatible(WorkspaceController.getSelectedProject()));
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void actionPerformed(ActionEvent e) {
		AWorkspaceProject project = WorkspaceController.getSelectedProject();
		if(DocearWorkspaceProject.isCompatible(project)) {
			FolderTypeLiteratureRepositoryNode litRepoNode = project.getExtensions(FolderTypeLiteratureRepositoryNode.class);
			if(litRepoNode == null) {
				return;
			}
			
			JFileChooser fileChooser = UrlManager.getController().getFileChooser(null, true, true);
			fileChooser.setSelectedFile(URIUtils.getFile(project.getProjectHome()));
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);			
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			
			int retVal = fileChooser.showOpenDialog(UITools.getFrame());
			if (retVal == JFileChooser.APPROVE_OPTION) {			
				File file = fileChooser.getSelectedFile();
				if(file == null) {
					return;
				}
				LiteratureRepositoryPathNode pathNode = new LiteratureRepositoryPathNode();
				pathNode.setPath(project.getRelativeURI(file.toURI()));
				pathNode.setName(file.getName());
				pathNode.setSystem(true);
				project.getModel().addNodeTo(pathNode, litRepoNode);
				
				litRepoNode.refresh();
				
				IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
				if(view != null) {
					view.refreshView();
				}
			}
			
		}

	}
}
