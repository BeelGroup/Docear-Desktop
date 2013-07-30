package org.freeplane.plugin.workspace.features;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceModelExtensionWriterReader implements IExtensionAttributeWriter {

	private static final String PROJECT_ID_XML_TAG = "project";
	private static final String MAP_EXTENSION_XML_TAG = "map";
	private static final String PROJECT_HOME_XML_TAG = "project_last_home";

	private final Map<MapModel, TempProjectItem> tempProjectCache = new HashMap<MapModel, TempProjectItem>();
	
	private WorkspaceModelExtensionWriterReader(MapController mapController) {
		registerAttributeHandlers(mapController.getReadManager());
		mapController.getWriteManager().addExtensionAttributeWriter(WorkspaceMapModelExtension.class, this);
	}

	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(MAP_EXTENSION_XML_TAG, PROJECT_ID_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object map, String value) {
				final MapModel mapModel = (MapModel) map;
				
				WorkspaceMapModelExtension wmme = WorkspaceController.getMapModelExtension(mapModel); 
				if(wmme.getProject() == null) {
					updateProjectPathIndex(mapModel, value, null);
					AWorkspaceProject prj = WorkspaceController.getCurrentModel().getProject(value);
					if(prj == null) {
						return;
					}
					wmme.setProject(prj);
				}
			}			
		});
		
		reader.addAttributeHandler(MAP_EXTENSION_XML_TAG, PROJECT_HOME_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object map, String value) {
				final MapModel mapModel = (MapModel) map;
				updateProjectPathIndex(mapModel, null, URIUtils.createURI(value));
			}
		});
		
	}

	private void updateProjectPathIndex(MapModel key, String id, URI path) {
		synchronized (tempProjectCache) {
			TempProjectItem item = tempProjectCache.get(key);
			if(item == null) {
				item = new TempProjectItem();
				tempProjectCache.put(key, item);
			}
			
			if(id != null) {
				item.projectID = id;
			}
			
			if(path != null) {
				item.projectPath = path;
			}
			//index
			if(item.isComplete()) {
				WorkspaceMapModelExtension wmme = WorkspaceController.getMapModelExtension(key); 
				AWorkspaceProject project = WorkspaceController.getCachedProjectByID(item.projectID);
				if(wmme.getProject() == null) {
					if(project == null) {
						project = AWorkspaceProject.create(item.projectID, item.projectPath);
					}
					WorkspaceController.addMapToProject(key, project);
					WorkspaceController.indexProject(project);
				}
				else {
					WorkspaceController.indexProject(wmme.getProject());
				}
				
				tempProjectCache.remove(key);
			}
		}
	}

	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		final WorkspaceMapModelExtension wmme = extension != null ? (WorkspaceMapModelExtension) extension : WorkspaceController.getMapModelExtension(((NodeModel) userObject).getMap());
		AWorkspaceProject prj = wmme.getProject();
		
		if(prj == null) {
			return;
		}		
		writer.addAttribute(PROJECT_ID_XML_TAG, prj.getProjectID());
		writer.addAttribute(PROJECT_HOME_XML_TAG, prj.getProjectHome().toString());
	}

	public static void register(ModeController modeController) {
		new WorkspaceModelExtensionWriterReader(modeController.getMapController());
	}
	
	static class TempProjectItem {
		public String projectID = null;
		public URI projectPath = null;
		
		public boolean isComplete() {
			return projectID != null && projectPath != null;
		}
	}
}
