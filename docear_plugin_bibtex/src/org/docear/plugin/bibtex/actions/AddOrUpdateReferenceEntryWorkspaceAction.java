package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.tree.TreePath;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

@EnabledAction(checkOnNodeChange=true)
@CheckEnableOnPopup
public class AddOrUpdateReferenceEntryWorkspaceAction extends AWorkspaceAction {
	public static final String KEY = "workspace.action.addOrUpdateReferenceEntry";

	public AddOrUpdateReferenceEntryWorkspaceAction() {
		super(KEY);
	}
	private static final long serialVersionUID = 1L;

	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		File file = null;
		if(node instanceof IFileSystemRepresentation) {
			file = ((IFileSystemRepresentation) node).getFile();
		}
		else {
			if(node instanceof LinkTypeFileNode) {				
				file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
			}
		}
		
		if(file == null || !file.getName().toLowerCase().endsWith(".pdf") /*|| (AnnotationController.getDocumentHash(file.toURI()) == null)*/) {
			setEnabled(false);
			return;
		}
		
		super.setEnabledFor(node, selectedPaths);
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
	
	public void actionPerformed(ActionEvent e) {
		JabrefWrapper jabrefWrapper = ReferencesController.getController().getJabrefWrapper();
		AWorkspaceTreeNode node = getNodeFromActionEvent(e);
		File file = null;
		if(node instanceof IFileSystemRepresentation) {
			file = ((IFileSystemRepresentation) node).getFile();
		}
		else {
			if(node instanceof LinkTypeFileNode) {				
				file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
			}
		}
		
		if(jabrefWrapper != null && file != null) {
			JabRefCommons.addNewRefenceEntry(new String[] { file.getPath() }, jabrefWrapper.getJabrefFrame(), jabrefWrapper.getJabrefFrame().basePanel());
		}

	}

}
