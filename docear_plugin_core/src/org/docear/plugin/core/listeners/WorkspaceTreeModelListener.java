package org.docear.plugin.core.listeners;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.ApplicationResourceController;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.view.swing.map.MapView;

public class WorkspaceTreeModelListener implements TreeModelListener {

	@Override
	public void treeNodesChanged(TreeModelEvent event) {
//		if(event instanceof WorkspaceTreeModelEvent && ((WorkspaceTreeModelEvent) event).getType() == WorkspaceTreeModelEventType.rename) {		
//			File from = (File) ((WorkspaceTreeModelEvent) event).getFrom();
//			File to = (File) ((WorkspaceTreeModelEvent) event).getTo();
//			
//			if(((WorkspaceTreeModelEvent) event).getFrom() == null || from == null || to == null) {
//				return;
//			}
//			Object obj = event.getTreePath().getLastPathComponent();
//			if(obj instanceof DefaultFileNode) {
//				if(to.isDirectory()) {
//					//go through all subdirs and files
//					traversThrough(to, from);
//				}
//				else {
//					updateFileTracking(from, to);
//				}
//			}			
//			
//		}
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent event) {
		for(Object obj : event.getChildren()) {
			if(obj instanceof IFileSystemRepresentation) {
				File file = ((IFileSystemRepresentation) obj).getFile();
				if(file != null && file.getName().toLowerCase().endsWith(".mm")) {
					try {
						String str = Controller.getCurrentController().getMapViewManager().checkIfFileIsAlreadyOpened(file.toURI().toURL());
						//if map is open right now, remove url and clean all tracking lists 
						if(str != null) {
							MapModel map = Controller.getCurrentController().getMapViewManager().getMaps().get(str);
							removeFileTracking(map);
							//reset the url (save path)
							map.setURL(null);
							//set to unsaved 
							map.setSaved(false);
							//update the application title to show that the map is not saved
							Controller.getCurrentController().getMapViewManager().setTitle();
						}
					} catch (Throwable e) {
						LogUtils.warn(e);
					}					
				}						
			}			
		}
	}
	
	private void removeFileTracking(MapModel map) {
		//get all view to access the map view needed to clean the currently opened list
		List<Component> views = Controller.getCurrentController().getMapViewManager().getViews(map);
		ResourceController resCtrl = Controller.getCurrentController().getResourceController();
		if(resCtrl instanceof ApplicationResourceController) {
			//retrieve the internal map save name
			String name = ((ApplicationResourceController) resCtrl).getLastOpenedList().getRestoreable(map);
			//remove from last opened list
			((ApplicationResourceController) resCtrl).getLastOpenedList().remove(name);
			//go through all view to get to the map view
			for(Component comp : views) {
				if(comp instanceof MapView) {
					//use map view to remove the map from the currently opened list by simulating that it was closed 
					((ApplicationResourceController) resCtrl).getLastOpenedList().afterViewClose(comp);										
				}
			}
		}
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		
	}

}
