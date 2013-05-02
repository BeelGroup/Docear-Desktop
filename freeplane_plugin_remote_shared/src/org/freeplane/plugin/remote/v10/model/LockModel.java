package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;

import org.freeplane.core.extension.IExtension;

public class LockModel implements IExtension, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private long lastAccess;
	
	public LockModel() {
		
	}
	
	public LockModel(String username, long lastAccess) {
		super();
		this.username = username;
		this.lastAccess = lastAccess;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public long getLastAccess() {
		return lastAccess;
	}
	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}
	
	
}
