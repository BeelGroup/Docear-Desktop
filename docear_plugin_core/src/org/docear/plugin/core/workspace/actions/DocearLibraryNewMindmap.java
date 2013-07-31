/**
 * author: Marcel Genzmehr
 * 30.01.2012
 */
package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.actions.WorkspaceNewMapAction;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class DocearLibraryNewMindmap extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearLibraryNewMindmap() {
		super("workspace.action.library.new.mindmap", TextUtils.getRawText("workspace.action.library.new.mindmap.label"), DefaultFileNode.getApplicationIcon());
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void actionPerformed(final ActionEvent e) {
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		AWorkspaceTreeNode targetNode = this.getNodeFromActionEvent(e);
		if (targetNode instanceof FolderTypeLibraryNode) {
			String fileName = JOptionPane.showInputDialog(Controller.getCurrentController().getViewController().getContentPane(),
					TextUtils.getText("add_new_mindmap"), TextUtils.getText("add_new_mindmap_title"), JOptionPane.OK_CANCEL_OPTION);
			
			if (fileName != null && fileName.length() > 0) {
				if (!FileUtils.isFilenameValid(fileName)) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("file.invalid_characters"), TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
					actionPerformed(e);
					return;
				}
				if (!fileName.endsWith(".mm")) {
					fileName += ".mm";
				}
				try {
					DocearWorkspaceProject project = (DocearWorkspaceProject) WorkspaceController.getSelectedProject(targetNode);
					File parentFolder = URIUtils.getFile(project.getProjectLibraryPath());
					File file = new File(parentFolder, fileName);
					try {
						if (file.exists()) {
							file = WorkspaceController.getFileSystemMgr().createFile(fileName, parentFolder);
						}
						
						if (createNewMindmap(project, file.toURI()) != null) {
							LinkTypeFileNode newNode = new LinkTypeFileNode();
							newNode.setLinkURI(project.getRelativeURI(file.toURI()));
							newNode.setName(FilenameUtils.getBaseName(file.getName()));
							targetNode.getModel().addNodeTo(newNode, targetNode);
							targetNode.refresh();
							WorkspaceNewMapAction.openMap(file.toURI());
						}
					}
					catch (Exception ex) {
						// WORKSPACE - todo: prepare for headless
						JOptionPane.showMessageDialog(UITools.getFrame(), ex.getMessage(), TextUtils.getText("error"), JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception ex) {
					LogUtils.severe("could not find library paht", ex);
				}

			}
		}
	}

	private MapModel createNewMindmap(DocearWorkspaceProject project, final URI uri) {
		String name = FilenameUtils.getBaseName(URIUtils.getAbsoluteFile(uri).getName());
		return WorkspaceNewMapAction.createNewMap(project, uri, name, true);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
