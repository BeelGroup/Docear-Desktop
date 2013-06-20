package org.freeplane.plugin.remote.server.v10;

import static org.freeplane.plugin.remote.RemoteUtils.changeEdgeAttribute;
import static org.freeplane.plugin.remote.RemoteUtils.changeNodeAttribute;
import static org.freeplane.plugin.remote.RemoteUtils.getNodeFromOpenMapById;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.docear.messages.Messages;
import org.docear.messages.Messages.AddNodeRequest;
import org.docear.messages.Messages.AddNodeResponse;
import org.docear.messages.Messages.ChangeEdgeRequest;
import org.docear.messages.Messages.ChangeEdgeResponse;
import org.docear.messages.Messages.ChangeNodeRequest;
import org.docear.messages.Messages.ChangeNodeResponse;
import org.docear.messages.Messages.CloseAllOpenMapsRequest;
import org.docear.messages.Messages.CloseMapRequest;
import org.docear.messages.Messages.CloseServerRequest;
import org.docear.messages.Messages.CloseUnusedMaps;
import org.docear.messages.Messages.CreateMindmapRequest;
import org.docear.messages.Messages.CreateMindmapResponse;
import org.docear.messages.Messages.FetchMindmapUpdatesRequest;
import org.docear.messages.Messages.FetchMindmapUpdatesResponse;
import org.docear.messages.Messages.GetNodeRequest;
import org.docear.messages.Messages.GetNodeResponse;
import org.docear.messages.Messages.ListenToUpdateOccurrenceRequest;
import org.docear.messages.Messages.MindmapAsJsonReponse;
import org.docear.messages.Messages.MindmapAsJsonRequest;
import org.docear.messages.Messages.MindmapAsXmlRequest;
import org.docear.messages.Messages.MindmapAsXmlResponse;
import org.docear.messages.Messages.MoveNodeToRequest;
import org.docear.messages.Messages.MoveNodeToResponse;
import org.docear.messages.Messages.OpenMindMapRequest;
import org.docear.messages.Messages.OpenMindMapResponse;
import org.docear.messages.Messages.ReleaseLockRequest;
import org.docear.messages.Messages.ReleaseLockResponse;
import org.docear.messages.Messages.RemoveNodeRequest;
import org.docear.messages.Messages.RemoveNodeResponse;
import org.docear.messages.Messages.RequestLockRequest;
import org.docear.messages.Messages.RequestLockResponse;
import org.docear.messages.exceptions.LockNotFoundException;
import org.docear.messages.exceptions.MapNotFoundException;
import org.docear.messages.exceptions.NodeAlreadyLockedException;
import org.docear.messages.exceptions.NodeNotFoundException;
import org.docear.messages.exceptions.NodeNotLockedByUserException;
import org.docear.messages.models.MapIdentifier;
import org.docear.messages.models.UserIdentifier;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.remote.RemoteUtils;
import org.freeplane.plugin.remote.server.InternalMessages.ReleaseTimedOutLocks;
import org.freeplane.plugin.remote.server.OpenMindmapInfo;
import org.freeplane.plugin.remote.server.RemoteController;
import org.freeplane.plugin.remote.v10.model.LockModel;
import org.freeplane.plugin.remote.v10.model.MapModel;
import org.freeplane.plugin.remote.v10.model.NodeModelDefault;
import org.freeplane.plugin.remote.v10.model.updates.AddNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.AddNodeUpdate.Side;
import org.freeplane.plugin.remote.v10.model.updates.ChangeEdgeAttributeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.ChangeNodeAttributeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.DeleteNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MoveNodeUpdate;
import org.slf4j.Logger;

