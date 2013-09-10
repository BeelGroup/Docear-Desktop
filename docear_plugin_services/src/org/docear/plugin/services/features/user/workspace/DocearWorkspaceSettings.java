package org.docear.plugin.services.features.user.workspace;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.features.user.DocearUserController;
import org.freeplane.core.user.IUserAccountChangeListener;
import org.freeplane.core.user.UserAccountChangeEvent;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.IWorkspaceSettingsHandler;
import org.freeplane.plugin.workspace.mindmapmode.MModeWorkspaceController;

public class DocearWorkspaceSettings extends ADocearServiceFeature implements IWorkspaceSettingsHandler {
	public static final String WORKSPACE_VIEW_WIDTH = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.width";
	public static final String WORKSPACE_VIEW_ENABLED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.enabled";
	public static final String WORKSPACE_VIEW_COLLAPSED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.collapsed";
	public static final String WORKSPACE_MODEL_PROJECTS = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".model.projects";
	public static final String WORKSPACE_MODEL_PROJECTS_SEPARATOR = ",";
	
	public final static String DOCEAR_CONNECTION_TOKEN_PROPERTY = "docear.service.connect.token";
	
	private static final String USER_SETTINGS_FILENAME = "user.settings";
	private Properties properties = new Properties();
	private IWorkspaceSettingsHandler wrappedHandler;
	private PropertyChangeListener userPropertyListener;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private void setupDefaultSettings() {
		setProperty(WORKSPACE_VIEW_WIDTH, "150");
		setProperty(WORKSPACE_VIEW_ENABLED, "true");
		setProperty(WORKSPACE_VIEW_COLLAPSED, "false");
	}
	
	private IUserAccountChangeListener getUserChangeListener() {
		return new IUserAccountChangeListener() {
			
			public void activated(UserAccountChangeEvent event) {
				if(event.getUser() instanceof DocearUser) {
//					try {
//						load((DocearUser) event.getUser());
//					} catch (IOException e) {
//						LogUtils.warn("Exception in org.docear.plugin.services.features.user.workspace.DocearWorkspaceSettings.getUserChangeListener().new IUserAccountChangeListener() {...}.activated(event): "+ e.getMessage());
//					}
					event.getUser().addPropertyChangeListener(getUserPropertyChangeListener());
				}
			}

			public void aboutToDeactivate(UserAccountChangeEvent event) {
				if(event.getUser() instanceof DocearUser) {
					try {
						store((DocearUser) event.getUser());
					} catch (IOException e) {
						LogUtils.warn("Exception in org.docear.plugin.services.features.user.workspace.DocearWorkspaceSettings.getUserChangeListener().new IUserAccountChangeListener() {...}.aboutToDeactivate(event): "+ e.getMessage());
					}
					event.getUser().removePropertyChangeListener(getUserPropertyChangeListener());
				}
			}
		};
	}
	
