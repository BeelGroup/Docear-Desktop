package org.docear.plugin.core.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.docear.plugin.core.ui.dialog.DocearImportProjectDialogPanel;
import org.docear.plugin.core.workspace.compatible.DocearWorkspaceToProjectConverter;
import org.docear.plugin.core.workspace.controller.DocearConversionDescriptor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler.LOAD_RETURN_TYPE;
import org.freeplane.plugin.workspace.model.WorkspaceModelException;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class DocearImportProjectAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.project.import";
	
	public DocearImportProjectAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		final DocearImportProjectDialogPanel dialog = new DocearImportProjectDialogPanel();
		
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("workspace.action.node.import.project.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(response == JOptionPane.OK_OPTION) {					
			AWorkspaceProject project = dialog.getProject();
			if(project == null) {
				return;
			}
			WorkspaceController.getCurrentModel().addProject(project);
			if(dialog.isConversionNecessary()) {
				try {
					DocearWorkspaceToProjectConverter.convert(project.getExtensions(DocearConversionDescriptor.class));
				} catch (IOException e) {
					LogUtils.severe(e);
				}
				return;
			} 
			
			try {
				LOAD_RETURN_TYPE return_type = WorkspaceController.getCurrentModeExtension().getProjectLoader().loadProject(project);
				if(return_type == LOAD_RETURN_TYPE.NEW_PROJECT && dialog.getProjectName() != null && dialog.getProjectName().length() > 0) {
					project.getModel().changeNodeName(project.getModel().getRoot(), dialog.getProjectName());
				}
			} catch (IOException e) {
				LogUtils.severe(e);
			} catch (WorkspaceModelException e) {
				LogUtils.severe(e);
			}
		}
	}

}
