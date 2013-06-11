package org.freeplane.plugin.remote.v10.model.updates;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class MapUpdate {
	public enum Type {
		ChangeNodeAttribute, AddNode, DeleteNode, MoveNode
	}

	private final Type type;
	private final String username;
	private final String source;

	public MapUpdate(String source, String username, Type type) {
		this.type = type;
		this.source = source;
		this.username = username;
	}

	public Type getType() {
		return type;
	}

	public String getUsername() {
		return username;
	}

	public String getSource() {
		return source;
	}

	public String toJson() {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new AssertionError("Could not serialize MapUpdate from type " + this.getClass().getSimpleName());
		}

	}
}
