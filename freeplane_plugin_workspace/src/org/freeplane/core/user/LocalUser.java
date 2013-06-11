package org.freeplane.core.user;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class LocalUser implements IUserAccount {
	private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	private final String name;
	private boolean enabled = true;

	public LocalUser(String username) {
		this.name = username;
	}

	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isActive() {
		return this.equals(UserAccountController.getController().getActiveUser());
	}

	public void activate() {
		UserAccountController.getController().setActiveUser(this);
	}

	public void setEnabled(boolean enabled) {
		this.enabled  = enabled;
	}
	
	public String toString() {
		return "LocalUser["+getName()+(isActive() ? ";active":"")+"]";
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
	
	protected void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		firePropertyChangedEvent(evt);
	}
	
	protected void firePropertyChangedEvent(PropertyChangeEvent event) {
		synchronized (listeners) {
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(event);
			}
		}
	}

}
