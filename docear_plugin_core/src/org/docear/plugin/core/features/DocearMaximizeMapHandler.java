package org.docear.plugin.core.features;

import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.ComponentCollapseListener;
import org.freeplane.core.ui.components.ResizeEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;

public class DocearMaximizeMapHandler implements IExtension, IDocearEventListener, ComponentCollapseListener {
	private static final String MAPVIEW_MAXIMIZED_PROPERTY = "mapview_maximized";
	
	private volatile boolean IS_IN_TOGGLE = false;
	private final Map<OneTouchCollapseResizer, Boolean> lastStateMap = new LinkedHashMap<OneTouchCollapseResizer, Boolean>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	private DocearMaximizeMapHandler(ModeController modeController) {
		ResourceController.getResourceController().setProperty(MAPVIEW_MAXIMIZED_PROPERTY, "false");
		WorkspaceController.replaceAction(modeController, new ToggleMapMaximizedAction());
	}

	/***********************************************************************************
	 * METHODS
	 * @return 
	 **********************************************************************************/
	public static DocearMaximizeMapHandler installMode(ModeController modeController) {
		DocearMaximizeMapHandler handler = modeController.getExtension(DocearMaximizeMapHandler.class);
		if(handler == null) {
			handler = new DocearMaximizeMapHandler(modeController);
			modeController.addExtension(DocearMaximizeMapHandler.class, handler);
		}
		return handler;
	}
	
	public static DocearMaximizeMapHandler getModeHandler() {
		return getModeHandler(Controller.getCurrentModeController());
	}
	
	public static DocearMaximizeMapHandler getModeHandler(ModeController modeController) {
		return modeController.getExtension(DocearMaximizeMapHandler.class);
	}
	
	public void addCollapsableResizer(OneTouchCollapseResizer resizer) {
		if(resizer == null) {
			return;
		}
		synchronized (lastStateMap) {
			if(!lastStateMap.containsKey(resizer)) {
				lastStateMap.put(resizer, resizer.isExpanded());
				resizer.addCollapseListener(this);
			}
		}
	}
	
	public void removeCollapsableResizer(OneTouchCollapseResizer resizer) {
		if(resizer == null) {
			return;
		}
		synchronized (lastStateMap) {
			if(lastStateMap.remove(resizer) != null) {
				resizer.removeCollapseListener(this);
			}
		}
	}
	
	public boolean isMapMaximizeEnabled() {
		return Boolean.parseBoolean(ResourceController.getResourceController().getProperty(MAPVIEW_MAXIMIZED_PROPERTY, "false"));
	}

	public void toggleMaximized() {
		Boolean max = !isMapMaximizeEnabled();
		setMapMaximized(max);
		ResourceController.getResourceController().setProperty(MAPVIEW_MAXIMIZED_PROPERTY, Boolean.toString(max));
	}

	private void setMapMaximized(boolean maximized) {
		IS_IN_TOGGLE = true;
		try {
			synchronized (lastStateMap) {
				boolean expand = !maximized;
				for (Entry<OneTouchCollapseResizer, Boolean> entry : lastStateMap.entrySet()) {
					if(!expand) {
						entry.getKey().setExpanded(false);
					}
					else {
						if(entry.getValue()) {
							entry.getKey().setExpanded(true);
						}
					}
				}
			}
		}
		finally {
			IS_IN_TOGGLE = false;
		}
	}

	private void updateState(OneTouchCollapseResizer resizer) {
		if(IS_IN_TOGGLE) {
			return;
		}
		synchronized (lastStateMap) {
			Boolean b = lastStateMap.get(resizer);
			if(b != null && b != resizer.isExpanded()) {
				lastStateMap.put(resizer, resizer.isExpanded());
			}
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void componentCollapsed(ResizeEvent event) {
		updateState((OneTouchCollapseResizer)event.getSource());
	}

	@Override
	public void componentExpanded(ResizeEvent event) {
		updateState((OneTouchCollapseResizer)event.getSource());
	}
	
	@Override
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.APPLICATION_CLOSING) {
			setMapMaximized(false);
		}
		else if(event.getType() == DocearEventType.APPLICATION_CLOSING_ABORTED) {
			setMapMaximized(isMapMaximizeEnabled());
		}
	}
	
	@SelectableAction(checkOnPropertyChange = MAPVIEW_MAXIMIZED_PROPERTY)
	public class ToggleMapMaximizedAction extends AFreeplaneAction {
		
		private static final long serialVersionUID = -2014522604202908914L;
		public static final String KEY = "ToggleMapMaximizedAction";
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public ToggleMapMaximizedAction() {
			super(KEY);
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		@Override
		public boolean isSelected() {
			return isMapMaximizeEnabled();
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		@Override
		public void actionPerformed(ActionEvent e) {
			toggleMaximized();
		}

	}
}
