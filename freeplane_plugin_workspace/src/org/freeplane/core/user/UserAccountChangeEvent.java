package org.freeplane.core.user;

public class UserAccountChangeEvent {
	private final Object source;
	private final IUserAccount user;
	
	public UserAccountChangeEvent(Object src, IUserAccount user) {
		this.user = user;
		this.source = src;
	}

	public Object getSource() {
		return source;
	}

	public IUserAccount getUser() {
		return user;
	}
}
