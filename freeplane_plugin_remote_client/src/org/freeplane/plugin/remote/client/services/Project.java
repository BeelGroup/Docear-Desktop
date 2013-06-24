package org.freeplane.plugin.remote.client.services;

import java.util.List;

public class Project {
	private String id;
	private String name;
	private Long revision;
	private List<String> authorizedUsers;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getRevision() {
		return revision;
	}
	public void setRevision(Long revision) {
		this.revision = revision;
	}
	public List<String> getAuthorizedUsers() {
		return authorizedUsers;
	}
	public void setAuthorizedUsers(List<String> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}
}
