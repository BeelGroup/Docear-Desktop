package org.freeplane.plugin.remote.v10.model.updates;

public class DeleteNodeUpdate extends MapUpdate {

	private final String nodeId;

	@SuppressWarnings("unused")
	private DeleteNodeUpdate() {
		this("", "", "");
	}

	public DeleteNodeUpdate(String source, String username, String nodeId) {
		super(source, username, Type.DeleteNode);

		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId;
	}

}
