package org.docear.plugin.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.freeplane.core.util.LogUtils;

public class DocearEventQueue {
	
	private final Thread dispatcher;
	private final Stack<DocearEvent> eventStack = new Stack<DocearEvent>();
	private final List<IDocearEventListener> eventListeners = Collections.synchronizedList(new ArrayList<IDocearEventListener>());
	
	private boolean stopDispatcher = false;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearEventQueue() {
		dispatcher = getDispatcherThread();
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private Thread getDispatcherThread() {
		return new Thread("Docear-EventQueue") {
			public void run() {
				while(!stopDispatcher) {
					DocearEvent event = null;
					while((event = popEvent()) != null) {
						try {
							if(event instanceof DocearRunnableEvent) {
								((DocearRunnableEvent) event).exec();
							}
							else {
								dispatchEvent(event);
							}
						}
						catch (Throwable e) {
							LogUtils.severe(e);
						}
						if(stopDispatcher) {
							return;
						}
					}
					try {
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
					}
				}
			}
		};
	}
	
	private DocearEvent popEvent() {
		synchronized (eventStack) {
			if(eventStack.empty()) {
				return null;
			}
			return eventStack.pop();
		}
	}
	public void invoke(DocearEvent event) {
		if(dispatcher.isAlive()) {
			synchronized (eventStack) {
				eventStack.add(event);
			}		
			synchronized(dispatcher)
			{
				dispatcher.notify();
			}
		}
	}
	
	public void invoke(Runnable task) {
		DocearEvent event = new DocearRunnableEvent(this, task);
		invoke(event);
	}
	
	public void addEventListener(IDocearEventListener listener) {
		synchronized (this.eventListeners) {
			if(this.eventListeners.contains(listener)) {
				return;
			}
			this.eventListeners.add(listener);
		}
	}
	
	public void removeEventListener(IDocearEventListener listener) {
		synchronized (this.eventListeners) {
			this.eventListeners.remove(listener);
		}
	}
	
	public void removeAllEventListeners() {
		synchronized (this.eventListeners) {
			this.eventListeners.clear();
		}
	}
	
	public synchronized void dispatchEvent(DocearEvent event) {
		synchronized (this.eventListeners) {
			IDocearEventListener[] listenerArray = this.eventListeners.toArray(new IDocearEventListener[0]);
			for(IDocearEventListener listener : listenerArray) {
				listener.handleEvent(event);
			}
		}
	}
	public void start() {
		if(!dispatcher.isAlive()) {
			dispatcher.start();
		}
		
	}
	
	public void stop() {
		stopDispatcher = true;
		if(dispatcher.isAlive()) {
			synchronized(dispatcher)
			{
				dispatcher.notify();
			}
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	

	/***********************************************************************************
	 * NESTED TYPES
	 **********************************************************************************/
	private static class DocearRunnableEvent extends DocearEvent {
		private static final long serialVersionUID = 1L;

		public DocearRunnableEvent(Object source, Runnable r) {
			super(source, r);
		}
		
		public void exec() {
			((Runnable)getEventObject()).run();
		}
	}
}
