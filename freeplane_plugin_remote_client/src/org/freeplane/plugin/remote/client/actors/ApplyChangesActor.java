package org.freeplane.plugin.remote.client.actors;

import static org.freeplane.plugin.remote.RemoteUtils.addNodeToOpenMap;
import static org.freeplane.plugin.remote.RemoteUtils.changeNodeAttribute;
import static org.freeplane.plugin.remote.RemoteUtils.getNodeFromOpenMapById;

import org.docear.messages.exceptions.NodeNotFoundException;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.plugin.remote.RemoteUtils;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.v10.model.updates.AddNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.AddNodeUpdate.Side;
import org.freeplane.plugin.remote.v10.model.updates.ChangeNodeAttributeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.DeleteNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MoveNodeUpdate;

public class ApplyChangesActor extends FreeplaneClientActor {

	public ApplyChangesActor(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof MapUpdate) {
			final MapUpdate mapUpdate = (MapUpdate) message;

			LogUtils.info("my System:  " + loggedInUser() + "@" + source());
			LogUtils.info("remote sys: " + mapUpdate.getUsername() + "@" + mapUpdate.getSource());
			if (mapUpdate.getUsername().equals(loggedInUser()) && mapUpdate.getSource().equals(source())) {
				// update was done by this instance
				return;
			}

			isUpdating(true);
			if (mapUpdate instanceof AddNodeUpdate) {
				final AddNodeUpdate update = (AddNodeUpdate) mapUpdate;
				addNodeUpdate(update);

			} else if (mapUpdate instanceof DeleteNodeUpdate) {
				final DeleteNodeUpdate update = (DeleteNodeUpdate) mapUpdate;
				deleteNodeUpdate(update);

			} else if (mapUpdate instanceof ChangeNodeAttributeUpdate) {
				final ChangeNodeAttributeUpdate update = (ChangeNodeAttributeUpdate) mapUpdate;
				changeNodeAttributeUpdate(update);

			} else if (mapUpdate instanceof MoveNodeUpdate) {
				final MoveNodeUpdate update = (MoveNodeUpdate) mapUpdate;
				moveNodeUpdate(update);

			}
			isUpdating(false);
		}
	}

	private void addNodeUpdate(AddNodeUpdate update) throws NodeNotFoundException {
		final String newNodeId = update.getNewNodeId();
		final String parentNodeId = update.getParentNodeId();
		LogUtils.info("adding node with Id: " + newNodeId);
		final NodeModel parentNode = getNodeFromOpenMapById(mmapController(), parentNodeId);

		final NodeModel newNode = addNodeToOpenMap(mmapController(), parentNode);
		newNode.setID(newNodeId);
		if (update.getSide() != null) {
			newNode.setLeft(update.getSide() == Side.Left);
		}
	}

	private void deleteNodeUpdate(DeleteNodeUpdate update) throws NodeNotFoundException {
		final String nodeId = update.getNodeId();
		final NodeModel node = getNodeFromOpenMapById(mmapController(), nodeId);
		mmapController().deleteNode(node);
	}

	private void changeNodeAttributeUpdate(ChangeNodeAttributeUpdate update) throws NodeNotFoundException {
		try {
			final NodeModel freeplaneNode = getNodeFromOpenMapById(mmapController(), update.getNodeId());
			changeNodeAttribute(freeplaneNode, update.getAttribute(), update.getValue());
			if (getClientController().selectedNodesMap().containsKey(freeplaneNode)) {
				getClientController().selectedNodesMap().get(freeplaneNode).updateCurrentState();
			}
			ClientController.mmapController().nodeChanged(freeplaneNode);
		} catch (NullPointerException e) {
			// Do nothing, but happens very often in freeplane view
			LogUtils.severe("NPE catched! ", e);
		}
	}

	private void moveNodeUpdate(MoveNodeUpdate update) throws NodeNotFoundException {
		RemoteUtils.moveNodeTo(mmapController(), update.getNewParentNodeId(), update.getNodetoMoveId(), update.getNewIndex());
	}

	private void isUpdating(boolean value) {
		getClientController().isUpdating(value);
	}

	private MMapController mmapController() {
		return ClientController.mmapController();
	}

	private String loggedInUser() {
		return ClientController.loggedInUserName();
	}

	private String source() {
		return getClientController().source();
	}
}
