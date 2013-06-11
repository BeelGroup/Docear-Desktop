package org.docear.plugin.core.ui.dialog;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.docear.plugin.core.workspace.controller.DocearConversionDescriptor;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.components.dialog.ImportProjectDialogPanel;

public class DocearImportProjectDialogPanel extends ImportProjectDialogPanel{
	
	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearImportProjectDialogPanel() {
		super();
		setConfirmButton(new Component() {
			private static final long serialVersionUID = 1L;
			private Component confirmButton;

			@Override
			public void setEnabled(boolean b) {
				if(confirmButton == null) {
					findButton(DocearImportProjectDialogPanel.this);
				}
				if(confirmButton != null) {
					confirmButton.setEnabled(b);
				}
			}

			private void findButton(Component dialog) {
				Component parent = dialog.getParent();
				while(parent != null) {
					if(parent instanceof JOptionPane) {
						//WORKSPACE - test: os other than windows7
						for(Component comp : ((JOptionPane) parent).getComponents()) {
							if(comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0 && ((JPanel) comp).getComponent(0) instanceof JButton) {
								confirmButton = ((JPanel) comp).getComponent(0);
							}
						}
					}						
					parent = parent.getParent();
				}
			}
			
		});
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	protected void updateProjectVersions() {
		super.updateProjectVersions();
		lookForIncompatibles();
	}

	private void lookForIncompatibles() {
		File dir = URIUtils.getFile(getProjectPath());
		if(dir != null) {
			dir = DocearConversionDescriptor.getOldProfilesHome(dir);
			if(dir.exists()) {
				for (File profileHome : dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if(pathname.isDirectory()) {
							return true;
						}
						return false;
					}
				})) {
					if(new File(profileHome, "workspace.xml").exists()) {
						String profileName = profileHome.getName();
						DocearWorkspaceProject project = new DocearWorkspaceProject(getProjectPath());
						project.addExtension(new DocearConversionDescriptor(project, profileName));
						getComboBoxModel().addItem(new ConversionItem(project, profileName, new Date(profileHome.lastModified())));
					}
				}
				enableControlls(getComboBoxModel().getSize() > 0);
			}
		}
		
	}
	
	public boolean isConversionNecessary() {
		return getProject().getExtensions(DocearConversionDescriptor.class) != null;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	class ConversionItem extends VersionItem {

		private ConversionItem(DocearWorkspaceProject prj, String name, Date version) {
			super(prj, name, version);
		}
		
		public String toString() {
			return "[convert] " + super.toString(); 
		}
	}
}
