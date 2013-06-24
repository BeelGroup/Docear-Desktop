package org.freeplane.plugin.remote.client.services;

import java.util.List;

import org.docear.messages.models.MapIdentifier;
import org.freeplane.plugin.remote.client.User;

import scala.concurrent.Future;

public interface WS {
	/**
	 * @deprecated Should not be necessary, because user credentials come from docear
	 * @param username
	 * @param password
	 * @return User or null on failure
	 */
	@Deprecated
	Future<User> login(String username, String password);
	
	Future<Boolean> listenIfUpdatesOccur(User user, MapIdentifier mapIdentifier);
	
	Project getProject(final User user,String projectId);
	List<Project> getProjectsForUser(final User user);
	void createMindmap(final User user, MapIdentifier mapIdentifier);
	
	Future<MapAsXmlResponse> getMapAsXml(User user, MapIdentifier mapIdentifier);
	
	Future<GetUpdatesResponse> getUpdatesSinceRevision(User user, MapIdentifier mapIdentifier, int sinceRevision);
	
	Future<String> createNode(User user, MapIdentifier mapIdentifier, String parentNodeId);
	
	Future<Boolean> moveNodeTo(User user, MapIdentifier mapIdentifier, String newParentId, String nodeToMoveId, int newIndex);
	Future<Boolean> removeNode(User user, MapIdentifier mapIdentifier, String nodeId);
	Future<Boolean> changeNode(User user, MapIdentifier mapIdentifier, String nodeId, String attribute, Object value);
	Future<Boolean> changeEdge(User user, MapIdentifier mapIdentifier, String nodeId, String attribute, Object value);
	
	
}
