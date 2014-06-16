package org.freeplane.plugin.docear.core.concurrent;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author genzmehr@docear.org
 *
 */
public final class DocearConcurrencyController {
	private static DocearConcurrencyController singleton;
	public static final ThreadGroup DEFAULT_GROUP = new ThreadGroup("docear_default_group");
	
	private Vector<ThreadGroup> threadGroups = new Vector<ThreadGroup>();
	
	private java.util.concurrent.locks.ReentrantLock LOCK = new ReentrantLock(true);
	private ClassLoader defaultClassLoader = null;
	
	private DocearConcurrencyController() {
	}
	
	public static synchronized DocearConcurrencyController getInstance() {
		if(singleton == null) {
			singleton = new DocearConcurrencyController();
		}
		return singleton;
	}
		
	public ThreadGroup createGroup(String name) throws GroupAlreadyExistsException {
		return createGroup((ThreadGroup)null, name);
	}
	
	public ThreadGroup createGroup(String parentGroup, String name) throws GroupAlreadyExistsException {
		LOCK.lock();
		try {
			ThreadGroup parent = getGroup(parentGroup);
			return createGroup(parent, name);
		}
		finally {
			LOCK.unlock();
		}
	}
	
	public ThreadGroup createGroup(ThreadGroup parent, String name) throws GroupAlreadyExistsException {
		LOCK.lock();
		try {
			ThreadGroup group = getGroup(name);
			if(group != null) {
				throw new GroupAlreadyExistsException();
			}
			
			if(parent != null) {
				group = new ThreadGroup(parent, name);
			}
			else {
				group = new ThreadGroup(name);
			}
			
			threadGroups.add(group);
					
			return group;
		}
		finally {
			LOCK.unlock();
		}
	}
	
	public ThreadGroup getGroup(String name) {
		LOCK.lock();
		try {
			for (int i = 0; i < threadGroups.size(); i++) {
				if(threadGroups.get(i).getName().equals(name)) {
					return threadGroups.get(i);
				}				
			}
			return null;}
		finally {
			LOCK.unlock();
		}
	}

	public Thread newThread(String groupName, Runnable target) {
		if(groupName == null || target == null) {
			throw new IllegalArgumentException("NULL passed to newThread(groupName, target)");
		}
		ThreadGroup group = getGroup(groupName);
		if(group == null) {
			throw new NullPointerException("group with name '"+group+"' does not exists");
		}
		Thread t = new Thread(group, target);
		t.setContextClassLoader(getDefaultClassLoader());
		return t;
	}

	public synchronized ClassLoader getDefaultClassLoader() {
		return defaultClassLoader ;
	}
	
	public synchronized ClassLoader setDefaultClassLoader(ClassLoader cl) {
		ClassLoader old = defaultClassLoader;
		defaultClassLoader = cl;
		return old;
	}
}
