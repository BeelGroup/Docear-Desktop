package org.freeplane.plugin.remote.v10.model.updates;

public class ChangeEdgeAttributeUpdate extends MapUpdate {

	
	private final String nodeId;
	private final String attribute;
	private final Object value;
	

	private ChangeEdgeAttributeUpdate() {
		super("","",Type.ChangeEdgeAttribute);
		nodeId = "";
		attribute = "";
		value = "";
	}
	
	public ChangeEdgeAttributeUpdate(String source, String username, String nodeId, String attribute, Object value) {
		super(source, username, Type.ChangeEdgeAttribute);
		
		this.nodeId = nodeId;
		this.attribute = attribute;
		this.value = value;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getAttribute() {
		return attribute;
	}

	public Object getValue() {
		return value;
	}
}