import akka.actor.ActorRef;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Actions {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static MapIdentifier currentSelectedMapId = null;

	/**
	 * returns a map as a JSON-Object
	 * 
	 * @param id
	 *            ID of map
	 * @param nodeCount
	 *            soft limit of node count. When limit is reached, it only loads
	 *            the outstanding child nodes of the current node.
	 * @return a map model
	 */
	public static MindmapAsJsonReponse getMapModelJson(MindmapAsJsonRequest request) throws MapNotFoundException {

		final int nodeCount = request.getNodeCount();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();

		final boolean loadAllNodes = nodeCount == -1;

		logger().debug("Actions.getMapModelJson => mapId:'{}'; nodeCount:{}; loadAllNodes:{}", mapIdentifier.getMapId(), nodeCount, loadAllNodes);
		final ModeController modeController = modeController();

		logger().debug("Actions.getMapModelJson => selecting map");
		selectMap(mapIdentifier);

		logger().debug("Actions.getMapModelJson => retrieving freeplane map");
		org.freeplane.features.map.MapModel freeplaneMap = modeController.getController().getMap();
		if (freeplaneMap == null) { // when not mapMode
			logger().error("Actions.getMapModelJson => Current mode not MapMode!");
			throw new AssertionError("Current mode not MapMode");
		}

		// create the MapModel for JSON
		logger().debug("Actions.getMapModelJson => creating mapmodel for JSON-convertion");
		final OpenMindmapInfo info = RemoteController.getMapIdentifierInfoMap().get(request.getMapIdentifier());
		final int revision = info.getCurrentRevision();
		MapModel mm = new MapModel(freeplaneMap, revision, loadAllNodes);

		if (!loadAllNodes) {
			RemoteUtils.loadNodesIntoModel(mm.root, nodeCount);
		}

		logger().debug("Actions.getMapModelJson => creating JSON string");
		String result = mm.toJsonString();

		logger().debug("Actions.getMapModelJson => returning JSON string");
		return new MindmapAsJsonReponse(result);
	}

	/**
	 * returns a map as a JSON-Object
	 * 
	 * @param id
	 *            ID of map
	 * @param nodeCount
	 *            soft limit of node count. When limit is reached, it only loads
	 *            the outstanding child nodes of the current node.
	 * @return a map model
	 * @throws MapNotFoundException
	 * @throws IOException
	 */
	public static MindmapAsXmlResponse getMapModelXml(final MindmapAsXmlRequest request) throws MapNotFoundException, IOException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		logger().debug("Actions.getMapModelXml => mapId:'{}'", mapIdentifier.getMapId());

		logger().debug("Actions.getMapModelXml => selecting map");
		selectMap(mapIdentifier);

		final ModeController modeController = modeController();
		final org.freeplane.features.map.MapModel freeplaneMap = modeController.getController().getMap();
		if (freeplaneMap == null) { // when not mapMode
			logger().error("Actions.getMapModelXml => current mode not MapMode!");
			throw new AssertionError("Current mode not MapMode");
		}

		logger().debug("Actions.getMapModelXml => serialising map to XML");

		Writer writer = null;
		ByteArrayOutputStream out = null;
		byte[] bytes = null;
		try {
			out = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(out);
			modeController.getMapController().getMapWriter().writeMapAsXml(freeplaneMap, writer, MapWriter.Mode.EXPORT, true, true);
			bytes = out.toByteArray();

		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(out);
		}

		final int currentRevision = getOpenMindMapInfo(request.getMapIdentifier()).getCurrentRevision();
		logger().debug("Actions.getMapModelXml => returning map as XML string");
		return new MindmapAsXmlResponse(bytes, currentRevision);
	}

	/**
	 * closes a map on the server
	 * 
	 * @param id
	 * @return
	 */
	public static void closeMap(final CloseMapRequest request) throws MapNotFoundException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		logger().debug("Actions.closeMap => mapId:'{}'", request.getMapId());

		// select map
		logger().debug("Actions.closeMap => selecting map");
		selectMap(mapIdentifier);

		// close and remove map
		logger().debug("Actions.closeMap => closing map");
		modeController().getController().close(true);

		logger().debug("Actions.closeMap => removing map info from MapIdInfoMap");
		openMindmapInfoMap().remove(mapIdentifier);
	}

	private static String getTempFileName() {
		final Random ran = new Random();
		final String filename = "" + System.currentTimeMillis() + ran.nextInt(100);
		final String tempDirPath = System.getProperty("java.io.tmpdir");
		return tempDirPath + "/docear/" + filename + ".mm";
	}

	public static OpenMindMapResponse openMindmap(final OpenMindMapRequest request, final ActorRef sender) {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String mapContent = request.getMindmapFileContent();
		logger().debug("Actions.openMindmap => mapId: {}; content:'{}...'", mapIdentifier.getMapId(), mapContent.substring(0, Math.min(mapContent.length(), 20)));

		// create file

		final File file = new File(getTempFileName());
		logger().debug("Actions.openMindmap => temporary file '{}' was created", file.getAbsolutePath());

		long currentRevision = -1;
		try {
			logger().debug("Actions.openMindmap => writing mindmap content to file");
			FileUtils.writeStringToFile(file, mapContent);

			// put map in openMap Collection
			final URL pathURL = file.toURI().toURL();
			final OpenMindmapInfo info = new OpenMindmapInfo(pathURL, sender);
			openMindmapInfoMap().put(request.getMapIdentifier(), info);
			currentRevision = info.getCurrentRevision();
			logger().debug("Actions.openMindmap => mindmap was put into openMindmapInfoMap ({} => {})", mapIdentifier.getMapId(), info.getMapUrl());
			// open map
			logger().debug("Actions.openMindmap => opening mindmap...");
			final MMapIO mio = (MMapIO) RemoteController.getMapIO();
			mio.newMap(pathURL);
			logger().debug("Actions.openMindmap => map successfully loaded and opened!");
		} catch (IOException e) {
			throw new AssertionError(e);
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		} catch (XMLException e) {
			throw new AssertionError(e);
		} finally {
			logger().debug("Actions.closeMap => removing temporary file from file system");
			file.delete();
		}

		return new OpenMindMapResponse(currentRevision, true);
	}

	public static FetchMindmapUpdatesResponse fetchUpdatesSinceRevision(FetchMindmapUpdatesRequest request) throws MapNotFoundException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final Integer sinceRevision = request.getRevisionId();
		logger().debug("Actions.getUpdatesSinceRevision => mapId: {}; sinceRevision: {}", mapIdentifier.getMapId(), sinceRevision);

		final OpenMindmapInfo info = getOpenMindMapInfo(request.getMapIdentifier());
		if (info == null) {
			throw new MapNotFoundException("Map with id " + mapIdentifier.getMapId() + " was not found", request.getMapIdentifier());
		}

		List<String> list = info.getUpdateListAsJson(sinceRevision);
		return new FetchMindmapUpdatesResponse(info.getCurrentRevision(), list);
	}

	public static void listenIfUpdateOccurs(ListenToUpdateOccurrenceRequest request, ActorRef sender) throws MapNotFoundException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final OpenMindmapInfo info = getOpenMindMapInfo(mapIdentifier);
		if (info == null)
			throw new MapNotFoundException("Map with id " + mapIdentifier.getMapId() + " was not present", request.getMapIdentifier());

		info.registerUpdateListener(sender);
	}

	public static void closeServer(CloseServerRequest request) {
		logger().debug("Actions.closeServer => no parameters");

		logger().debug("Actions.closeServer => closing open maps");
		saveAndCloseAllOpenMaps(new CloseAllOpenMapsRequest(request.getUserIdentifier()));

		logger().debug("Actions.closeServer => Starting Thread to shutdown App in 2 seconds");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger().debug("Actions.closeServer => shutting down");
				System.exit(15);

			}
		}).start();

	}

	/**
	 * returns a node as a JSON-Object
	 * 
	 * @param id
	 *            ID of node
	 * @param nodeCount
	 *            soft limit of node count. When limit is reached, it only loads
	 *            the outstanding child nodes of the current node.
	 * @return a node model
	 * @throws MapNotFoundException
	 * @throws NodeNotFoundException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static GetNodeResponse getNode(final GetNodeRequest request) throws MapNotFoundException, NodeNotFoundException, JsonGenerationException, JsonMappingException, IOException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String nodeId = request.getNodeId();
		final int nodeCount = request.getNodeCount();
		final boolean loadAllNodes = nodeCount == -1;

		logger().debug("Actions.getNode => mapId:'{}'; nodeId:'{}'; nodeCount:{}; loadAllNodes:{}", mapIdentifier.getMapId(), nodeId, nodeCount, loadAllNodes);

		logger().debug("Actions.getNode => selecting map");
		selectMap(mapIdentifier);

		logger().debug("Actions.getNode => retrieving freeplane node");
		final NodeModel freeplaneNode = getNodeFromOpenMapById(mmapController(), nodeId);

		logger().debug("Actions.getNode => loading into model to convert to JSON");
		final NodeModelDefault node = new NodeModelDefault(freeplaneNode, loadAllNodes);
		if (!loadAllNodes) {
			RemoteUtils.loadNodesIntoModel(node, request.getNodeCount());
		}

		logger().debug("Actions.getNode => returning node as JSON");
		return new GetNodeResponse(buildJSON(node));
	}

	public static AddNodeResponse addNode(AddNodeRequest request) throws MapNotFoundException, NodeNotFoundException, JsonGenerationException, JsonMappingException, IOException {
		final String source = request.getSource();
		final String username = request.getUsername();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String parentNodeId = request.getParentNodeId();
		final Side side = request.getSide() == null ? null : Side.valueOf(request.getSide());
		logger().debug("Actions.addNode => mapId:'{}'; parentNodeId:'{}'; side:'{}'", mapIdentifier.getMapId(), parentNodeId, side);

		logger().debug("Actions.addNode => selecting map");
		selectMap(mapIdentifier);

		// get parent Node
		logger().debug("Actions.addNode => retrieving freeplane parent node");
		final NodeModel parentNode = getNodeFromOpenMapById(mmapController(), parentNodeId);

		Side sideAdjusted = side;
		if (parentNode.isRoot()) {
			sideAdjusted = side == null ? Side.Left : side;
		} else {
			sideAdjusted = null;
		}

		// create new node
		final NodeModel node = RemoteUtils.addNodeToOpenMap(mmapController(), parentNode, sideAdjusted);

		logger().debug("Actions.addNode => returning response with new node as json");
		final JsonNode nodeJson = new ObjectMapper().valueToTree(new NodeModelDefault(node, false));
		final AddNodeUpdate update = new AddNodeUpdate(source, username, parentNodeId, node.getID(), nodeJson, sideAdjusted);

		final OpenMindmapInfo openMindmapInfo = getOpenMindMapInfo(request.getMapIdentifier());
		openMindmapInfo.addUpdate(update);
		return new AddNodeResponse(openMindmapInfo.getCurrentRevision(), true, update.toJson());
	}

	public static ChangeNodeResponse changeNode(final ChangeNodeRequest request) throws MapNotFoundException, NodeNotFoundException, NodeNotLockedByUserException {
		final UserIdentifier userIdentifier = request.getUserIdentifier();
		final String source = request.getSource();
		final String username = request.getUsername();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final Map<String, Object> attributeMap = request.getAttributeValueMap();
		final String nodeId = request.getNodeId();
		logger().debug("Actions.changeNode => mapIdentifier:'{}'; userIdentifier: '{}'; attributes: '{}'", mapIdentifier, userIdentifier, attributeMap.toString());

		logger().debug("Actions.changeNode => selecting map");
		selectMap(mapIdentifier);

		// get node
		logger().debug("Actions.changeNode => retrieving node");
		final NodeModel freeplaneNode = getNodeFromOpenMapById(mmapController(), nodeId);
		// check if user has lock
		if (!hasUserLockOnNode(freeplaneNode, username)) {
			throw new NodeNotLockedByUserException("User has no lock on node");
		}

		// list to collect updates done
		final List<MapUpdate> updates = new ArrayList<MapUpdate>();

		for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
			final String attribute = entry.getKey();
			final String valueObj = entry.getValue().toString();

			logger().debug("Actions.changeNode => {} changed to {}", attribute, valueObj);
			updates.add(new ChangeNodeAttributeUpdate(source, username, nodeId, attribute, valueObj));

			changeNodeAttribute(mmapController(), freeplaneNode, attribute, valueObj);
		}

		logger().debug("Actions.changeNode => refreshing lock access time");
		refreshLockAccessTime(freeplaneNode);

		// submit changes and create list for response
		final OpenMindmapInfo info = getOpenMindMapInfo(request.getMapIdentifier());
		final List<String> updateJsons = new ArrayList<String>();
		for (MapUpdate update : updates) {
			info.addUpdate(update);
			updateJsons.add(update.toJson());
		}

		return new ChangeNodeResponse(info.getCurrentRevision(), true, updateJsons);
	}

	public static ChangeEdgeResponse changeEdge(ChangeEdgeRequest request) throws NodeNotFoundException {
		final String source = request.getSource();
		final String username = request.getUsername();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final Map<String, Object> attributeMap = request.getAttributeValueMap();
		final String nodeId = request.getNodeId();
		logger().debug("Actions.changeEdge => mapId:'{}'; nodeId:'{}'; username: '{}'; attributes: '{}'", mapIdentifier.getMapId(), nodeId, username, attributeMap.toString());

		logger().debug("Actions.changeEdge => selecting map");
		selectMap(mapIdentifier);

		// get node
		logger().debug("Actions.changeEdge => retrieving node");
		final NodeModel freeplaneNode = getNodeFromOpenMapById(mmapController(), nodeId);

		// list to collect updates done
		final List<MapUpdate> updates = new ArrayList<MapUpdate>();

		for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
			final String attribute = entry.getKey();
			final Object valueObj = entry.getValue();

			logger().debug("Actions.changeEdge => {} changed to {}", attribute, valueObj);
			updates.add(new ChangeEdgeAttributeUpdate(source, username, nodeId, attribute, valueObj));

			changeEdgeAttribute(freeplaneNode, attribute, valueObj == null ? null : valueObj.toString());
		}

		// submit changes and create list for response
		final OpenMindmapInfo info = getOpenMindMapInfo(request.getMapIdentifier());
		for (MapUpdate update : updates) {
			info.addUpdate(update);
		}

		return new ChangeEdgeResponse(info.getCurrentRevision(), true);
	}

	public static MoveNodeToResponse moveNodeTo(MoveNodeToRequest request) throws NodeNotFoundException {
		final String source = request.getSource();
		final String username = request.getUsername();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String newParentNodeId = request.getNewParentNodeId();
		final String nodeId = request.getNodeToMoveId();
		final Integer index = request.getNewIndex();

		selectMap(mapIdentifier);
		RemoteUtils.moveNodeTo(mmapController(), newParentNodeId, nodeId, index);
		final OpenMindmapInfo openMindmapInfo = getOpenMindMapInfo(mapIdentifier);
		openMindmapInfo.addUpdate(new MoveNodeUpdate(source, username, newParentNodeId, nodeId, index));

		return new MoveNodeToResponse(openMindmapInfo.getCurrentRevision(), true);
	}

	public static RemoveNodeResponse removeNode(RemoveNodeRequest request) throws NodeNotFoundException, MapNotFoundException {
		final String source = request.getSource();
		final String username = request.getUsername();
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String nodeId = request.getNodeId();
		logger().debug("Actions.removeNode => mapId:'{}'; nodeId:'{}'; username:'{}'", mapIdentifier.getMapId(), nodeId, username);

		logger().debug("Actions.removeNode => selecting map");
		selectMap(mapIdentifier);

		logger().debug("Actions.removeNode => retrieving node");
		final MMapController mapController = (MMapController) modeController().getMapController();
		final NodeModel node = getNodeFromOpenMapById(mmapController(), nodeId);
		final OpenMindmapInfo info = getOpenMindMapInfo(request.getMapIdentifier());
		// check if any node below has a lock
		if (hasAnyChildALock(node)) {
			return new RemoveNodeResponse(info.getCurrentRevision(), false);
		}

		logger().debug("Actions.removeNode => deleting node");
		mapController.deleteNode(node);

		info.addUpdate(new DeleteNodeUpdate(source, username, nodeId));

		return new RemoveNodeResponse(info.getCurrentRevision(), true);
	}

	private static boolean hasAnyChildALock(NodeModel freeplaneNode) {
		boolean hasLock = freeplaneNode.containsExtension(LockModel.class);
		// check if node itself has a lock
		if (hasLock)
			return true;

		// check child nodes have lock
		for (NodeModel node : freeplaneNode.getChildren()) {
			if (hasAnyChildALock(node))
				return true;
		}

		// else no lock
		return false;
	}

	public static RequestLockResponse requestLock(RequestLockRequest request) throws MapNotFoundException, NodeAlreadyLockedException, NodeNotFoundException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String nodeId = request.getNodeId();
		final UserIdentifier userIdentifier = request.getUserIdentifier();
		logger().debug("Actions.requestLock => mapId:'{}'; nodeId:'{}'; username:'{}'", mapIdentifier.getMapId(), nodeId, userIdentifier.getUsername());

		logger().debug("Actions.requestLock => selecting map");
		selectMap(mapIdentifier);

		logger().debug("Actions.requestLock => retrieving freeplane node");
		final NodeModel node = getNodeFromOpenMapById(mmapController(), nodeId);

		logger().debug("Actions.requestLock => retrieving lock model");
		final LockModel lockModel = node.getExtension(LockModel.class);
		final OpenMindmapInfo openMindmapInfo = getOpenMindMapInfo(mapIdentifier);
		if (lockModel == null) { // no lock present
			logger().debug("Actions.requestLock => no lock on node, creating lock...");
			final String mapUpdateJson = addLockToNode(userIdentifier, mapIdentifier, node);
			return new RequestLockResponse(openMindmapInfo.getCurrentRevision(), true, mapUpdateJson);
		} else if (userIdentifier.getUsername().equals(lockModel.getUsername())) { // refresh
																					// from
			// locking user
			refreshLockAccessTime(node);
			return new RequestLockResponse(openMindmapInfo.getCurrentRevision(), true, null);
		} else { // already locked by someone else
			return new RequestLockResponse(openMindmapInfo.getCurrentRevision(), false, null);
		}
	}

	public static ReleaseLockResponse releaseLock(ReleaseLockRequest request) throws MapNotFoundException, LockNotFoundException, NodeNotFoundException {
		final MapIdentifier mapIdentifier = request.getMapIdentifier();
		final String nodeId = request.getNodeId();
		final String username = request.getUsername();
		logger().debug("Actions.releaseLock => mapId:'{}'; nodeId:'{}'; username: {}", mapIdentifier.getMapId(), nodeId, username);
		final OpenMindmapInfo openMindmapInfo = getOpenMindMapInfo(mapIdentifier);

		logger().debug("Actions.releaseLock => selecting map");
		selectMap(mapIdentifier);

		logger().debug("Actions.releaseLock => retrieving node");
		final NodeModel node = getNodeFromOpenMapById(mmapController(), nodeId);

		logger().debug("Actions.releaseLock => retrieving lock");
		final LockModel lm = node.getExtension(LockModel.class);
		if (lm == null) {
			// No lock available, nothing to release... just quit
			logger().warn("Actions.releaseLock => no lock present");
			// throw new LockNotFoundException("Lock for nodeId " +
			// request.getNodeId() + " not found.");
			return new ReleaseLockResponse(openMindmapInfo.getCurrentRevision(), true, null);
		}

		// check if correct user
		if (username.equals(lm.getUsername())) {
			// release lock
			logger().debug("Actions.releaseLock => releasing lock");
			final String updateJson = releaseLockFromNode(request.getUserIdentifier(), request.getMapIdentifier(), node);

			return new ReleaseLockResponse(openMindmapInfo.getCurrentRevision(), true, updateJson);
		} else {
			return new ReleaseLockResponse(openMindmapInfo.getCurrentRevision(), false, null);
		}
	}

	public static CreateMindmapResponse createNewMindMap(CreateMindmapRequest request, final ActorRef sender) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
		final String filename = getTempFileName();

		final File file = new File(filename);
		final MMapIO mapIO = (MMapIO) RemoteController.getMapIO();
		final org.freeplane.features.map.MapModel mapModel = mmapController().newModel();
		mapIO.writeToFile(mapModel, file);
		final URL mapUrl = file.toURI().toURL();
		mapIO.newMap(mapUrl);

		final OpenMindmapInfo openMindmapInfo = new OpenMindmapInfo(mapUrl, sender);

		openMindmapInfoMap().put(request.getMapIdentifier(), openMindmapInfo);

		return new CreateMindmapResponse(openMindmapInfo.getCurrentRevision(), true);
	}

	public static void releaseTimedOutLocks(ReleaseTimedOutLocks request) throws MapNotFoundException, JsonGenerationException, JsonMappingException, IOException {
		final Long millisSinceRequest = request.getMillisecondsSinceRequest();

		for (Entry<MapIdentifier, OpenMindmapInfo> entry : openMindmapInfoMap().entrySet()) {
			final MapIdentifier mapIdentifier = entry.getKey();
			final OpenMindmapInfo info = entry.getValue();
			final Set<NodeModel> lockedNodes = new HashSet<NodeModel>(info.getLockedNodes());

			for (NodeModel lockedNode : lockedNodes) {
				LockModel lock = lockedNode.getExtension(LockModel.class);
				long timeDiff = System.currentTimeMillis() - lock.getLastAccess();
				if (timeDiff > millisSinceRequest) {
					releaseLockFromNode(new UserIdentifier("server", "server"), mapIdentifier, lockedNode);
				}
			}

		}
	}

	public static void saveAndCloseAllOpenMaps(CloseAllOpenMapsRequest request) {
		logger().debug("Actions.closeAllOpenMaps => no parameters");

		Set<MapIdentifier> mapIds = new HashSet<MapIdentifier>(openMindmapInfoMap().keySet());
		for (MapIdentifier mapId : mapIds) {
			logger().debug("Actions.closeAllOpenMaps => saving map with id '{}'", mapId);

			// only trigger save, play will close maps
			for (OpenMindmapInfo info : openMindmapInfoMap().values()) {
				info.getSender().tell(new Messages.ForceSaveAndCloseRequest(mapId), null);
			}
		}
	}

	public static void closeUnusedMaps(CloseUnusedMaps request) throws Exception {
		final long allowedMsSinceLastAccess = request.getUnusedSinceInMs();
		logger().debug("Actions.closeUnusedMaps => max ms since last access:'{}'", allowedMsSinceLastAccess);

		final long now = System.currentTimeMillis();
		for (final MapIdentifier mapId : new HashSet<MapIdentifier>(openMindmapInfoMap().keySet())) {
			final OpenMindmapInfo omi = getOpenMindMapInfo(mapId);
			final long lastAccessTime = omi.getLastAccessTime();
			final long sinceLastAccess = now - lastAccessTime;
			final long sinceLastAccessInMinutes = sinceLastAccess / 60000;
			logger().debug("Actions.closeUnusedMaps => mapId:'{}'; lastAccess:{}; sinceLastAccess:{}", mapId, lastAccessTime, sinceLastAccess);

			if (sinceLastAccess > allowedMsSinceLastAccess) {
				openMindmapInfoMap().get(mapId).getSender().tell(new Messages.ForceSaveAndCloseRequest(mapId), null);
				logger().info("Actions.closeUnusedMaps => map will be closed, because it havent been used for about {} minutes.", sinceLastAccessInMinutes);
			}
		}
	}

	/**
	 * Select Map so getMapController() has right map.
	 * 
	 * @param mapId
	 *            Id of Map
	 * @throws MapNotFoundException
	 */
	private static void selectMap(MapIdentifier mapIdentifier) throws MapNotFoundException {
		logger().debug("Actions.selectMap => mapId:'{}'", mapIdentifier.getMapId());

		// check if map is already selected
		if (currentSelectedMapId == mapIdentifier) {
			return;
		}

		if (!isMapPresent(mapIdentifier)) {
			logger().error("Actions.selectMap => map not found");
			throw new MapNotFoundException("Map with id " + mapIdentifier.getMapId() + " is not present.", mapIdentifier);
		}

		logger().debug("Actions.selectMap => Changing map to '{}'", mapIdentifier.getMapId());
		final URL pathURL = getOpenMindMapInfo(mapIdentifier).getMapUrl();

		try {
			final MapIO mio = RemoteController.getMapIO();
			mio.newMap(pathURL);
			logger().debug("Actions.selectMap => Map succesfully selected");
		} catch (Exception e) {
			logger().error("Actions.selectMap => Error while selecting map with id '{}'", mapIdentifier.getMapId());
			throw new MapNotFoundException("Could not open Map with id " + mapIdentifier.getMapId(), e, mapIdentifier);
		}
	}

	/**
	 * refresh lastAccesTime of node lock
	 * 
	 * @param node
	 *            Node with lock
	 */
	private static void refreshLockAccessTime(NodeModel node) {
		LockModel lm = node.getExtension(LockModel.class);
		if (lm != null) {
			lm.setLastAccess(System.currentTimeMillis());
		}
	}

	private static String addLockToNode(UserIdentifier userIdentifier, MapIdentifier mapIdentifier, NodeModel freeplaneNode) {
		final OpenMindmapInfo info = getOpenMindMapInfo(mapIdentifier);
		if (freeplaneNode.getExtension(LockModel.class) == null) {
			final LockModel lockModel = new LockModel(userIdentifier.getUsername(), System.currentTimeMillis());

			// add node to locked list
			logger().debug("Actions.addLockToNode => adding node to locked node list");
			info.addLockedNode(freeplaneNode);
			// add lock
			freeplaneNode.addExtension(lockModel);

			final ChangeNodeAttributeUpdate update = new ChangeNodeAttributeUpdate(userIdentifier.getSource(), userIdentifier.getUsername(), freeplaneNode.getID(), "locked",
					userIdentifier.getUsername());
			// add change to revision list
			info.addUpdate(update);
			return update.toJson();
		} else {
			throw new AssertionError("Tried to add Lock to a Node with a Lock present");
		}
	}

	private static String releaseLockFromNode(UserIdentifier userIdentifier, MapIdentifier mapIdentifier, NodeModel freeplaneNode) {
		final OpenMindmapInfo info = getOpenMindMapInfo(mapIdentifier);
		if (freeplaneNode.getExtension(LockModel.class) != null) {

			// remove node from locked list
			logger().debug("Actions.releaseLockFromNode => remove node from locked list");
			info.removeLockedNode(freeplaneNode);
			// remove lock
			freeplaneNode.removeExtension(LockModel.class);

			final ChangeNodeAttributeUpdate update = new ChangeNodeAttributeUpdate(userIdentifier.getSource(), userIdentifier.getUsername(), freeplaneNode.getID(), "locked", null);
			// add change to revision list
			info.addUpdate(update);
			return update.toJson();
		} else {
			throw new AssertionError("Tried to remove Lock from a Node without a Lock");
		}
	}

	private static boolean hasUserLockOnNode(NodeModel node, String userName) {
		LockModel lm = node.getExtension(LockModel.class);
		if (lm == null) { // no lock at all
			return false;
		} else if (userName.equals(lm.getUsername())) {
			return true;
		} else { // locked by someone else
			return false;
		}
	}

	private static OpenMindmapInfo getOpenMindMapInfo(MapIdentifier mapIdentifier) {
		final Map<MapIdentifier, OpenMindmapInfo> map = openMindmapInfoMap();
		if (!map.containsKey(mapIdentifier)) {
			return null;
		}
		return map.get(mapIdentifier);
	}

	private static boolean isMapPresent(MapIdentifier mapIdentifier) {
		return (getOpenMindMapInfo(mapIdentifier) != null);
	}

	private static String buildJSON(Object object) {
		String result = null;

		try {
			result = objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			LogUtils.severe("Error while parsing object to JSON-String!", e);
			throw new AssertionError(e);
		}

		return result;
	}

	private static Map<MapIdentifier, OpenMindmapInfo> openMindmapInfoMap() {
		return RemoteController.getMapIdentifierInfoMap();
	}

	private static ModeController modeController() {
		return RemoteController.getModeController();
	}

	private static MMapController mmapController() {
		return (MMapController) modeController().getMapController();
	}

	private static Logger logger() {
		return RemoteController.getLogger();
	}

}