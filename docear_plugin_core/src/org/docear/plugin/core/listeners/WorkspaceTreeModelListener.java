package org.docear.plugin.core.listeners;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;

public class WorkspaceTreeModelListener implements TreeModelListener {

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		
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
						if(str != null) {
							MapModel map = Controller.getCurrentController().getMapViewManager().getMaps().get(str);
							map.setSaved(false);
							map.setURL(null);
							Controller.getCurrentController().getViewController().setTitle();
						}
					} catch (MalformedURLException e) {
						LogUtils.warn(e);
					}					
				}						
			}			
		}
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		
	}

}
