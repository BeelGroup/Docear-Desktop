package org.freeplane.plugin.remote;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.docear.messages.exceptions.NodeNotFoundException;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.MindIconFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.remote.v10.model.NodeModelBase;

public final class RemoteUtils {

	public static void loadNodesIntoModel(NodeModelBase node, int nodeCount) {
		LinkedList<NodeModelBase> nodeQueue = new LinkedList<NodeModelBase>();
		nodeQueue.add(node);
		while (nodeCount > 0 && !nodeQueue.isEmpty()) {
			NodeModelBase curNode = nodeQueue.pop();

			nodeCount -= curNode.loadChildren(false);
			for (NodeModelBase child : curNode.getAllChildren()) {
				nodeQueue.add(child);
			}
		}
	}

	public static NodeModel addNodeToOpenMap(MMapController mapController, NodeModel parentNode) {
		// logger().debug("Actions.addNode => creating new node");
		NodeModel node = mapController.newNode("", parentNode.getMap());

		// insert node
		// logger().debug("Actions.addNode => inserting new node");
		mapController.insertNode(node, parentNode);

		node.createID();
		// logger().debug("Actions.addNode => node with id '{}' successfully created",
		// node.getID());
		return node;
	}

	public static NodeModel getNodeFromOpenMapById(MMapController mapController, final String nodeId) throws NodeNotFoundException {
		// logger().debug("Actions.getNodeFromOpenMapById => nodeId: {}",
		// nodeId);
		final NodeModel freeplaneNode = mapController.getNodeFromID(nodeId);

		if (freeplaneNode == null) {
			// logger().error("Actions.getNodeFromOpenMapById => requested node not found; throwing exception");
			throw new NodeNotFoundException("Node with id '" + nodeId + "' not found.");
		}

		return freeplaneNode;
	}

	public static void changeNodeAttribute(MMapController mapController, NodeModel freeplaneNode, String attribute, String valueObj) {
		System.out.println("attribute: " + attribute);
		if (attribute.equals("folded")) {
			final Boolean value = Boolean.parseBoolean(valueObj);
			freeplaneNode.setFolded(value);
		} else if (attribute.equals("isHtml")) {
			final Boolean isHtml = Boolean.parseBoolean(valueObj);
			if (isHtml) {
				freeplaneNode.setXmlText(freeplaneNode.getText());
			} else {
				freeplaneNode.setText(freeplaneNode.getText());
			}
		} else if (attribute.equals("attributes")) {
			// remove current extension, because everything is written new
			if (freeplaneNode.getExtension(NodeAttributeTableModel.class) != null)
				freeplaneNode.removeExtension(NodeAttributeTableModel.class);

			final String[] parts = valueObj.split("%:%");
			

			NodeAttributeTableModel attrTable;
			MAttributeController attrController = MAttributeController.getController();

			if (parts.length > 0) {
				attrTable = attrController.createAttributeTableModel(freeplaneNode);

				for (int i = 0; i < parts.length; i+=2) {
					attrController.performInsertRow(attrTable, i/2, parts[i], parts[i+1]);
				}
				freeplaneNode.addExtension(attrTable);
			}
		} else if (attribute.equals("hGap")) {
			updateLocationModel(mapController, freeplaneNode, Integer.parseInt(valueObj), null);
		} else if (attribute.equals("shiftY")) {
			updateLocationModel(mapController, freeplaneNode, null, Integer.parseInt(valueObj));
		} else if (attribute.equals("icons")) {
			final String[] icons = valueObj.toString().split("%:%");
			
			final MIconController iconCtrl = (MIconController)MIconController.getController();
			iconCtrl.removeAllIcons(freeplaneNode);
			
			for(String icon: icons) {
				iconCtrl.addIcon(freeplaneNode, new MindIcon(icon));
			}
		} else if (attribute.equals("image")) {
			
			// TODO handle (https://github.com/Docear/HTW-Frontend/issues/437)
		} else if (attribute.equals("link")) {
			final String value = valueObj;

			NodeLinks nodeLinks = freeplaneNode.getExtension(NodeLinks.class);

			if (nodeLinks == null) {
				nodeLinks = new NodeLinks();
				freeplaneNode.addExtension(nodeLinks);
			}

			try {
				nodeLinks.setHyperLink(new URI(value));
			} catch (URISyntaxException e) {
				LogUtils.severe("problem saving hyperlink", e);
			}
		} else if (attribute.equals("nodeText")) {
			MTextController.getController().setNodeObject(freeplaneNode, valueObj);
		} else if (attribute.equals("note")) {
			final MNoteController noteController = (MNoteController) MNoteController.getController();
			if (valueObj == null) {
				noteController.setNoteText(freeplaneNode, "");
			} else {
				final String noteText = valueObj.toString();
				noteController.setNoteText(freeplaneNode, noteText);
			}
		}
		freeplaneNode.fireNodeChanged(new NodeChangeEvent(freeplaneNode, "node_text", "", ""));
	}

	public static void changeEdgeAttribute(NodeModel freeplaneNode, String attribute, String valueObj) {
		final MEdgeController mEdgeController = (MEdgeController) MEdgeController.getController();
		System.out.println("attribute: " + attribute);
		if (attribute.equals("color")) {
			if (valueObj == null) {
				mEdgeController.setColor(freeplaneNode, null);
			} else {
				final Integer colorInt = Integer.parseInt(valueObj.toString());
				final Color color = new Color(colorInt);
				mEdgeController.setColor(freeplaneNode, color);
			}
		} else if (attribute.equals("width")) {
			if (valueObj == null) {
				mEdgeController.setWidth(freeplaneNode, EdgeModel.DEFAULT_WIDTH);
			} else {
				final Integer width = Integer.parseInt(valueObj.toString());
				mEdgeController.setWidth(freeplaneNode, width);
			}
		} else if (attribute.equals("style")) {
			if (valueObj == null) {
				mEdgeController.setStyle(freeplaneNode, null);
			} else {
				final String styleString = valueObj.toString();
				final EdgeStyle style = EdgeStyle.getStyle(styleString);
				mEdgeController.setStyle(freeplaneNode, style);
			}
		}
	}

	public static void moveNodeTo(MMapController mapController, String parentNodeId, String nodeToMoveId, int newIndex) throws NodeNotFoundException {
		final NodeModel parentNode = getNodeFromOpenMapById(mapController, parentNodeId);
		final NodeModel nodeToMove = getNodeFromOpenMapById(mapController, nodeToMoveId);

		mapController.moveNode(nodeToMove, parentNode, newIndex);
		nodeToMove.setLeft(parentNode.isLeft());
	}

	private static void updateLocationModel(MMapController mapController, NodeModel freeplaneNode, Integer hGap, Integer shiftY) {
		System.out.println("changing location");
		final MLocationController locationController = (MLocationController)MLocationController.getController();
		final LocationModel lm = LocationModel.getModel(freeplaneNode);
		final int newHGap = hGap != null ? hGap : lm.getHGap();
		final int newShiftY = shiftY != null ? shiftY : lm.getShiftY();
		final int parentVGap = LocationModel.getModel(freeplaneNode.getParentNode()).getVGap();
		
		locationController.moveNodePosition(freeplaneNode, parentVGap, newHGap, newShiftY);
	}

}
