package org.docear.plugin.core.workspace.controller;

import java.io.File;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;

public class DocearConversionDescriptor implements IWorkspaceProjectExtension {
	
	public static final String OLD_WORKSPACE_URL_HANDLE = "workspace";
	private final DocearWorkspaceProject target;
	private final String profileName;
	private boolean deleteOldSettings = false;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearConversionDescriptor(DocearWorkspaceProject project, String selectedProfile) {
		if(project == null || selectedProfile == null) {
			throw new IllegalArgumentException("NULL");
		}
		this.target = project;
		this.profileName = selectedProfile;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public DocearWorkspaceProject getTargetProject() {
		return target;
	}
	
	public String getSelectedProfile() {
		return profileName;
	}
	
	public File getSelectedProfileHome() {
		File profileHome = new File(getOldProfilesHome(URIUtils.getFile(target.getProjectHome())), getSelectedProfile());
		if(profileHome.exists()) {
			return profileHome;
		}
		return null;
	}
	
	public static File getOldProfilesHome(File projectHome) {
		File profilesHome = new File(projectHome, "_data/profiles/");
		return profilesHome;
	}
	
	public void setDeleteOldSettings(boolean selected) {
		deleteOldSettings = selected;
	}
	
	public boolean deleteOldSettings() {
		return deleteOldSettings;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	

	
}
