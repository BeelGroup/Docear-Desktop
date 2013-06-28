package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

public class RibbonMapChangeAdapter implements INodeSelectionListener, INodeChangeListener, IMapChangeListener {
	private List<IChangeObserver> listeners = new ArrayList<IChangeObserver>();
	
	public void clear() {
		listeners.clear();
	}
	
	public void addListener(IChangeObserver listener) {
		synchronized (listeners) {
			if(!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}
	

	public void mapChanged(MapChangeEvent event) {
		fireSelectionChanged(event.getMap().getRootNode());		
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		// TODO Auto-generated method stub
		
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
		// TODO Auto-generated method stub
		
	}

	public void nodeChanged(NodeChangeEvent event) {
		fireSelectionChanged(event.getNode());
	}

	public void onDeselect(NodeModel node) {
		// TODO Auto-generated method stub
		
	}

	public void onSelect(NodeModel node) {
		fireSelectionChanged(node);
		
	}
	protected void fireSelectionChanged(NodeModel node) {
		synchronized (listeners) {
			for (IChangeObserver observer : listeners) {
				observer.updateState(node);
			}
		}
	}

}