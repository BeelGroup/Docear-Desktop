package org.freeplane.plugin.remote.client.listeners;

import java.util.Map;

import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.listeners.NodeViewListener.NodeChangeData;

public class NodeSelectionListener extends BaseListener implements INodeSelectionListener {
	
	public NodeSelectionListener(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onSelect(NodeModel node) {
		final NodeViewListener listener = new NodeViewListener(getClientController(), node, null, null);
		node.addViewer(listener);
		getClientController().selectedNodesMap().put(node, listener);
	}

	@Override
	public void onDeselect(NodeModel node) {
		final NodeViewListener listener = getClientController().selectedNodesMap().remove(node);
		if (listener != null && isListening()) {
			final NodeChangeData data = listener.getChangedAttributes();

			for (Map.Entry<String, Object> entry : data.getNodeChanges().entrySet()) {
				webservice().changeNode(user(), mapIdentifier(), node.getID(), entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, Object> entry : data.getEdgeChanges().entrySet()) {
				webservice().changeEdge(user(), mapIdentifier(), node.getID(), entry.getKey(), entry.getValue());
			}

			node.removeViewer(listener);
		}

	}
}
