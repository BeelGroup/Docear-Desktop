package example;

import org.docear.messages.Messages.MindmapAsJsonReponse;
import org.docear.messages.Messages.MindmapAsJsonRequest;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.typesafe.config.ConfigFactory;

/**
 * Only demo class
 * @author Julius
 */
@SuppressWarnings("unused")
public class RemoteCommunicationExample {
	private ActorSystem system;
	private ActorRef remoteActor;
	private ActorRef localActor;

	public RemoteCommunicationExample() {
		system = ActorSystem.create("actoruser", ConfigFactory.load().getConfig("local"));
		remoteActor = system.actorFor("akka://freeplaneRemote@127.0.0.1:2553/user/main");
		localActor = system.actorOf(new Props(MyActor.class), "local");

		final MindmapAsJsonRequest request = new MindmapAsJsonRequest("source", "username", "<id of the map>");
		//Asynch call with own actor to handle (preferred)
		remoteActor.tell(request, localActor);

		//Asynch call without own actor (slower and harder to handle)
		final long tenSecondsInMillis = 10000;
		final Future<Object> future = Patterns.ask(remoteActor, request, tenSecondsInMillis);
		future.onSuccess(new OnSuccess<Object>() {
			@Override
			public void onSuccess(Object responseObject) throws Throwable {
				if(responseObject instanceof MindmapAsJsonReponse) {
					final MindmapAsJsonReponse response = (MindmapAsJsonReponse)responseObject;
					// do something with the response
				}	
			}}, system.dispatcher());
	}

	public static class MyActor extends UntypedActor {

		@Override
		public void onReceive(Object message) throws Exception {
			if(message instanceof MindmapAsJsonReponse) {
				final MindmapAsJsonReponse response = (MindmapAsJsonReponse)message;
				// do something with the message
			}

		}

	}
}