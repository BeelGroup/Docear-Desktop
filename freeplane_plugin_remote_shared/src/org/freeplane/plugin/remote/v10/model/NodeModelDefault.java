package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodelocation.LocationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class NodeModelDefault extends NodeModelBase implements Serializable{
	private static final long serialVersionUID = 1L;

	
	public List<NodeModelDefault> children;

	public EdgeModel edgeStyle;
	public Integer hGap;
	public Integer shiftY;
	public List<String> attributes;

	/**
	 * necessary for JAX-B
	 */
	public NodeModelDefault() {
		super();
	}
	
	public NodeModelDefault(NodeModel freeplaneNode, boolean autoloadChildren) {
		super(freeplaneNode,autoloadChildren);
		
		loadLocation(freeplaneNode);
		loadAttributes(freeplaneNode);
		loadEdgeStyle(freeplaneNode);
		
	}
	
	private void loadEdgeStyle(NodeModel freeplaneNode) {
		this.edgeStyle = new EdgeModel(freeplaneNode);
		if(edgeStyle.areAllValuesNull())
			edgeStyle = null;
	}
	
	private void loadLocation(org.freeplane.features.map.NodeModel freeplaneNode) {
		LocationModel l = freeplaneNode.getExtension(LocationModel.class);
		if(l != null) {
			hGap = l.getHGap();
			shiftY = l.getShiftY();
		} else {
			hGap = 0;
			shiftY = 0;
		}
	}
	
	private void loadAttributes(org.freeplane.features.map.NodeModel freeplaneNode){
		NodeAttributeTableModel attributeModel = freeplaneNode.getExtension(NodeAttributeTableModel.class);
		if(attributeModel != null) {
			this.attributes = new ArrayList<String>();
			for (Attribute attribute : attributeModel.getAttributes()){
				this.attributes.add(attribute.getName()+"%:%"+String.valueOf(attribute.getValue()));
			}
		} else {
			attributes = null;
		}
	}

	@Override
	public int loadChildren(boolean autoloadChildren) {
		children = new ArrayList<NodeModelDefault>();
		
		int totalCount = childrenIds.size();
		for(NodeModel freeplaneChild : getFreeplaneNode().getChildren()) {
			children.add(new NodeModelDefault(freeplaneChild,false));
		}
		
		if(autoloadChildren) {
			for(NodeModelBase child : this.children) {
				totalCount += child.loadChildren(true);
			}
		}
		
		childrenIds = null;
		return totalCount;
	}
	

	@Override
	@JsonIgnore
	public List<NodeModelDefault> getAllChildren() {
		return children;
	}
	
//	public String toJsonString() {
//		try {
//		final ObjectMapper mapper = new ObjectMapper();
//		return mapper.writeValueAsString(this);
//		} catch (Exception e) {
//			return "";
//		}
//		String childrenList = "";
//		if(children != null && children.size() > 0) {
//			for(DefaultNodeModel node : children) {
//				childrenList += ","+node.toJsonString();
//			}
//			childrenList = childrenList.substring(1);
//		}
//		
//		
//		StringBuilder builder = new StringBuilder();
//		builder.append("{"+getJsonStringParts()+",");
//		if(children != null) {
//			builder.append("\"children\":["+childrenList+"],");
//		}
//		builder.append("\"attributes\":\"NOT IMPLEMENTED\",");
//		builder.append("\"hGap\":\""+hGap+"\",");
//		builder.append("\"shiftY\":\""+shiftY+"\"");
//		builder.append("}");
//		
//		//return "{}"
//		return builder.toString();
//	}

}
