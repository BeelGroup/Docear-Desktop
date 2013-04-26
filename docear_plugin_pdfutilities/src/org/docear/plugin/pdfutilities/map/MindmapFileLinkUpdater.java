package org.docear.plugin.pdfutilities.map;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent.WorkspaceModelEventType;

public class MindmapFileLinkUpdater extends AMindmapUpdater {
	
	WorkspaceModelEvent event;
	Map<File, File> fileMap = new HashMap<File, File>();

	public MindmapFileLinkUpdater(String title, WorkspaceModelEvent event,	Map<File, File> fileMap) {
		super(title);		
		this.event = event;
		this.fileMap = fileMap;
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		if(map == null) return false;		
		return updateLinks(map.getRootNode());
	}

	private boolean updateLinks(NodeModel node) {
		if(node == null) return false;
		URI uri = URIUtils.getAbsoluteURI(node);
		File link = URIUtils.getFile(uri);
		if(link != null){
			if(fileMap.containsKey(link)){
				((MLinkController) LinkController.getController()).setLinkTypeDependantLink(node, fileMap.get(link));
				if(event != null && event.getType() == WorkspaceModelEventType.RENAMED && node.getText().equals(link.getName())){
					node.setText(fileMap.get(link).getName());
				}
				IAnnotation annotation = AnnotationController.getModel(node, false);
				if(annotation != null && annotation.getAnnotationID() != null && fileMap.containsKey(URIUtils.getFile(annotation.getAnnotationID().getUri()))){
					annotation.getAnnotationID().setId(fileMap.get(URIUtils.getFile(annotation.getAnnotationID().getUri())).toURI(), annotation.getAnnotationID().getObjectNumber());
				}
			}
		}
		for(NodeModel child : node.getChildren()){
			updateLinks(child);
		}
		return true;
	}
	
}
