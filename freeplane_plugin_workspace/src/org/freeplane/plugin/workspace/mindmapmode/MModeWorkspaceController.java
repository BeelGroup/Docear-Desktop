package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Box;

import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.CollapseDirection;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.controller.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.io.AFileNodeCreator;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.io.FilesystemManager;
import org.freeplane.plugin.workspace.listener.DefaultWorkspaceComponentHandler;
import org.freeplane.plugin.workspace.model.WorkspaceModel;

public class MModeWorkspaceController extends AWorkspaceModeExtension {
	
	private FileReadManager fileTypeManager;
	private TreeView view;

	public MModeWorkspaceController(ModeController modeController) {
		super(modeController);
		
		
		
		
		Box resizableTools = Box.createHorizontalBox();
		resizableTools.add(getWorkspaceView());
		resizableTools.add(new OneTouchCollapseResizer(Direction.LEFT, CollapseDirection.COLLAPSE_LEFT));
		modeController.getUserInputListenerFactory().addToolBar("workspace", ViewController.LEFT, resizableTools);
	}
		
	private TreeView getWorkspaceView() {
		if (this.view == null) {
			this.view = new TreeView();
			this.view.addComponentListener(new DefaultWorkspaceComponentHandler(this.view));
			this.view.setMinimumSize(new Dimension(100, 40));
			this.view.setPreferredSize(new Dimension(100, 40));
		}
		return this.view;
	}
	
	public WorkspaceModel getModel() {
		return null;
	}


	@Override
	public IWorkspaceView getView() {
		return getWorkspaceView();
	}
	
	public FileReadManager getFileTypeManager() {
		if (this.fileTypeManager == null) {
			this.fileTypeManager = new FileReadManager();
			Properties props = new Properties();
			try {
				props.load(this.getClass().getResourceAsStream("/conf/filenodetypes.properties"));

				Class<?>[] args = {};
				for (Object key : props.keySet()) {
					try {
						Class<?> clazz = DefaultFileNodeCreator.class;
						
						clazz = this.getClass().getClassLoader().loadClass(key.toString());

						AFileNodeCreator handler = (AFileNodeCreator) clazz.getConstructor(args).newInstance();
						handler.setFileTypeList(props.getProperty(key.toString(), ""), "\\|");
						this.fileTypeManager.addFileHandler(handler);
					}
					catch (ClassNotFoundException e) {
						LogUtils.warn("Class not found [" + key + "]", e);
					}
					catch (ClassCastException e) {
						LogUtils.warn("Class [" + key + "] is not of type: PhysicalNode", e);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.fileTypeManager;
	}

}
