package org.freeplane.core.user;

public interface IUserAccountChangeListener {
	public void aboutToDeactivate(UserAccountChangeEvent event);
	public void activated(UserAccountChangeEvent event);
}
