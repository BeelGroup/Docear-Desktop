package org.freeplane.plugin.remote.v10.model;

import java.awt.Color;
import java.io.Serializable;

import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.NodeModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class EdgeModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public Integer width;
	public Integer color;
	public EdgeStyle style;
	
	
	protected EdgeModel() {
		
	}
	
	public EdgeModel(NodeModel freeplaneNode) {
		MEdgeController edgeController = (MEdgeController)MEdgeController.getController();
		this.width = edgeController.getWidth(freeplaneNode, false);
		final Color color = edgeController.getColor(freeplaneNode, false);
		this.color = color != null ? color.getRGB() : null;
		this.style = edgeController.getStyle(freeplaneNode, false);
	}
	
	public boolean areAllValuesNull() {
		return width == null && color == null && style == null;
	}
}
