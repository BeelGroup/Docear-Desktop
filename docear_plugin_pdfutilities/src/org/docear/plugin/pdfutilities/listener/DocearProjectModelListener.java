package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.map.MindmapFileLinkUpdater;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.ProjectModelEventType;
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
		if(event.getType() == ProjectModelEventType.RENAMED){
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode){
				updateMaps(event);
			}
		}
	}

	public void treeNodesInserted(WorkspaceModelEvent event) {
		
	}

	public void treeNodesRemoved(WorkspaceModelEvent event) {
		
	}

	public void treeStructureChanged(WorkspaceModelEvent event) {
		if(event.getType() == ProjectModelEventType.MOVED){
			if(event.getTreePath().getLastPathComponent() instanceof DefaultFileNode || event.getTreePath().getLastPathComponent() instanceof LinkTypeFileNode){
				updateMaps(event);
			}
		}
	}
}
