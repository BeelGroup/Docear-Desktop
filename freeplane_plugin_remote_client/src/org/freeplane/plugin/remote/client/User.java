package org.freeplane.plugin.remote.client;

import org.apache.commons.lang.Validate;

public class User {
	private final String accessToken;
	private final String username;
	
	public User(final String username, final String accessToken) {
		Validate.notNull(username);
		Validate.notNull(accessToken);
		
		this.accessToken = accessToken;
		this.username = username;
	}

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "accessToken='" + accessToken + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
