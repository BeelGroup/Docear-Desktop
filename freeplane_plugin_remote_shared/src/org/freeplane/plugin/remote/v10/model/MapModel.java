package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.freeplane.features.map.NodeModel;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
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
