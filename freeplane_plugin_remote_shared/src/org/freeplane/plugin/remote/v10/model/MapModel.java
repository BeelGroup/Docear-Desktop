package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;

import org.freeplane.features.map.NodeModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class MapModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String id;
	public Boolean isReadonly;
	public NodeModelRoot root;
	public String name;
	public int revision;

	public MapModel() {
	}
	
	public MapModel(org.freeplane.features.map.MapModel freeplaneMap, String name, int revision, boolean autoloadChildren) {
		id = freeplaneMap.getTitle();
		isReadonly = freeplaneMap.isReadOnly();
		this.name = name;
		this.revision = revision;
		
		NodeModel rootNodeFreeplane = freeplaneMap.getRootNode();
		root = new NodeModelRoot(rootNodeFreeplane, autoloadChildren);
	}
	
	public String toJsonString() {
		try {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
		} catch (Exception e) {
			return "";
		}
		//return "{\"id\":\""+id+"\",\"name\":\""+name+"\",\"revision\":\""+revision+"\",\"isReadonly\":\""+isReadonly+"\",\"root\":"+root.toJsonString()+"}";
	}
	
}
