package org.freeplane.plugin.workspace.features;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class WorkspaceNodeSelectionHandler {
	
	final EventListenerList listeners = new EventListenerList();
	private final TreeSelectionListener treeListener = new TreeSelectionListener() {
		
		public void valueChanged(TreeSelectionEvent e) {
			fireSelectionChanged(e);			
		}
	};
	
	
	public TreeSelectionListener getTreeSelectionListener() {
		return this.treeListener ;
	}
	
	protected void fireSelectionChanged(TreeSelectionEvent e) {
		IWorkspaceNodeSelectionListener[] ls = listeners.getListeners(IWorkspaceNodeSelectionListener.class);
		for(int i=ls.length-1; i >= 0; i--) {
			ls[i].selectionChanged(e);
		}		
	}
	
	public void add(IWorkspaceNodeSelectionListener listener) {
		synchronized (listeners) {
			if(!containsListener(listener)) {
				listeners.add(IWorkspaceNodeSelectionListener.class, listener);
			}
		}
	}
	
	public void remove(IWorkspaceNodeSelectionListener listener) {
		synchronized (listeners) {
			listeners.remove(IWorkspaceNodeSelectionListener.class, listener);
		}
	}
	
	private boolean containsListener(IWorkspaceNodeSelectionListener listener) {
		for (IWorkspaceNodeSelectionListener l : listeners.getListeners(IWorkspaceNodeSelectionListener.class)) {
			if(l.equals(listener)) {
				return true;
			}
		}
		return false;
	}

}
