package org.freeplane.plugin.remote.client;

import java.net.URI;

public class ListenedMapInfo {
	private final User user;
	private final URI uri;
	private final Long projectId;

	public ListenedMapInfo(User user, URI uri, Long projectId) {
		this.user = user;
		this.uri = uri;
		this.projectId = projectId;
	}

	public User getUser() {
		return user;
	}

	public URI getUri() {
		return uri;
	}

	public Long getProjectId() {
		return projectId;
	}

}
