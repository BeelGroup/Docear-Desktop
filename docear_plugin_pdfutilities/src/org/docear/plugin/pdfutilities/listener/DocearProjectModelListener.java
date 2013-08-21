package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.map.MindmapFileLinkUpdater;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.WorkspaceModelEventType;
import org.freeplane.plugin.workspace.model.project.IProjectModelListener;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class DocearProjectModelListener implements IProjectModelListener {
	
	private void updateMaps(WorkspaceModelEvent event) {
		Map<File, File> fileMap = new HashMap<File, File>();
		if(!((File)event.getNewValue()).isDirectory()){				
			File oldFile = (File) event.getOldValue();
			File newFile = (File) event.getNewValue();			
			fileMap.put(oldFile, newFile);			
		}
		else{
			File oldFile = (File) event.getOldValue();
			File newFile = (File) event.getNewValue();
			Collection<File> files = FileUtils.listFiles(newFile, null, true);			
			for(File file : files){
				String oldPath = file.getPath().replace(newFile.getPath(), oldFile.getPath());
				fileMap.put(new File(oldPath), file);				
			}			
		}
		updateMaps(event, fileMap); 
	}

	private void updateMaps(WorkspaceModelEvent event,	Map<File, File> fileMap) {
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
		mindmapUpdateController.addMindmapUpdater(new MindmapFileLinkUpdater(TextUtils.getText("updating_links"), event, fileMap));
		mindmapUpdateController.updateCurrentMindmap();//updateAllMindmapsInWorkspace();
	}

	public void treeNodesChanged(WorkspaceModelEvent event) {
		if(event.getType() == WorkspaceModelEventType.RENAMED){
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode) { 
				try { 
					DefaultFileNode target = (DefaultFileNode) event.getTreePath().getLastPathComponent();
					File parent = target.getFile().getParentFile();
					File oldFile = new File(parent, (String) event.getOldValue());
					File newFile = new File(parent, (String) event.getNewValue());
					 
					AnnotationController.getController().updateIndex(newFile, oldFile);
					updateMaps(event);
				}
				catch (Exception e) {
					DocearLogger.warn(e);
				}
			}
		}
	}

	public void treeNodesInserted(WorkspaceModelEvent event) {
		
	}

	public void treeNodesRemoved(WorkspaceModelEvent event) {
		
	}

	public void treeStructureChanged(WorkspaceModelEvent event) {
		if(event.getType() == WorkspaceModelEventType.MOVED){
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode || event.getTreePath().getLastPathComponent() instanceof LinkTypeFileNode){
				try {
					File oldFile = null;
					File newFile = null;
					if(event.getType() == WorkspaceModelEventType.RENAMED) {
						DefaultFileNode target = (DefaultFileNode) event.getTreePath().getLastPathComponent();
						File parent = target.getFile().getParentFile();
						oldFile = new File(parent, (String) event.getOldValue());
						newFile = new File(parent, (String) event.getNewValue());
					} 
					else if(event.getType() == WorkspaceModelEventType.MOVED) {
						newFile = (File) event.getNewValue();
						oldFile = (File) event.getOldValue();
					}
					AnnotationController.getController().updateIndex(newFile, oldFile);
					updateMaps(event);
				}
				catch (Exception e) {
					DocearLogger.warn(e);
				}
			}
		}
	}
}
