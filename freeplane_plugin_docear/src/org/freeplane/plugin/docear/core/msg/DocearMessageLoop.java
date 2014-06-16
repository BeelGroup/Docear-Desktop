package org.freeplane.plugin.docear.core.msg;

import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.freeplane.plugin.docear.core.DocearCore;
import org.freeplane.plugin.docear.core.concurrent.DocearConcurrencyController;
import org.freeplane.plugin.docear.core.extension.DocearExtension;
import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;

/**
 * @author genzmehr@docear.org
 *
 */
public final class DocearMessageLoop {	
	private final Thread loop;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	private final Vector<DocearMessageEnvelope> messageQueue = new Vector<DocearMessageEnvelope>();
	public DocearMessageLoop() {
		try {
			ThreadGroup group = DocearConcurrencyController.getInstance().createGroup("DOCEAR_MESSAGES");
		
			loop = DocearConcurrencyController.getInstance().newThread(group.getName(), getRunLoop());
			loop.setName("DocearMessageLoop");
			loop.setDaemon(false);
			loop.setPriority(Thread.NORM_PRIORITY);
			running.set(true);
			loop.start();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private Runnable getRunLoop() {
		return new Runnable() {
			
			public void run() {
				long lastCycle = 0;
				while(running.get()) {
					lastCycle++;
					while(running.get() && pendingMessages(lastCycle)) {
						DocearMessageEnvelope[] envelopes = peekMessages();
						for (int i = 0; i < envelopes.length && running.get(); i++) {
							DocearMessageEnvelope envelope = envelopes[i];
							
							//TODO try to deliver the message! filter flags etc.
							DocearExtensionIdentifier[] availableIDs = DocearCore.getInstance().getExtensionIDs();
							for (int j = 0; j < availableIDs.length && !envelope.getMessage().isConsumed(); j++) {
								DocearExtension ext = DocearCore.getInstance().getExtension(availableIDs[j]);
								ext.processMessage(envelope.getMessage());
								
							}
							
							if(envelope.isDelivered()) {
								removeMessage(envelope);
							}
							else {
								envelope.setLastCycle(lastCycle);
							}
						}
					}
					if(running.get()) {
						try {						
							loop.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		};
	}
	
	private void removeMessage(DocearMessageEnvelope envelope) {
		synchronized (messageQueue) {
			messageQueue.remove(envelope);
		}
	}
	
	private DocearMessageEnvelope[] peekMessages() {
		synchronized (messageQueue) {
			return messageQueue.toArray(new DocearMessageEnvelope[0]);
		}		
	}
	
	private boolean pendingMessages(long currentCycle) {
		synchronized(messageQueue) {
			for (int i = 0; i < messageQueue.size(); i++) {
				if(messageQueue.get(i).getLastCycle() < currentCycle) {
					return true;
				}
			}
					
			return false;
		}
	}
	
	public void dispatchMessage(DocearMessage msg) {
		DocearMessageEnvelope envelope = new DocearMessageEnvelope(msg);
		synchronized (messageQueue) {
			messageQueue.add(envelope);
		}
		loop.interrupt();
	}

	public void shutdown() {
		running.set(false);
		this.loop.interrupt();
	}
	
}
