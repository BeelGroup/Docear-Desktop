package org.freeplane.core.user;

import java.beans.PropertyChangeListener;

public interface IUserAccount {
	public String getName();
	public boolean isEnabled();
	public boolean isActive();
	public void activate();
	public void setEnabled(boolean enabled);
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
