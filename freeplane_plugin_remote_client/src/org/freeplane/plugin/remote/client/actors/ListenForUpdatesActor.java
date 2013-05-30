package org.freeplane.plugin.remote.client.actors;

import org.docear.messages.models.MapIdentifier;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.services.GetUpdatesResponse;
import org.freeplane.plugin.remote.client.services.WS;
import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;

import scala.concurrent.Future;
import akka.pattern.Patterns;

public class ListenForUpdatesActor extends FreeplaneClientActor {

	private MapIdentifier currentMapIdentifier;
	private MapIdentifier mapIdentifierForThisExecution;
	private int currentRevision;

	public ListenForUpdatesActor(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		LogUtils.info(message.toString());
		if (message instanceof Messages.SetMapAndRevision) {
			currentMapIdentifier = ((Messages.SetMapAndRevision) message).getMapIdentifier();
			currentRevision = ((Messages.SetMapAndRevision) message).getRevision();
		} else if (message.equals("listen")) {
			LogUtils.info("listening");
			mapIdentifierForThisExecution = currentMapIdentifier;
			final User user = getClientController().getUser();
			final Future<Boolean> future = webservice().listenIfUpdatesOccur(user, mapIdentifierForThisExecution);
			Patterns.pipe(future, getContext().system().dispatcher()).to(getSelf());
		} else if (message instanceof Boolean) {
			final Boolean updateOccured = (Boolean) message;
			if (updateOccured && mapIdentifierForThisExecution.equals(currentMapIdentifier)) {
				LogUtils.info("updates occured");
				final User user = getClientController().getUser();
				final Future<GetUpdatesResponse> future = webservice().getUpdatesSinceRevision(user, mapIdentifierForThisExecution, currentRevision);
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

	public MapIdentifier getCurrentMapIdentifier() {
		return currentMapIdentifier;
	}

	public void changeMap(MapIdentifier mapIdentifier, int currentRevision) {
		this.currentMapIdentifier = mapIdentifier;
		this.currentRevision = currentRevision;
	}

	private WS webservice() {
		return getClientController().webservice();
	}

	public final static class Messages {
		private Messages() {
		}

		public static class SetMapAndRevision {
			private final MapIdentifier mapIdentifier;
			private final int revision;

			public SetMapAndRevision(MapIdentifier mapIdentifier, int revision) {
				super();
				this.mapIdentifier = mapIdentifier;
				this.revision = revision;
			}

			public MapIdentifier getMapIdentifier() {
				return mapIdentifier;
			}

			public int getRevision() {
				return revision;
			}

		}
	}
}