	private PropertyChangeListener getUserPropertyChangeListener() {
		if(userPropertyListener == null) {
			userPropertyListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if(evt.getSource() instanceof DocearUser) {
						DocearUser user = ((DocearUser)evt.getSource());
						if(DocearUser.USERNAME_PROPERTY.equals(evt.getPropertyName())) {
							
						}
						else if(DocearUser.IS_VALID_PROPERTY.equals(evt.getPropertyName())) {
							if(user.isValid()) {
								properties.setProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY, user.getAccessToken());
							}
							else {
								properties.setProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY, "");
							}
						}
					}
				}
			};
		}
		return userPropertyListener;
	}
	
	public IWorkspaceSettingsHandler getWrappedHandler() {
		return wrappedHandler;
	}
	public static final String DOCEAR_INFORMATION_RETRIEVAL = "docear_information_retrieval";
	public static final String DOCEAR_SAVE_BACKUP = "docear_save_backup";

	private void storeUser(DocearUser user) {
		if(user.isValid()) {
			properties.setProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY, user.getAccessToken());
		}
		else {
			properties.setProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY, "");
		}
		if(user.getEnabledServicesCode() > 0) {
			properties.setProperty(DOCEAR_INFORMATION_RETRIEVAL, String.valueOf(user.getEnabledServicesCode()));
		}
		else {
			properties.setProperty(DOCEAR_INFORMATION_RETRIEVAL, "0");
		}
		if(user.isBackupEnabled()) {
			properties.setProperty(DOCEAR_SAVE_BACKUP, Boolean.toString(user.isBackupEnabled()));
		}
	}
	
	private void loadUser(DocearUser user) {
		String token = properties.getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
		if(token == null) {
			token = DocearController.getPropertiesController().getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
		}
		user.setAccessToken(token);
		String strRetrieval = properties.getProperty(DOCEAR_INFORMATION_RETRIEVAL);
		if(strRetrieval == null) {
			strRetrieval = DocearController.getPropertiesController().getProperty(DOCEAR_INFORMATION_RETRIEVAL);
		}
		if(strRetrieval != null) {
			int ir = Integer.parseInt(strRetrieval);
			user.setRecommendationsEnabled((DocearUser.RECOMMENDATIONS & ir) > 0);
			user.setCollaborationEnabled(((DocearUser.COLLABORATION & ir) > 0));
			user.setBackupEnabled(((DocearUser.BACKUP & ir) > 0));
			user.setSynchronizationEnabled(((DocearUser.SYNCHRONIZATION & ir) > 0));
		}
		if(DocearController.getPropertiesController().getProperty(DOCEAR_SAVE_BACKUP) != null) {
			user.setBackupEnabled(Boolean.parseBoolean(DocearController.getPropertiesController().getProperty(DOCEAR_SAVE_BACKUP)));
		}
//		WorkspaceController.load();
	}
	
	public void loadSettings(DocearUser user) throws IOException {
		if(user == null) {
			throw new IOException("user is NULL");
		}
		final File userPropertiesFolder = new File(getSettingsPath(user));
		final File settingsFile = new File(userPropertiesFolder, USER_SETTINGS_FILENAME);
		
		InputStream in = null;
		try {
			properties = new Properties();
			in = new FileInputStream(settingsFile);
			properties.load(in);
			loadUser(user);
		}
		catch (final Exception ex) {
			LogUtils.info("Workspace settings not found, creating new default settings");
			setupDefaultSettings();
		}
		finally {
			FileUtils.silentlyClose(in);
		}
	}
	
	public void store(DocearUser user) throws IOException {
		if(user == null) {
			throw new IOException("user is NULL");
		}
		final File userPropertiesFolder = new File(getSettingsPath(user));
		final File settingsFile = new File(userPropertiesFolder, USER_SETTINGS_FILENAME);
		storeUser(user);
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
	
	public String getSettingsPath(DocearUser user) {
		return URIUtils.getAbsoluteFile(WorkspaceController.getApplicationSettingsHome()).getPath() + File.separator + "users" + File.separator+user.getName();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	protected void installDefaults(ModeController modeController) {
		MModeWorkspaceController ctrl = ((MModeWorkspaceController)WorkspaceController.getModeExtension(modeController));
		wrappedHandler = ctrl.getWorkspaceSettings();
		ctrl.setWorkspaceSettings(this);
		UserAccountController.getController().addUserAccountChangeListener(getUserChangeListener());
	}

	@Override
	public void shutdown() {
		try {
			store();
		} catch (IOException e) {
			LogUtils.warn("Exception in org.docear.plugin.services.features.user.workspace.DocearWorkspaceSettings.shutdown(): "+ e.getMessage());
		}
	}

	public String getProperty(String key, String defaultValue) {
		String value = properties.getProperty(key);
//		if(value == null && wrappedHandler != null) {
//			value = wrappedHandler.getProperty(key, defaultValue);
//		}
		if(value == null) {
			return defaultValue;
		}
		return value;
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public String removeProperty(String key) {
		return (String) properties.remove(key);
	}
	
	public void load() throws IOException {
		loadSettings(DocearUserController.getActiveUser());
	}

	public void store() throws IOException {
		store(DocearUserController.getActiveUser());
	}
	
	public String getSettingsPath() {
		return this.getSettingsPath(DocearUserController.getActiveUser());
	}
}
