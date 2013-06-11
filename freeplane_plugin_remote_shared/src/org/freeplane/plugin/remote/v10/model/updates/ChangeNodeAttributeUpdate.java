package org.freeplane.plugin.remote.v10.model.updates;

public class ChangeNodeAttributeUpdate extends MapUpdate {

	
	private final String nodeId;
	private final String attribute;
	private final Object value;
	

	private ChangeNodeAttributeUpdate() {
		super("","",Type.ChangeNodeAttribute);
		nodeId = "";
		attribute = "";
		value = "";
	}
	
	public ChangeNodeAttributeUpdate(String source, String username, String nodeId, String attribute, Object value) {
		super(source, username, Type.ChangeNodeAttribute);
		
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
