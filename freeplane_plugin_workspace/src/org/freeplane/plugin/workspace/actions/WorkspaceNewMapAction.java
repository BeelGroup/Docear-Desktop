/**
 * author: Marcel Genzmehr
 * 14.12.2011
 */
package org.freeplane.plugin.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.edge.mindmapmode.AutomaticEdgeColorHook;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

/**
 * FIX for issue that a new mindmap is always set to <code>saved</code> by
 * default. This Action is used to set the new mindmap to <code>unsaved</code>
 * right after its creation.
 */
public class WorkspaceNewMapAction extends AFreeplaneAction {

	public static final String KEY = "NewMapAction";
	private static final long serialVersionUID = 1L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * 
	 */
	public WorkspaceNewMapAction() {
		super(KEY);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	
	public static MapModel createNewMap(AWorkspaceProject project) {
		return createNewMap(project, null, null, false);
	}
	
	public static MapModel createNewMap(final URI uri, String name, boolean save) {		
		return createNewMap(null, uri, name, save);
	}
	
	public static MapModel createNewMap(AWorkspaceProject project, URI uri, String name, boolean save) {
		if (uri == null) {
			save = false;
		}

		File f = URIUtils.getAbsoluteFile(uri);
		if (save) {
			if (!createFolderStructure(f)) {
				return null;
			}
		}
		final MMapIO mapIO = (MMapIO) MModeController.getMModeController().getExtension(MapIO.class);
		
		final MapModel map = new MMapModel();
		map.createNewRoot();

		if (name != null) {
			map.getRootNode().setText(name);
		}
		
		if(project != null) {
			WorkspaceController.getMapModelExtension(map).setProject(project);
		}
		 
		final ModeController modeController = Controller.getCurrentModeController();
		AutomaticEdgeColorHook al = (AutomaticEdgeColorHook) modeController.getExtension(AutomaticEdgeColorHook.class);
		al.undoableToggleHook(map.getRootNode());		
		
		if (save) {
			try {
				mapIO.writeToFile(map, f);
				map.setURL(Compat.fileToUrl(f));
			} catch (Exception e) {
				LogUtils.warn("Exception in org.freeplane.plugin.workspace.actions.WorkspaceNewMapAction.createNewMap(project, uri, name, save)..writeToFile: "+e.getMessage());
			}
		}
		
		return map;
	}
	
	@SuppressWarnings("deprecation")
	public static void openMap(URI path) throws IOException {
		try {
    		File file = URIUtils.getAbsoluteFile(path);
    		Controller.getCurrentModeController().getMapController().newMap(Compat.fileToUrl(file));
    		Controller.getCurrentController().getMapViewManager().setTitle();
		}
		catch (Exception cause) {
			throw new IOException(cause);
		}
	}

	private static boolean createFolderStructure(final File f) {
		final File folder = f.getParentFile();
		if (folder.exists()) {
			return true;
		}
		return folder.mkdirs();
	}
	
	public static void openNewMap() {
		final MMapIO mapIO = (MMapIO) MModeController.getMModeController().getExtension(MapIO.class);
		mapIO.newMapFromDefaultTemplate();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		openNewMap();
	}
}
