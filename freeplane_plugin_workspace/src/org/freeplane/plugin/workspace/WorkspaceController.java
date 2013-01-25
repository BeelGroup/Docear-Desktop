package org.freeplane.plugin.workspace;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.controller.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.controller.ModeControlAlreadyRegisteredException;
import org.freeplane.plugin.workspace.io.FilesystemManager;
import org.freeplane.plugin.workspace.model.WorkspaceModel;

public final class WorkspaceController implements IExtension {
	
	public static final String WORKSPACE_RESOURCE_URL_PROTOCOL = "workspace";
	public static final String PROPERTY_RESOURCE_URL_PROTOCOL = "property";
	public static final String WORKSPACE_VERSION = "1.0";
	
//	public static final String SHOW_WORKSPACE_PROPERTY_KEY = "workspace.enabled";
//	public static final String COLLAPSE_WORKSPACE_PROPERTY_KEY = "workspace.collapsed";
//	public static final String WORKSPACE_WIDTH_PROPERTY_KEY = "workspace_view_width";
	
	private static WorkspaceController self;
	private static Map<Class<? extends ModeController>, Class<? extends AWorkspaceModeExtension>> modeWorkspaceCtrlMap = new HashMap<Class<? extends ModeController>, Class<? extends AWorkspaceModeExtension>>();
	
	private WorkspaceController(Controller controller) {
		self = this;
	}

	public static void install(Controller controller) {
		if(self == null) {
			new WorkspaceController(controller);
		}
		controller.addExtension(WorkspaceController.class, self);
	}
	
	public static WorkspaceController getController() {
		return self;
	}
	
	public static void registerWorkspaceModeExtension(Class<? extends ModeController> modeController, Class<? extends AWorkspaceModeExtension> modeWorkspaceCtrl) throws ModeControlAlreadyRegisteredException {
		synchronized (modeWorkspaceCtrlMap) {
			if(modeWorkspaceCtrlMap.containsKey(modeController)) {
				throw new ModeControlAlreadyRegisteredException(modeController);
			}
			modeWorkspaceCtrlMap.put(modeController, modeWorkspaceCtrl);
		}
	}
	
	public static void removeWorkspaceModeExtension(Class<? extends ModeController> modeController) {
		synchronized (modeWorkspaceCtrlMap) {
			modeWorkspaceCtrlMap.remove(modeController);
		}
	}

	public boolean installMode(ModeController modeController) {
		AWorkspaceModeExtension modeCtrl = modeController.getExtension(AWorkspaceModeExtension.class);
		if(modeCtrl == null) {
			Class<? extends AWorkspaceModeExtension> clazz = modeWorkspaceCtrlMap.get(modeController.getClass());
			if(clazz == null) {
				return false;
			}
			try {
				modeCtrl = clazz.getConstructor(ModeController.class).newInstance(modeController);
				modeController.addExtension(AWorkspaceModeExtension.class, modeCtrl);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		else {
			return true;
		}
		return false;
		
	}
	
	public static WorkspaceModel getCurrentModel() {
		return getCurrentModeExtension().getModel();
	}

	public static AWorkspaceModeExtension getCurrentModeExtension() {
		return Controller.getCurrentModeController().getExtension(AWorkspaceModeExtension.class);
	}
	
	public static FilesystemManager getFilesystemMgr() {
		return new FilesystemManager(getCurrentModeExtension().getFileTypeManager());
	}

	public static URI resolveURI(URI uri) {
		//TODO - get absolute uri
		return uri;
	}

	public static File resolveFile(URI path) {
		return new File(resolveURI(path));
	}
}
