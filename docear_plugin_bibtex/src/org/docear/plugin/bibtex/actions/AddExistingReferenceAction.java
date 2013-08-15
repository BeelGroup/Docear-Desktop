package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.docear.plugin.bibtex.JabRefProjectExtension;
import org.docear.plugin.bibtex.Reference;
import org.docear.plugin.bibtex.dialogs.ExistingReferencesDialog;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@EnabledAction(checkOnNodeChange=true)
public class AddExistingReferenceAction extends AFreeplaneAction {
	
	public static final String KEY = "AddExistingReferenceAction";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddExistingReferenceAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent arg0) {
		Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		URI link = null;
		String name = null;
		// check for conflicting file links (two nodes linking to at least two distinct files)
		AWorkspaceProject project = null;
		for (NodeModel node : nodes) {
			if(project == null) {
				WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
				if(modelExt != null) {
					project = modelExt.getProject();
					JabRefProjectExtension ext = (JabRefProjectExtension) project.getExtensions(JabRefProjectExtension.class);
					if(ext == null) {
						return;
					}
					ext.selectBasePanel();
				}
			}
			//DOCEAR - ToDo: show error msg
			if(project == null || !project.isLoaded()) {
				return;
			}
			try {				
				URI tempLink = Reference.getBibTeXRelativeURI(NodeLinks.getLink(node), (DocearWorkspaceProject) project);
				String tempName = UrlManager.getController().getAbsoluteFile(node.getMap(), tempLink).getName();

				if (link == null) {
					link = tempLink;
					name = tempName;
				}
				
				if (!tempName.equals(name)) {
					int yesOrNo = JOptionPane.showConfirmDialog(UITools.getFrame(), TextUtils.getText("docear.add_existing_reference.error.conflicting_pdf_files"), TextUtils.getText("docear.add_existing_reference.error.title"),
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					
					
					if (yesOrNo == JOptionPane.YES_OPTION) {
						link = null;
						break;
					}					
					else {
						return;
					}
				}
			}
			catch (NullPointerException ex) {
			}
		}

		ExistingReferencesDialog dialog = new ExistingReferencesDialog(Controller.getCurrentController().getViewController().getFrame(), link);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

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
