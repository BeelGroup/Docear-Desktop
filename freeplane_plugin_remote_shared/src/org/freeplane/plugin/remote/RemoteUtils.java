package org.freeplane.plugin.remote;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.docear.messages.exceptions.NodeNotFoundException;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.plugin.remote.v10.model.NodeModelBase;

public final class RemoteUtils {

	public static void loadNodesIntoModel(NodeModelBase node, int nodeCount) {
		LinkedList<NodeModelBase> nodeQueue = new LinkedList<NodeModelBase>();
		nodeQueue.add(node);
		while(nodeCount > 0 && !nodeQueue.isEmpty()) {
			NodeModelBase curNode = nodeQueue.pop();

			nodeCount -= curNode.loadChildren(false);
			for(NodeModelBase child : curNode.getAllChildren()) {
				nodeQueue.add(child);
			}
		}
	}
	
	public static NodeModel addNodeToOpenMap(MMapController mapController, NodeModel parentNode) {
		//logger().debug("Actions.addNode => creating new node");
		NodeModel node = mapController.newNode("", parentNode.getMap());

		// insert node
		//logger().debug("Actions.addNode => inserting new node");
		mapController.insertNode(node, parentNode);

		node.createID();
		//logger().debug("Actions.addNode => node with id '{}' successfully created", node.getID());
		return node;
	}
	
	public static NodeModel getNodeFromOpenMapById(MMapController mapController, final String nodeId) throws NodeNotFoundException {
		//logger().debug("Actions.getNodeFromOpenMapById => nodeId: {}", nodeId);
		final NodeModel freeplaneNode = mapController.getNodeFromID(nodeId);

		if (freeplaneNode == null) {
			//logger().error("Actions.getNodeFromOpenMapById => requested node not found; throwing exception");
			throw new NodeNotFoundException("Node with id '" + nodeId + "' not found.");
		}

		return freeplaneNode;
	}
	
	public static void changeNodeAttribute(NodeModel freeplaneNode, String attribute, Object valueObj) {
		System.out.println("attribute: "+attribute);
		if (attribute.equals("folded")) {
			final Boolean value = (Boolean) valueObj;
			freeplaneNode.setFolded(value);
		} else if (attribute.equals("isHtml")) {
			final Boolean isHtml = (Boolean) valueObj;
			if (isHtml) {
				//TODO correct handling
					//freeplaneNode.setXmlText(freeplaneNode.getText());
			} else {
				//freeplaneNode.setText(freeplaneNode.getText());
			}
		} else if (attribute.equals("attributes")) {
			// remove current extension, because everything is written new
			if (freeplaneNode.getExtension(NodeAttributeTableModel.class) != null)
				freeplaneNode.removeExtension(NodeAttributeTableModel.class);

			@SuppressWarnings("unchecked")
			// "Play" sends it as an ArrayList, so I can just grab it
			final List<String> orderedItems = (List<String>) valueObj;

			NodeAttributeTableModel attrTable;
			MAttributeController attrController = MAttributeController.getController();

			if (orderedItems.size() > 0) {
				attrTable = attrController.createAttributeTableModel(freeplaneNode);

				for (int i = 0; i < orderedItems.size(); i++) {
					final String[] parts = orderedItems.get(i).split("%:%");
					//logger().debug("key: {}; value: {}", parts[0], parts[1]);
					attrController.performInsertRow(attrTable, i, parts[0], parts[1]);
				}
				freeplaneNode.addExtension(attrTable);
			}
		} else if (attribute.equals("hGap")) {
			updateLocationModel(freeplaneNode, (Integer) valueObj, null);
		} else if (attribute.equals("shiftY")) {
			updateLocationModel(freeplaneNode, null, (Integer) valueObj);
		} else if (attribute.equals("icons")) {
			// TODO handle
		} else if (attribute.equals("image")) {
			// TODO handle
		} else if (attribute.equals("link")) {
			final String value = valueObj.toString();

			NodeLinks nodeLinks = freeplaneNode.getExtension(NodeLinks.class);

			if (nodeLinks == null) {
				nodeLinks = new NodeLinks();
				freeplaneNode.addExtension(nodeLinks);
			}

			try {
				nodeLinks.setHyperLink(new URI(value));
			} catch (URISyntaxException e) {
				//logger().error("problem saving hyperlink", e);
			}
		} else if (attribute.equals("nodeText")) {
			//TODO handle isHtml correct
			//MMapController ctrl = null;
			//MTextController.getController().setNodeObject(freeplaneNode, valueObj);
			freeplaneNode.setText(valueObj.toString());
			freeplaneNode.fireNodeChanged(new NodeChangeEvent(freeplaneNode, "node_text", "", ""));
		} else if (attribute.equals("note")) {
			final MNoteController noteController = (MNoteController) MNoteController.getController();
			if (valueObj == null) {
				noteController.setNoteText(freeplaneNode, "");
			} else {
				final String noteText = valueObj.toString();
				noteController.setNoteText(freeplaneNode, noteText);
			}
		}
	}
	
	public static void moveNodeTo(MMapController mapController, String parentNodeId, String nodeToMoveId, int newIndex) throws NodeNotFoundException {
		final NodeModel parentNode = getNodeFromOpenMapById(mapController, parentNodeId);
		final NodeModel nodeToMove = getNodeFromOpenMapById(mapController, nodeToMoveId);

		mapController.moveNode(nodeToMove, parentNode, newIndex);
		nodeToMove.setLeft(parentNode.isLeft());
	}
	
	private static void updateLocationModel(NodeModel freeplaneNode, Integer hGap, Integer Shifty) {
		System.out.println("changing location");
		int oldhGap = 0;
		int oldshiftY = 0;
		
		LocationModel lm = freeplaneNode.getExtension(LocationModel.class);
		if (lm != null) {
			oldhGap = lm.getHGap();
			oldshiftY = lm.getShiftY();
		}
		LocationModel model = LocationModel.createLocationModel(freeplaneNode);
		model.setHGap(hGap != null ? hGap : oldhGap);
		model.setShiftY(Shifty != null ? Shifty : oldshiftY);
	}

}
