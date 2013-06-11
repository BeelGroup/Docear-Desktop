package org.docear.plugin.services.features.user;

import java.beans.PropertyChangeListener;

public class DocearLocalUser extends DocearUser {
	public String getName() { 
		return "local";
	}

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getAccessToken() {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean isBackupEnabled() {
		return false;
	}

	@Override
	public boolean isSynchronizationEnabled() {
		return false;
	}

	@Override
	public boolean isRecommendationsEnabled() {
		return false;
	}

	@Override
	public boolean isTransmissionEnabled() {
		return false;
	}

	@Override
	public boolean isOnline() {
		return false;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}
}