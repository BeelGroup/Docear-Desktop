package org.freeplane.plugin.remote.server.actors;

import org.docear.messages.Messages.AddNodeRequest;
import org.docear.messages.Messages.ChangeNodeRequest;
import org.docear.messages.Messages.CloseAllOpenMapsRequest;
import org.docear.messages.Messages.CloseMapRequest;
import org.docear.messages.Messages.CloseServerRequest;
import org.docear.messages.Messages.CloseUnusedMaps;
import org.docear.messages.Messages.FetchMindmapUpdatesRequest;
import org.docear.messages.Messages.GetNodeRequest;
import org.docear.messages.Messages.ListenToUpdateOccurrenceRequest;
import org.docear.messages.Messages.MindmapAsJsonRequest;
import org.docear.messages.Messages.MindmapAsXmlRequest;
import org.docear.messages.Messages.MoveNodeToRequest;
import org.docear.messages.Messages.OpenMindMapRequest;
import org.docear.messages.Messages.ReleaseLockRequest;
import org.docear.messages.Messages.RemoveNodeRequest;
import org.docear.messages.Messages.RequestLockRequest;
import org.docear.messages.exceptions.LockNotFoundException;
import org.docear.messages.exceptions.MapNotFoundException;
import org.docear.messages.exceptions.NodeAlreadyLockedException;
import org.docear.messages.exceptions.NodeNotFoundException;
import org.freeplane.plugin.remote.server.InternalMessages.ReleaseTimedOutLocks;
import org.freeplane.plugin.remote.server.RemoteController;
import org.freeplane.plugin.remote.server.v10.Actions;
import org.slf4j.Logger;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.actor.UntypedActor;

public class MainActor extends UntypedActor {

	public MainActor() {
	}

	@Override
	public void onReceive(Object message) throws Exception {
		final Logger logger = RemoteController.getLogger();
		final ActorRef sender = getSender();

		if (!(message instanceof ReleaseTimedOutLocks)) {
			// Release check happens every 5 seconds and would flood the logging
			logger.info("MainActor.onReceive => '{}' received.", message.getClass().getName());
			logger.info("MainActor.onReceive => Sender: '{}'", sender.path());
		}

		Object response = null;
		try {
			// get map as json
			if (message instanceof MindmapAsJsonRequest) {
				response = Actions.getMapModelJson((MindmapAsJsonRequest) message);
			}

			// get map as xml
			else if (message instanceof MindmapAsXmlRequest) {
				response = Actions.getMapModelXml((MindmapAsXmlRequest) message);
			}

			// add node to map
			else if (message instanceof AddNodeRequest) {
				response = Actions.addNode((AddNodeRequest) message);
			}

			// change node
			else if (message instanceof ChangeNodeRequest) {
				response = Actions.changeNode((ChangeNodeRequest) message);
			}

			// move node to another position
			else if (message instanceof MoveNodeToRequest) {
				response = Actions.moveNodeTo((MoveNodeToRequest) message);
			}

			// remove node from map
			else if (message instanceof RemoveNodeRequest) {
				response = Actions.removeNode((RemoveNodeRequest) message);
			}

			// get node from map
			else if (message instanceof GetNodeRequest) {
				response = Actions.getNode((GetNodeRequest) message);
			}

			// Open mindmap
			else if (message instanceof OpenMindMapRequest) {
				response = Actions.openMindmap((OpenMindMapRequest) message);
			}

			// close map
			else if (message instanceof CloseMapRequest) {
				Actions.closeMap((CloseMapRequest) message);
			}

			// close all maps
			else if (message instanceof CloseAllOpenMapsRequest) {
				Actions.closeAllOpenMaps((CloseAllOpenMapsRequest) message);
			}

			// close server
			else if (message instanceof CloseServerRequest) {
				Actions.closeServer((CloseServerRequest) message);
			}

			// release lock
			else if (message instanceof ReleaseLockRequest) {
				response = Actions.releaseLock((ReleaseLockRequest) message);
			}

			// request lock
			else if (message instanceof RequestLockRequest) {
				response = Actions.requestLock((RequestLockRequest) message);
			}

			// get updates since specific revision
			else if (message instanceof FetchMindmapUpdatesRequest) {
				response = Actions.fetchUpdatesSinceRevision((FetchMindmapUpdatesRequest) message);
			}

			// listen if update occurs
			else if (message instanceof ListenToUpdateOccurrenceRequest) {
				Actions.listenIfUpdateOccurs((ListenToUpdateOccurrenceRequest) message, getSender());
			}

			// close unused maps
			else if (message instanceof CloseUnusedMaps) {
				Actions.closeUnusedMaps((CloseUnusedMaps) message);
			}

			// release timed out Locks
			else if (message instanceof ReleaseTimedOutLocks) {
				Actions.releaseTimedOutLocks((ReleaseTimedOutLocks) message);
			}
		} catch (MapNotFoundException e) {
			logger.warn("MainActor.onReceive => Map not found exception catched. ", e);
			response = new Status.Failure(e);
		} catch (NodeNotFoundException e) {
			logger.warn("MainActor.onReceive => Node not found exception catched. ", e);
			response = new Status.Failure(e);
		} catch (NodeAlreadyLockedException e) {
			logger.warn("MainActor.onReceive => Node already locked exception catched. ", e);
			response = new Status.Failure(e);
		} catch (LockNotFoundException e) {
			logger.warn("MainActor.onReceive => Lock not found exception catched. ", e);
			response = new Status.Failure(e);
		} catch (Exception e) {
			logger.error("MainActor.onReceive => Unrecognized Exception! ", e);
			response = new Status.Failure(e);
		} catch (AssertionError e) {
			logger.error("MainActor.onReceive => Something really bad happened! ", e);
			response = new Status.Failure(e);
		}

		if (response != null) {
			logger.debug("MainActor.onReceive => sending '{}' as response.", response.getClass().getName());
			sender.tell(response, getSelf());
		} else {
			logger.trace("MainActor.onReceive => No response available");
		}
	}
}
