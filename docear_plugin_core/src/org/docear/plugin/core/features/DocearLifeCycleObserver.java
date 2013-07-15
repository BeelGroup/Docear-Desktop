package org.docear.plugin.core.features;

import java.awt.Component;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewChangeListener;

public class DocearLifeCycleObserver {

	public static enum ViewEventType {
		BEFORE_VIEW_CHANGED, AFTER_VIEW_CHANGED, AFTER_VIEW_CLOSED, AFTER_VIEW_CREATED
	}

	public static enum MapEventType {
		CREATED, REMOVED, SAVED_AS, SAVED

	}

	private ListenerAdapter listenerAdapter = new ListenerAdapter();
	private EventListenerList list = new EventListenerList();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearLifeCycleObserver(ModeController modeController) {
		modeController.getMapController().addMapLifeCycleListener(listenerAdapter);
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(listenerAdapter);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void addMapLifeCycleListener(IMapLifeCycleListener l) {
		list.add(InternalMapLifeCycleListenerWrapper.class, new InternalMapLifeCycleListenerWrapper(l));
	}
	
	public void removeMapLifeCycleListener(IMapLifeCycleListener l) {
		list.remove(InternalMapLifeCycleListenerWrapper.class, new InternalMapLifeCycleListenerWrapper(l));
	}
	
	public void addMapViewChangeListener(IMapViewChangeListener l) {
		list.add(InternalMapViewChangedListenerWrapper.class, new InternalMapViewChangedListenerWrapper(l));
	}
	
	public void removeMapViewChangeListener(IMapViewChangeListener l) {
		list.remove(InternalMapViewChangedListenerWrapper.class, new InternalMapViewChangedListenerWrapper(l));
	}

	public void fireMapEvent(MapEventType type, MapModel map) {
		InternalMapLifeCycleListenerWrapper[] listeners = list.getListeners(InternalMapLifeCycleListenerWrapper.class);
		if(listeners == null) {
			return;
		}
		for (int i = listeners.length-1; i >= 0; i -= 1) {
			InternalMapLifeCycleListenerWrapper wrapper = listeners[i];
			switch (type) {
			case CREATED:
				wrapper.getListener().onCreate(map);
				break;
			case REMOVED:
				wrapper.getListener().onRemove(map);
				break;
			case SAVED:
				wrapper.getListener().onSaved(map);
				break;
			case SAVED_AS:
				wrapper.getListener().onSavedAs(map);
				break;
			}
		}
	}

	public void fireViewEvent(ViewEventType type, Component oldView, Component newView) {
		InternalMapViewChangedListenerWrapper[] listeners = list.getListeners(InternalMapViewChangedListenerWrapper.class);
		if(listeners == null) {
			return;
		}
		for (int i = listeners.length-1; i >= 0; i -= 1) {
			InternalMapViewChangedListenerWrapper wrapper = listeners[i];
			switch (type) {
			case BEFORE_VIEW_CHANGED:
					wrapper.getListener().beforeViewChange(oldView, newView);
				break;
			case AFTER_VIEW_CHANGED:
					wrapper.getListener().afterViewChange(oldView, newView);
				break;
			case AFTER_VIEW_CREATED:
					wrapper.getListener().afterViewCreated(newView);
				break;
			case AFTER_VIEW_CLOSED:
					wrapper.getListener().afterViewClose(oldView);
				break;
			}
		}
		
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	private class InternalMapViewChangedListenerWrapper implements EventListener {
		private final IMapViewChangeListener l;
		public InternalMapViewChangedListenerWrapper(IMapViewChangeListener l) {
			this.l = l;
		}
		
		public IMapViewChangeListener getListener() {
			return l;
		}
		
		public int hashCode() {
			return l.hashCode();
		}
		
		public boolean equals(Object o) {
			if(o instanceof InternalMapViewChangedListenerWrapper) {
				return l.equals(((InternalMapViewChangedListenerWrapper) o).getListener());
			}
			return super.equals(o);
		}
	}
	
	private class InternalMapLifeCycleListenerWrapper implements EventListener {
		private final IMapLifeCycleListener l;
		public InternalMapLifeCycleListenerWrapper(IMapLifeCycleListener l) {
			this.l = l;
		}
		
		public IMapLifeCycleListener getListener() {
			return l;
		}
		
		public int hashCode() {
			return l.hashCode();
		}
		
		public boolean equals(Object o) {
			if(o instanceof InternalMapLifeCycleListenerWrapper) {
				return l.equals(((InternalMapLifeCycleListenerWrapper) o).getListener());
			}
			return super.equals(o);
		}
	}
	
	private class ListenerAdapter implements IMapLifeCycleListener, IMapViewChangeListener {
		@Override
		public void afterViewChange(Component oldView, Component newView) {
			fireViewEvent(ViewEventType.AFTER_VIEW_CHANGED, oldView, newView);
		}

		@Override
		public void afterViewClose(Component oldView) {
			fireViewEvent(ViewEventType.AFTER_VIEW_CLOSED, oldView, null);
		}

		@Override
		public void afterViewCreated(Component mapView) {
			fireViewEvent(ViewEventType.AFTER_VIEW_CREATED, null, mapView);
		}

		@Override
		public void beforeViewChange(Component oldView, Component newView) {
			fireViewEvent(ViewEventType.BEFORE_VIEW_CHANGED, oldView, newView);
		}

		@Override
		public void onCreate(MapModel map) {
			fireMapEvent(MapEventType.CREATED, map);
			
		}

		@Override
		public void onRemove(MapModel map) {
			fireMapEvent(MapEventType.REMOVED, map);
		}

		@Override
		public void onSavedAs(MapModel map) {
			fireMapEvent(MapEventType.SAVED_AS, map);
		}

		@Override
		public void onSaved(MapModel map) {
			fireMapEvent(MapEventType.SAVED, map);
		}
	}
}
