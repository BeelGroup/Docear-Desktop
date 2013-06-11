package org.freeplane.plugin.remote.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.remote.client.actors.ApplyChangesActor;
import org.freeplane.plugin.remote.client.actors.InitCollaborationActor;
import org.freeplane.plugin.remote.client.actors.ListenForUpdatesActor;
import org.freeplane.plugin.remote.client.listeners.MapChangeListener;
import org.freeplane.plugin.remote.client.listeners.NodeChangeListener;
import org.freeplane.plugin.remote.client.listeners.NodeViewListener;
import org.freeplane.plugin.remote.client.services.DocearOnlineWs;
import org.freeplane.plugin.remote.client.services.WS;

import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.actor.UntypedActorFactory;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.typesafe.config.ConfigFactory;

public class ClientController {

	// Temp variables, only for developping
	public static final String MAP_ID = "5";
	public static final String USER = "Julius";
	public static final String PW = "secret";

	private final ActorSystem system;
	private final ActorRef listenForUpdatesActor;
	private final ActorRef applyChangeActor;
	private final ActorRef initCollaborationactor;

	private final WS webservice;
	private User user = null;

	private final String sourceString;
	private boolean isUpdating = false;

	private final Map<NodeModel, NodeViewListener> selectedNodesMap = new HashMap<NodeModel, NodeViewListener>();

	@SuppressWarnings("serial")
	public ClientController() {

		// set sourceString, used to identify for updates
		try {
			final String computername = InetAddress.getLocalHost().getHostName();
			sourceString = computername + "_" + System.currentTimeMillis();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		// create Threadpool
		// executor =
		// MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(10));

		// change class loader
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Activator.class.getClassLoader());

		LogUtils.info("starting Client Plugin...");

		system = ActorSystem.create("freeplaneClient", ConfigFactory.load().getConfig("local"));
		listenForUpdatesActor = system.actorOf(new Props(new UntypedActorFactory() {
			@Override
			public Actor create() throws Exception {
				return new ListenForUpdatesActor(ClientController.this);
			}
		}), "updateListener");
		applyChangeActor = system.actorOf(new Props(new UntypedActorFactory() {
			@Override
			public Actor create() throws Exception {
				return new ApplyChangesActor(ClientController.this);
			}
		}), "changeApplier");
		initCollaborationactor = system.actorOf(new Props(new UntypedActorFactory() {
			@Override
			public Actor create() throws Exception {
				return new InitCollaborationActor(ClientController.this);
			}
		}), "initCollaboration");

		webservice = TypedActor.get(system).typedActorOf(new TypedProps<DocearOnlineWs>(WS.class, new Creator<DocearOnlineWs>() {

			@Override
			public DocearOnlineWs create() throws Exception {
				return new DocearOnlineWs(ClientController.this);
			}
		}).withTimeout(Timeout.apply(3, TimeUnit.MINUTES)));

		this.registerListeners();

		initCollaborationactor.tell(new InitCollaborationActor.Messages.InitCollaborationMode(MAP_ID, USER, PW), null);

		// set back to original class loader
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	public static final class CheckForChangesRunnable implements Runnable {
		private final ClientController clientController;

		public CheckForChangesRunnable(ClientController clientController) {
			super();
			this.clientController = clientController;
		}

		@Override
		public void run() {
			final Map<NodeModel, NodeViewListener> selectedNodesMap = clientController.selectedNodesMap();
			for (Map.Entry<NodeModel, NodeViewListener> nodePair : selectedNodesMap.entrySet()) {
				final Map<String, Object> attributeValueMap = nodePair.getValue().getChangedAttributes();
				final User user = clientController.getUser();

				for (Map.Entry<String, Object> entry : attributeValueMap.entrySet()) {
					clientController.webservice().changeNode(user.getUsername(), user.getAccessToken(), "5", nodePair.getKey().getID(), entry.getKey(), entry.getValue());
				}

				nodePair.getValue().updateCurrentState();
			}
		}

	}

	/**
	 * registers all listeners to react on necessary events like created nodes
	 * Might belong into a new plugin, which sends changes to the server
	 */
	private void registerListeners() {
		// mmapController().addMapLifeCycleListener(new MapLifeCycleListener());
		mmapController().addMapChangeListener(new MapChangeListener(this));
		mmapController().addNodeChangeListener(new NodeChangeListener(this));
		mmapController().addNodeSelectionListener(new INodeSelectionListener() {

			@Override
			public void onSelect(NodeModel node) {
				final NodeViewListener listener = new NodeViewListener(ClientController.this, node, null, null);
				node.addViewer(listener);
				selectedNodesMap.put(node, listener);
			}

			@Override
			public void onDeselect(NodeModel node) {
				final NodeViewListener listener = selectedNodesMap.remove(node);
				if (listener != null) {
					final Map<String, Object> attributeValueMap = listener.getChangedAttributes();

					for (Map.Entry<String, Object> entry : attributeValueMap.entrySet()) {
						webservice().changeNode(user.getUsername(), user.getAccessToken(), "5", node.getID(), entry.getKey(), entry.getValue());

					}

					node.removeViewer(listener);
				}

			}
		});
	}

	public void stop() {
		LogUtils.info("Shutting down client plugin...");
		Patterns.gracefulStop(applyChangeActor, Duration.create(5, TimeUnit.SECONDS), system());
		Patterns.gracefulStop(listenForUpdatesActor, Duration.create(5, TimeUnit.SECONDS), system());
		Patterns.gracefulStop(initCollaborationactor, Duration.create(5, TimeUnit.SECONDS), system());
		system.shutdown();
	}

	public static ModeController getModeController() {
		return MModeController.getMModeController();
	}

	public static MapIO getMapIO() {
		return getModeController().getExtension(MapIO.class);
	}

	public static MMapController mmapController() {
		return (MMapController) getModeController().getMapController();
	}

	public WS webservice() {
		return webservice;
	}

	public String source() {
		return sourceString;
	}

	public boolean isUpdating() {
		return isUpdating;
	}

	public void isUpdating(boolean value) {
		isUpdating = value;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public static String loggedInUserName() {
		return USER;
	}

	public ActorRef applyChangesActor() {
		return applyChangeActor;
	}

	public ActorRef listenForUpdatesActor() {
		return listenForUpdatesActor;
	}

	public Map<NodeModel, NodeViewListener> selectedNodesMap() {
		return selectedNodesMap;
	}

	public ActorSystem system() {
		return system;
	}

}
