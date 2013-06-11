package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.freeplane.features.map.NodeModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class NodeModelRoot extends NodeModelBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//@XmlElement(required=true,nillable=true)
	@XmlElement(name="leftChildren")
	public List<NodeModelDefault> leftChildren;
	//@XmlElement(required=true)
	@XmlElement(name="rightChildren")
	public List<NodeModelDefault> rightChildren;

	//public NodeModel preferredChild;
	
	
	/**
	 * necessary for JAX-B
	 */
	@SuppressWarnings("unused")
	private NodeModelRoot() {
		super();
	}
	
	/**
	 * automatically converts the whole tree
	 * @param freeplaneNode
	 */
	public NodeModelRoot(org.freeplane.features.map.NodeModel freeplaneNode, boolean autoloadChildren) {
		super(freeplaneNode,autoloadChildren);
	}
	
	
	
	@Override
	public int loadChildren(boolean autoloadChildren) {
		leftChildren = new ArrayList<NodeModelDefault>();
		rightChildren = new ArrayList<NodeModelDefault>();
		
		int totalCount = childrenIds.size();
		for(NodeModel child : getFreeplaneNode().getChildren()) {
			if(child.isLeft()) {
				this.leftChildren.add(new NodeModelDefault(child,false));
			} else {
				this.rightChildren.add(new NodeModelDefault(child,false));
			}
		}
		
		if(autoloadChildren) {
			for(NodeModelDefault child : this.leftChildren) {
				totalCount += child.loadChildren(true);
			}
			for(NodeModelDefault child : this.rightChildren) {
				totalCount += child.loadChildren(true);
			}
		}
			
		childrenIds = null;
		return totalCount;
	}

	@Override
	@JsonIgnore
	public List<NodeModelDefault> getAllChildren() {
		List<NodeModelDefault> list = new ArrayList<NodeModelDefault>(leftChildren);
		list.addAll(rightChildren);
		return list;
	}

//	public String toJsonString() {
//		StringBuilder builder = new StringBuilder();
//		builder.append("{"+getJsonStringParts()+",\"leftChildren\":[");
//		for(int i = 0; i < leftChildren.size(); i ++) {
//			builder.append(leftChildren.get(i).toJsonString());
//			if(i < leftChildren.size()-1)
//				builder.append(",");
//		}
//		builder.append("],\"rightChildren\":[");
//		for(int i = 0; i < rightChildren.size(); i ++) {
//			builder.append(rightChildren.get(i).toJsonString());
//			if(i < rightChildren.size()-1)
//				builder.append(",");
//		}
//		builder.append("]}");
//		return builder.toString();
//	}
}
