package org.freeplane.plugin.remote.v10.model.updates;

import com.fasterxml.jackson.databind.JsonNode;

public class AddNodeUpdate extends MapUpdate {
	public enum Side {
		Left, Right
	}

	private final String parentNodeId;
	private final String newNodeId;
	private final JsonNode nodeAsJson;
	private final Side side;

	// Do not remove! Needed for jackson (in testing project)
	@SuppressWarnings("unused")
	private AddNodeUpdate() {
		this("","","", "", null);
	}

	/**
	 * Constructor for default nodes (don't need side)
	 */
	public AddNodeUpdate(String source, String username, String parentNodeId, String newNodeId, JsonNode nodeAsJson) {
		this(source, username, parentNodeId, newNodeId, nodeAsJson, null);
	}

	/**
	 * For node that is added to root node
	 * 
	 * @param side
	 *            only required at root node level
	 */
	public AddNodeUpdate(String source, String username, String parentNodeId, String newNodeId, JsonNode nodeAsJson, Side side) {
		super(source, username, Type.AddNode);
		this.parentNodeId = parentNodeId;
		this.nodeAsJson = nodeAsJson;
		this.side = side;
		this.newNodeId = newNodeId;
	}

	public Side getSide() {
		return side;
	}

	public JsonNode getNodeAsJson() {
		return nodeAsJson;
	}

	public String getParentNodeId() {
		return parentNodeId;
	}

	public String getNewNodeId() {
		return newNodeId;
	}

}
