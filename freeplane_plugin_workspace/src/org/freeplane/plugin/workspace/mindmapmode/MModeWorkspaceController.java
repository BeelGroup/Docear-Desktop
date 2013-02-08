package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JComponent;

import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.CollapseDirection;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.ComponentCollapseListener;
import org.freeplane.core.ui.components.ResizeEvent;
import org.freeplane.core.ui.components.ResizerListener;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.controller.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.controller.DefaultFileNodeIconHandler;
import org.freeplane.plugin.workspace.controller.LinkTypeFileIconHandler;
import org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.io.AFileNodeCreator;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.listener.DefaultWorkspaceComponentHandler;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class MModeWorkspaceController extends AWorkspaceModeExtension {
	
	abstract class ResizerEventAdapter implements ResizerListener, ComponentCollapseListener {
	}

	protected static final String WORKSPACE_VIEW_WIDTH = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.width";
	protected static final String WORKSPACE_VIEW_ENABLED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.enabled";
	protected static final String WORKSPACE_VIEW_COLLAPSED = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".view.collapsed";
	protected static final String WORKSPACE_MODEL_PROJECTS = MModeWorkspaceController.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".model.projects";
	protected static final String WORKSPACE_MODEL_PROJECTS_SEPARATOR = ",";
	
	
	private FileReadManager fileTypeManager;
	private TreeView view;
	private Properties settings;
	private WorkspaceModel wsModel;

	public MModeWorkspaceController(ModeController modeController) {
		super(modeController);		
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {			
			public void run() {
				String appName = Controller.getCurrentController().getResourceController().getProperty("ApplicationName").toLowerCase(Locale.ENGLISH);
				String settingsPath = System.getProperty("user.home")+ File.separator + "." + appName + File.separator + "users"+File.separator+"default";
				saveSettings(settingsPath);
			}
		}));
	}
	
	public void start(ModeController modeController) {
		setupSettings(modeController);
		setupModel(modeController);
		setupView(modeController);
	}
	
	private void setupSettings(ModeController modeController) {
		String settingsPath = WorkspaceController.getApplicationHome().getPath() + File.separator + "users"+File.separator+"default";
		loadSettings(settingsPath);
	}
	
	private void setupModel(ModeController modeController) {
		String[] projectsIds = settings.getProperty(WORKSPACE_MODEL_PROJECTS, "").split(WORKSPACE_MODEL_PROJECTS_SEPARATOR);
		for (String projectID : projectsIds) {
			String projectHome = settings.getProperty(projectID);
			if(projectHome == null) {
				continue;
			}
			AWorkspaceProject project = null;
			try {
				project = AWorkspaceProject.create(projectID, URI.create(projectHome));
				getModel().addProject(project);
				getProjectLoader().loadProject(project);
			}
			catch (Exception e) {
				LogUtils.severe(e);
				if(project != null) {
					getModel().removeProject(project);
				}
			}
		}	
	}

	private void setupView(ModeController modeController) {
		boolean expanded = true;
		try {
			expanded = !Boolean.parseBoolean(settings.getProperty(WORKSPACE_VIEW_COLLAPSED, "false"));
		}
		catch (Exception e) {
			// ignore -> default is true
		}
		
		OneTouchCollapseResizer otcr = new OneTouchCollapseResizer(Direction.LEFT, CollapseDirection.COLLAPSE_LEFT);
		otcr.addCollapseListener(getWorkspaceView());
		ResizerEventAdapter adapter = new ResizerEventAdapter() {
			
			public void componentResized(ResizeEvent event) {
				if(event.getSource().equals(getView())) {
					settings.setProperty(WORKSPACE_VIEW_WIDTH, String.valueOf(((JComponent) event.getSource()).getPreferredSize().width));
				}
			}

			public void componentCollapsed(ResizeEvent event) {
				if(event.getSource().equals(getView())) {
					settings.setProperty(WORKSPACE_VIEW_COLLAPSED, "true");
				}
			}

			public void componentExpanded(ResizeEvent event) {
				if(event.getSource().equals(getView())) {
					settings.setProperty(WORKSPACE_VIEW_COLLAPSED, "false");
				}
			}			
		};
		
		otcr.addResizerListener(adapter);
		otcr.addCollapseListener(adapter);
		
		Box resizableTools = Box.createHorizontalBox();
		resizableTools.add(getWorkspaceView());			
		resizableTools.add(otcr);
		otcr.setExpanded(expanded);
		modeController.getUserInputListenerFactory().addToolBar("workspace", ViewController.LEFT, resizableTools);
		getWorkspaceView().setModel(getModel());
		getView().expandPath(getModel().getRoot().getTreePath());
		
		getView().getNodeTypeIconManager().addNodeTypeIconHandler(LinkTypeFileNode.class, new LinkTypeFileIconHandler());
		getView().getNodeTypeIconManager().addNodeTypeIconHandler(DefaultFileNode.class, new DefaultFileNodeIconHandler());

				
	}
		
	private void loadSettings(String settingsPath) {
		final File userPropertiesFolder = new File(settingsPath);
		final File settingsFile = new File(userPropertiesFolder, "workspace.settings");
				
		settings = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(settingsFile);
			settings.load(in);
		}
		catch (final Exception ex) {
			LogUtils.info("Workspace settings not found, new file created");
			setupDefaultSettings();
		}
		finally {
			FileUtils.silentlyClose(in);
		}
	}
	
	private void setupDefaultSettings() {
		settings.setProperty(WORKSPACE_VIEW_WIDTH, "150");
		settings.setProperty(WORKSPACE_VIEW_ENABLED, "true");
		settings.setProperty(WORKSPACE_VIEW_COLLAPSED, "false");		
	}

	private void saveSettings(String settingsPath) {
		final File userPropertiesFolder = new File(settingsPath);
		final File settingsFile = new File(userPropertiesFolder, "workspace.settings");
		List<String> projectIDs = new ArrayList<String>();
		for(AWorkspaceProject project : getModel().getProjects()) {
			saveProject(project);
			if(projectIDs.contains(project.getProjectID())) {
				continue;
			}
			projectIDs.add(project.getProjectID());
			settings.setProperty(project.getProjectID(), project.getProjectHome().toString());			
		}
		StringBuilder sb = new StringBuilder();
		for (String prjId : projectIDs) {
			if(sb.length()>0) {
				sb.append(WORKSPACE_MODEL_PROJECTS_SEPARATOR);
			}
			sb.append(prjId);
		}
		settings.setProperty(WORKSPACE_MODEL_PROJECTS, sb.toString());
		OutputStream os = null;
		try {
			if(!settingsFile.exists()) {
				settingsFile.getParentFile().mkdirs();
				settingsFile.createNewFile();
			}		
			os = new FileOutputStream(settingsFile);
			settings.store(os, "user settings for the freeplane workspace");
		}
		catch (final Exception ex) {
			LogUtils.severe("Workspace settings could not be stored.", ex);
		}
		finally {
			FileUtils.silentlyClose(os);
		}
	}
	
	private void saveProject(AWorkspaceProject project) {
		try {
			getProjectLoader().storeProject(project);
		} catch (IOException e) {
			LogUtils.severe(e);
		}
		
	}

	private TreeView getWorkspaceView() {
		if (this.view == null) {
			this.view = new TreeView();
			this.view.addComponentListener(new DefaultWorkspaceComponentHandler(this.view));
			this.view.setMinimumSize(new Dimension(100, 40));
			int width = 150;
			try {
				width = Integer.parseInt(settings.getProperty(WORKSPACE_VIEW_WIDTH, "150"));
			}
			catch (Exception e) {
				// blind accept
			}
			this.view.setPreferredSize(new Dimension(width, 40));
		}
		return this.view;
	}
	
	public WorkspaceModel getModel() {
		if(wsModel == null) {
			wsModel = WorkspaceModel.createDefaultModel();
		}
		return wsModel;
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

	public URI getDefaultProjectHome() {
		//example c:\Users\joeran\Docear\joeran2\myfirstproject
		File home = new File(WorkspaceController.getApplicationHome());
		home = new File(home, "default");
		return  home.toURI();
	}

}
