package org.freeplane.plugin.remote.client.services;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.output.NullOutputStream;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.v10.model.updates.AddNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.ChangeNodeAttributeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.DeleteNodeUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;
import org.freeplane.plugin.remote.v10.model.updates.MoveNodeUpdate;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.dispatch.Futures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DocearOnlineWs implements WS {
	private final ClientController clientController;
	private final String serviceUrl = "http://localhost:9000";
	// private final String serviceUrl = "https://staging.my.docear.org";
	private final Client restClient;

	public DocearOnlineWs(ClientController clientController) {
		this.clientController = clientController;
		// com.google.common.util.concurrent.
		PrintStream stream = new PrintStream(new NullOutputStream());
		// disableCertificateValidation();
		restClient = ApacheHttpClient.create();
		restClient.addFilter(new LoggingFilter(stream));

		final String source = clientController.source();
		restClient.addFilter(new ClientFilter() {

			@Override
			public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
				String uriString = request.getURI().toASCIIString();
				uriString = uriString.contains("?") ? uriString + "&" : uriString + "?";

				final URI newUri = URI.create(uriString + "source=" + source);
				request.setURI(newUri);

				return getNext().handle(request);
			}
		});
	}

	@Override
	public Future<User> login(final String username, final String password) {
		final WebResource loginResource = restClient.resource(serviceUrl).path("user/login");
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("username", username);
		formData.add("password", password);
		final ClientResponse loginResponse = loginResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

		if(loginResponse.getStatus() == 200) {
			final User user = new User(username, loginResponse.getEntity(String.class));
			return Futures.successful(user);
		} else {
			return null;
		}
	}

	@Override
	public Future<Boolean> listenIfUpdatesOccur(final String username, final String accessToken, final String mapId) {
		return Futures.future(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/listen");

				final ClientResponse loginResponse = resource.get(ClientResponse.class);
				return loginResponse.getStatus() == 200;
			}
		}, clientController.system().dispatcher());

	}

	@Override
	public Future<JsonNode> getMapAsXml(String username, String accessToken, final String mapId) {

		try {
			final WebResource mapAsXmlResource = preparedResource(username, accessToken).path("map/" + mapId + "/xml");
			final JsonNode response = new ObjectMapper().readTree(mapAsXmlResource.get(String.class));
			return Futures.successful(response);
		} catch (Exception e) {
			e.printStackTrace();
			return Futures.failed(e);
		}

	}

	@Override
	public Future<GetUpdatesResponse> getUpdatesSinceRevision(String username, String accessToken, final String mapId, final int sinceRevision) {

		int currentRevision = -1;
		List<MapUpdate> updates = new ArrayList<MapUpdate>();
		final WebResource fetchUpdates = preparedResource(username, accessToken).path("map/" + mapId + "/updates/" + sinceRevision);
		final ClientResponse response = fetchUpdates.get(ClientResponse.class);
		final ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode json = mapper.readTree(response.getEntity(String.class));
			currentRevision = json.get("currentRevision").asInt();

			Iterator<JsonNode> it = json.get("orderedUpdates").iterator();
			while (it.hasNext()) {
				final JsonNode mapUpdateJson = mapper.readTree(it.next().asText());

				final MapUpdate.Type type = MapUpdate.Type.valueOf(mapUpdateJson.get("type").asText());
				switch (type) {
				case AddNode:
					
					updates.add(mapper.treeToValue(mapUpdateJson, AddNodeUpdate.class));
					break;
				case ChangeNodeAttribute:
					updates.add(mapper.treeToValue(mapUpdateJson, ChangeNodeAttributeUpdate.class));
					break;
				case DeleteNode:
					updates.add(mapper.treeToValue(mapUpdateJson, DeleteNodeUpdate.class));
					break;
				case MoveNode:
					updates.add(mapper.treeToValue(mapUpdateJson, MoveNodeUpdate.class));
					break;

				}

			}
		} catch (Exception e) {
			return Futures.failed(e);
		}
		return Futures.successful(new GetUpdatesResponse(currentRevision, updates));

	}

	@Override
	public Future<String> createNode(String username, String accessToken, final String mapId, final String parentNodeId) {

		final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/create");
		final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("parentNodeId", parentNodeId);

		final ClientResponse response = resource.post(ClientResponse.class, formData);
		try {
			final AddNodeUpdate update = new ObjectMapper().readValue(response.getEntity(String.class), AddNodeUpdate.class);
			return Futures.successful(update.getNewNodeId());
		} catch (Exception e) {
			e.printStackTrace();
			return Futures.failed(e);
		}
	}

	@Override
	public Future<Boolean> moveNodeTo(String username, String accessToken, final String mapId, final String newParentId, final String nodeToMoveId, final int newIndex) {
		final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/move");
		final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("newParentNodeId", newParentId);
		formData.add("nodetoMoveId", nodeToMoveId);
		formData.add("newIndex", newIndex + "");

		final ClientResponse response = resource.post(ClientResponse.class, formData);
		LogUtils.info("Status: " + response.getStatus());
		return Futures.successful(response.getStatus() == 200);

	}

	@Override
	public Future<Boolean> removeNode(String username, String accessToken, final String mapId, final String nodeId) {

		final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/delete");
		final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("nodeId", nodeId);

		ClientResponse response = resource.delete(ClientResponse.class, formData);

		LogUtils.info("Status: " + response.getStatus());
		return Futures.successful(response.getStatus() == 200);

	}

	@Override
	public Future<Boolean> changeNode(String username, String accessToken, final String mapId, final String nodeId, final String attribute, final Object value) {
		try {
			final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/change");
			final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			formData.add("nodeId", nodeId);
			Map<String, Object> attributeValueMap = new HashMap<String, Object>();
			attributeValueMap.put(attribute, value);
			formData.add("AttributeValueMapJson", new ObjectMapper().writeValueAsString(attributeValueMap));

			LogUtils.info("locking node");
			// boolean isLocked =
			boolean isLocked = Await.result(lockNode(username, accessToken, mapId, nodeId), Duration.create("5 seconds"));
			if (!isLocked)
				return Futures.successful(false);
			LogUtils.info("changing");
			ClientResponse response = resource.post(ClientResponse.class, formData);
			LogUtils.info("releasing node");
			releaseNode(username, accessToken, mapId, nodeId);

			LogUtils.info("Status: " + response.getStatus());
			return Futures.successful(response.getStatus() == 200);

		} catch (Exception e) {
			e.printStackTrace();
			return Futures.failed(e);
		}
	}

	private Future<Boolean> lockNode(String username, String accessToken, final String mapId, final String nodeId) {
		final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/requestLock");
		final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("nodeId", nodeId);

		ClientResponse response = resource.post(ClientResponse.class, formData);
		LogUtils.info("Status: " + response.getStatus());
		return Futures.successful(response.getStatus() == 200);
	}

	private Future<Boolean> releaseNode(String username, String accessToken, final String mapId, final String nodeId) {
		final WebResource resource = preparedResource(username, accessToken).path("map/" + mapId + "/node/releaseLock");
		final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("nodeId", nodeId);

		ClientResponse response = resource.post(ClientResponse.class, formData);

		LogUtils.info("Status: " + response.getStatus());
		return Futures.successful(response.getStatus() == 200);
	}
	
	private WebResource preparedResource(String username, String accessToken) {
		return restClient.resource(serviceUrl).queryParam("username", username).queryParam("accessToken", accessToken);
	}
}
