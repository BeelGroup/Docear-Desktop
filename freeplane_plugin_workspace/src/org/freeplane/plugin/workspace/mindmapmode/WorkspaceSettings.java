package org.freeplane.plugin.workspace.mindmapmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

import org.freeplane.core.user.IUserAccount;
import org.freeplane.core.user.LocalUser;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.IWorkspaceSettingsHandler;

final class WorkspaceSettings implements IWorkspaceSettingsHandler {
	public static final String WORKSPACE_VIEW_WIDTH = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.width";
	public static final String WORKSPACE_VIEW_ENABLED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.enabled";
	public static final String WORKSPACE_VIEW_COLLAPSED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.collapsed";
	public static final String WORKSPACE_MODEL_PROJECTS = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".model.projects";
	public static final String WORKSPACE_MODEL_PROJECTS_SEPARATOR = ",";
	
	private static final String USER_SETTINGS_FILENAME = "user.settings";
	private Properties properties = new Properties();

	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void load() throws IOException {
		final File userPropertiesFolder = new File(getSettingsPath());
		final File settingsFile = new File(userPropertiesFolder, USER_SETTINGS_FILENAME);
				
		InputStream in = null;
		try {
			in = new FileInputStream(settingsFile);
			properties.load(in);
		}
		catch (final Exception ex) {
			LogUtils.info("Workspace settings not found, create new file");
			setupDefaultSettings();
		}
		finally {
			FileUtils.silentlyClose(in);
		}
		
	}

	public String removeProperty(String key) {
		return (String) properties.remove(key);
	}

	public void store() throws IOException {
		final File userPropertiesFolder = new File(getSettingsPath());
		final File settingsFile = new File(userPropertiesFolder, USER_SETTINGS_FILENAME);
		OutputStream os = null;
		try {
			if(!settingsFile.exists()) {
				settingsFile.getParentFile().mkdirs();
				settingsFile.createNewFile();
			}		
			os = new FileOutputStream(settingsFile);
			properties.store(os, "user settings for the workspace");
		}
		catch (final Exception ex) {
			LogUtils.severe("could not store workspace settings.", ex);
		}
		finally {
			FileUtils.silentlyClose(os);
		}
	}
	
	public String getSettingsPath() {
		IUserAccount user = UserAccountController.getController().getActiveUser();
		if(user == null) {
			user = new LocalUser("local");
			user.activate();
		}
		return URIUtils.getAbsoluteFile(WorkspaceController.getApplicationSettingsHome()).getPath() + File.separator + "users"+File.separator+user.getName();
	}
	
	private void setupDefaultSettings() {
		setProperty(WORKSPACE_VIEW_WIDTH, "150");
		setProperty(WORKSPACE_VIEW_ENABLED, "true");
		setProperty(WORKSPACE_VIEW_COLLAPSED, "false");		
	}
}