package org.freeplane.plugin.remote.v10.model.updates;

public class MoveNodeUpdate extends MapUpdate {
	private final String newParentNodeId;
	private final String nodetoMoveId;
	private final Integer newIndex;

	@SuppressWarnings("unused")
	private MoveNodeUpdate() {
		this("", "", "", "", 0);
	}

	public MoveNodeUpdate(String source, String username, String newParentNodeId, String nodetoMoveId, Integer newIndex) {
		super(source, username, Type.MoveNode);
		this.newParentNodeId = newParentNodeId;
		this.nodetoMoveId = nodetoMoveId;
		this.newIndex = newIndex;
	}

	public String getNewParentNodeId() {
		return newParentNodeId;
	}

	public String getNodetoMoveId() {
		return nodetoMoveId;
	}

	public Integer getNewIndex() {
		return newIndex;
	}

}
