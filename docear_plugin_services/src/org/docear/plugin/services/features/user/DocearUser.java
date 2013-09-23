package org.docear.plugin.services.features.user;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.user.IUserAccount;
import org.freeplane.core.user.UserAccountController;

public class DocearUser implements IUserAccount, Cloneable {
	public static final String RECOMMENDATIONS_PROPERTY = "RECOMMENDATIONS";
	public static final String SYNCHRONIZATION_PROPERTY = "SYNCHRONIZATION";
	public static final String BACKUP_PROPERTY = "BACKUP";
	public static final String COLLABORATION_PROPERTY = "COLLABORATION";
	public static final String ACCESS_TOKEN_PROPERTY = "ACCESS_TOKEN";
	public static final String TRANSMISSION_PROPERTY = "TRANSMISSION";
	public static final String NEWSLETTER_PROPERTY = "NEWSLETTER";
	public static final String IS_VALID_PROPERTY = "IS_VALID";
	public static final String IS_ONLINE_PROPERTY = "IS_ONLINE";
	public static final String ENABLED_PROPERTY = "ENABLED";
	public static final String EMAIL_PROPERTY = "EMAIL";
	public static final String PASSWORD_PROPERTY = "PASSWORD";
	public static final String USERNAME_PROPERTY = "USERNAME";
	
	public static final int COLLABORATION = 64;
	public static final int SYNCHRONIZATION = 32;	
	public static final int BACKUP = 16;
	public static final int RECOMMENDATIONS = 8;
	
	public static final int ALLOW_USAGE_MINING = 4;
	public static final int ALLOW_INFORMATION_RETRIEVAL = 2;
	public static final int ALLOW_RESEARCH = 1;
	
	
	private String username = null;
	private String email = null;
	private String password = null;
	
	private String accessToken = null;
	
	private boolean collaborationEnabled = false;
	private boolean recommendationsEnabled = false;
	private boolean synchronizationEnabled = false;
	private boolean backupEnabled = false;
	private boolean transmissionEnabled = true;
	private boolean isNewsletterEnabled = false;
	
	private boolean valid = false;
	private transient List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	private boolean isOnline;
	private boolean enabled = true;
	private boolean isNew;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearUser() {
	}
	
	public DocearUser(IUserAccount user) {
		this();
		if(user != null) {
			setUsername(user.getName());
		}
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void setUsername(String name) {
		String oldValue = this.username;
		if(oldValue != name) {
			this.username = name;			
			firePropertyChanged(USERNAME_PROPERTY, oldValue, name);
			setValid(false);
		}
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setPassword(String password) {
		String oldValue = this.password;
		if(oldValue != password) {
			this.password = password;
			firePropertyChanged(PASSWORD_PROPERTY, oldValue, password);
			setValid(false);
		}
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setEmail(String email) {
		String oldValue = this.email;
		if(oldValue != email) {
			this.email = email;
			firePropertyChanged(EMAIL_PROPERTY, oldValue, email);
			setValid(false);
		}
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setAccessToken(String token) {
		String oldValue = this.accessToken;
		if(token == null || token.trim().isEmpty()) {
			this.accessToken = null;
			firePropertyChanged(ACCESS_TOKEN_PROPERTY, oldValue, accessToken);
			setValid(false);
		}
		else {
			this.accessToken = token;
			firePropertyChanged(ACCESS_TOKEN_PROPERTY, oldValue, accessToken);
			setOnline(true);
			setValid(true);
		}
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	private void setValid(boolean valid) {
		if(this.valid != valid) {
			this.valid = valid;
			if(!valid) {
				this.accessToken = null;
			}
			firePropertyChanged(IS_VALID_PROPERTY, valid, !valid);
		}
	}
	
	public void setCollaborationEnabled(boolean selected) {
		if(collaborationEnabled != selected) {
			collaborationEnabled = selected;
			firePropertyChanged(COLLABORATION_PROPERTY, selected, !selected);
		}
	}
	
	public void setBackupEnabled(boolean selected) {
		if(backupEnabled != selected) {
			backupEnabled = selected;
			firePropertyChanged(BACKUP_PROPERTY, selected, !selected);
		}
	}

	public void setSynchronizationEnabled(boolean selected) {
		if(synchronizationEnabled != selected) {
			synchronizationEnabled = selected;
			firePropertyChanged(SYNCHRONIZATION_PROPERTY, selected, !selected);
		}
	}

	public void setRecommendationsEnabled(boolean selected) {
		if(recommendationsEnabled != selected) {
			recommendationsEnabled = selected;
			firePropertyChanged(RECOMMENDATIONS_PROPERTY, selected, !selected);
		}
	}
	
	private void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
		//if(isActive()) //DOCEAR - restrict to active settings only?
		{
			PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
			synchronized (listeners) {
				for(PropertyChangeListener listener : listeners) {
					listener.propertyChange(evt);
				}
			}
		}
	}

	public boolean isCollaborationEnabled() {
		return collaborationEnabled;
	}
	
	public boolean isBackupEnabled() {
		return backupEnabled;
	}

	public boolean isSynchronizationEnabled() {
		return synchronizationEnabled;
		
	}

	public boolean isRecommendationsEnabled() {
		return recommendationsEnabled;
	}
	
	public int getEnabledServicesCode() {
		int code = 0;
		if (isRecommendationsEnabled()) {
			code += DocearUser.RECOMMENDATIONS;
		}
		if (isBackupEnabled()) {
			code += DocearUser.BACKUP;
		}
		if (isSynchronizationEnabled()) {
			code += DocearUser.SYNCHRONIZATION;
		}
		if (isCollaborationEnabled()) {
			code += DocearUser.COLLABORATION;
		}
		
		return code;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			if(!listeners.contains(listener)) {
				listeners.add(0, listener);
			}
		}
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public boolean isTransmissionEnabled() {
		return this.transmissionEnabled;
	}
	
	public void toggleTransmissionEnabled() {
		this.transmissionEnabled = !this.transmissionEnabled;
		firePropertyChanged(TRANSMISSION_PROPERTY, transmissionEnabled, !transmissionEnabled);
	}
	
	public void setOnline(boolean online) {
		if(isOnline != online) {
			isOnline = online;
			firePropertyChanged(IS_ONLINE_PROPERTY, online, !online);
		}	
	}
	
	public boolean isOnline() {
		return this.isOnline;
	}
	
	public void setNewsletterEnabled(boolean enabled) {
		if(isNewsletterEnabled != enabled) {
			isNewsletterEnabled = enabled;
			firePropertyChanged(NEWSLETTER_PROPERTY, enabled, !enabled);
		}
	}
	
	public boolean isNewsletterEnabled() {
		return false;
	}
	
	protected void setNew() {
		this.isNew = true;		
	}
	
	public boolean isNew() {
		return isNew;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof DocearUser) {
			if(getUsername() != null) {				
				return getUsername().equals(((DocearUser) obj).getUsername())  && (getAccessToken() == ((DocearUser) obj).getAccessToken());
			}
			
		}
		
		return super.equals(obj);
	}
	
	public DocearUser clone() {
		DocearUser user;
		try {
			user = (DocearUser) super.clone();
			return user;
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException();
		}
	}
	
	public String toString() {
		return "DocearUser[name="+getName() + ";token="+getAccessToken()+"]";
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public String getName() {
		return this.username;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isActive() {
		return this.equals(UserAccountController.getController().getActiveUser());
	}

	public void activate() {
		UserAccountController.getController().setActiveUser(this);
	}

	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			this.enabled  = enabled;
			firePropertyChanged(ENABLED_PROPERTY, enabled, !enabled);
		}
	}
	
}
