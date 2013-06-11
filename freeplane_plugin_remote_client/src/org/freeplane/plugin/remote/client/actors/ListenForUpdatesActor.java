package org.freeplane.plugin.remote.client.actors;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.GetUpdatesResponse;
import org.freeplane.plugin.remote.client.services.WS;
import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;

import scala.concurrent.Future;
import akka.pattern.Patterns;

public class ListenForUpdatesActor extends FreeplaneClientActor {

	private String currentMapId;
	private String mapIdForThisExecution;
	private int currentRevision;

	public ListenForUpdatesActor(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		LogUtils.info(message.toString());
		if (message instanceof Messages.SetMapAndRevision) {
			currentMapId = ((Messages.SetMapAndRevision) message).getMapId();
			currentRevision = ((Messages.SetMapAndRevision) message).getRevision();
		} else if (message.equals("listen")) {
			LogUtils.info("listening");
			mapIdForThisExecution = currentMapId;
			final User user = getClientController().getUser();
			final Future<Boolean> future = webservice().listenIfUpdatesOccur(user.getUsername(), user.getAccessToken(), mapIdForThisExecution);
			Patterns.pipe(future, getContext().system().dispatcher()).to(getSelf());
		} else if (message instanceof Boolean) {
			final Boolean updateOccured = (Boolean) message;
			if (updateOccured && mapIdForThisExecution.equals(currentMapId)) {
				LogUtils.info("updates occured");
				final User user = getClientController().getUser();
				final Future<GetUpdatesResponse> future = webservice().getUpdatesSinceRevision(user.getUsername(), user.getAccessToken(), mapIdForThisExecution, currentRevision);
				Patterns.pipe(future, getContext().system().dispatcher()).to(getSelf());
			} else {
				getSelf().tell("listen", getSelf());
			}
		} else if (message instanceof GetUpdatesResponse) {
			final GetUpdatesResponse response = (GetUpdatesResponse) message;

			this.currentRevision = response.getCurrentRevision();

			for (MapUpdate mapUpdate : response.getOrderedUpdates()) {
				getClientController().applyChangesActor().tell(mapUpdate, getSelf());

			}
			getSelf().tell("listen", getSelf());
		}
	}

	public int getCurrentRevision() {
		return currentRevision;
	}

	public String getCurrentMapId() {
		return currentMapId;
	}

	public void changeMap(String mapId, int currentRevision) {
		this.currentMapId = mapId;
		this.currentRevision = currentRevision;
	}

	private WS webservice() {
		return getClientController().webservice();
	}

	public final static class Messages {
		private Messages() {
		}

		public static class SetMapAndRevision {
			private final String mapId;
			private final int revision;

			public SetMapAndRevision(String mapId, int revision) {
				super();
				this.mapId = mapId;
				this.revision = revision;
			}

			public String getMapId() {
				return mapId;
			}

			public int getRevision() {
				return revision;
			}

		}
	}
}
