package org.docear.plugin.core.listeners;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.application.ApplicationResourceController;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent.WorkspaceTreeModelEventType;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.view.swing.map.MapView;

public class WorkspaceTreeModelListener implements TreeModelListener {

	@Override
	public void treeNodesChanged(TreeModelEvent event) {
		if(event instanceof WorkspaceTreeModelEvent && ((WorkspaceTreeModelEvent) event).getType() == WorkspaceTreeModelEventType.rename) {		
			File from = (File) ((WorkspaceTreeModelEvent) event).getFrom();
			File to = (File) ((WorkspaceTreeModelEvent) event).getTo();
			
			if(true || ((WorkspaceTreeModelEvent) event).getFrom() == null || from == null || to == null) {
				return;
			}
			Object obj = event.getTreePath().getLastPathComponent();
			if(obj instanceof DefaultFileNode) {
				if(to.isDirectory()) {
					//go through all subdirs and files
					traversThrough(to, from);
				}
				else {
					updateFileTracking(from, to);
				}
			}			
			
		}
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
							Controller.getCurrentController().getViewController().setTitle();
						}
					} catch (Throwable e) {
						LogUtils.warn(e);
					}					
				}						
			}			
		}
	}

	private void traversThrough(File dir, File from) {
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if(pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".mm")) {
					return true;
				}
				return false;
			}
		});
		for (File file : files) {
			if(file.isDirectory()) {
				traversThrough(file, new File(from, file.getName()));
			}
			else {
				updateFileTracking(new File(from, file.getName()), file);
			}
		}
		
	}
	
	private void updateFileTracking(File from, File to) {
		if(from != null && from.getName().toLowerCase().endsWith(".mm")) {
			//doesn't work because the remove clears the url before the renamed event is called
			try {
//				Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
//				//if map is open right now, remove url and clean all tracking lists 
//				if(str != null) {
//					MapModel map = Controller.getCurrentController().getMapViewManager().getMaps().get(str);
//					removeFileTracking(map);
//					final URL urlBefore = map.getURL();
//					map.setURL(to.toURI().toURL());
//					final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
//					mapController.fireMapChanged(new MapChangeEvent(this, map, UrlManager.MAP_URL, urlBefore, map.getURL()));
//				}
			} catch (Throwable e) {
				LogUtils.warn(e);
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
