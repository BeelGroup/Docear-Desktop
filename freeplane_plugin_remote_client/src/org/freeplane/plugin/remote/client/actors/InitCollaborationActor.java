package org.freeplane.plugin.remote.client.actors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.docear.messages.models.MapIdentifier;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.remote.client.ClientController;
import org.freeplane.plugin.remote.client.ClientController.CheckForChangesRunnable;
import org.freeplane.plugin.remote.client.User;
import org.freeplane.plugin.remote.client.actors.InitCollaborationActor.Messages.InitCollaborationMode;
import org.freeplane.plugin.remote.client.actors.ListenForUpdatesActor.Messages.SetMapAndRevision;
import org.freeplane.plugin.remote.client.services.MapAsXmlResponse;
import org.freeplane.plugin.remote.client.services.WS;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;

public class InitCollaborationActor extends FreeplaneClientActor {

	private MapIdentifier mapIdentifier = null;

	public InitCollaborationActor(ClientController clientController) {
		super(clientController);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof InitCollaborationMode) {
			final InitCollaborationMode msg = (InitCollaborationMode) message;
			
			this.mapIdentifier = msg.getMapIdentifier();
			final WS ws = getClientController().webservice();
			final User user = msg.getUser();
			Validate.notNull(user);

			final Future<MapAsXmlResponse> mindmapFuture = ws.getMapAsXml(user, mapIdentifier);
			Patterns.pipe(mindmapFuture, getContext().system().dispatcher()).to(getSelf());
		}
		// xml mindmap wrapped in json
		else if (message instanceof MapAsXmlResponse) {
			final MapAsXmlResponse response = (MapAsXmlResponse) message;
			final int currentRevision = (int)response.getRevision();
			final String xmlString = response.getXmlString();
			final Random ran = new Random();
			final String filename = "" + System.currentTimeMillis() + ran.nextInt(100);
			final String tempDirPath = System.getProperty("java.io.tmpdir");
			final File file = new File(tempDirPath + "/docear/" + filename + ".mm");

			try {
				FileUtils.writeStringToFile(file, xmlString);
				final URL pathURL = file.toURI().toURL();

				final MMapIO mio = (MMapIO) ClientController.getMapIO();
				mio.newMap(pathURL);
			} catch (IOException e) {
				throw new AssertionError(e);
			} catch (URISyntaxException e) {
				throw new AssertionError(e);
			} catch (XMLException e) {
				throw new AssertionError(e);
			} catch (NullPointerException e) {
				LogUtils.warn("NPE catched!", e);
			} finally {
				file.delete();
			}

			final ActorRef listenForUpdatesActor = getClientController().listenForUpdatesActor();
			listenForUpdatesActor.tell(new SetMapAndRevision(mapIdentifier, currentRevision), getSelf());
			listenForUpdatesActor.tell("listen", getSelf());

			final ActorSystem system = getContext().system();
			system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), new CheckForChangesRunnable(getClientController()), system.dispatcher());
		}

	}

	public static final class Messages {
		public static class InitCollaborationMode {
			private final MapIdentifier mapIdentifier;
			private final User user;

			public InitCollaborationMode(MapIdentifier mapIdentifier, User user) {
				super();
				this.mapIdentifier = mapIdentifier;
				this.user = user;
			}

			public MapIdentifier getMapIdentifier() {
				return mapIdentifier;
			}

			public User getUser() {
				return user;
			}

		}
	}
}
