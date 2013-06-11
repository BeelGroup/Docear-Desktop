package org.freeplane.plugin.workspace.features;

import java.io.IOException;

public interface IWorkspaceSettingsHandler {
	
	public String getProperty(String key, String defaultValue);
	
	public String getProperty(String key);

	public void setProperty(String key, String value);

	public String removeProperty(String key);
	
	public void load() throws IOException;

	public String getSettingsPath();

	public void store() throws IOException;
}
