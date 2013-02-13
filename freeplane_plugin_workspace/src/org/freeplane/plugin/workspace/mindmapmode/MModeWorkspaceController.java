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
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.FileNodeDeleteAction;
import org.freeplane.plugin.workspace.actions.FileNodeNewFileAction;
import org.freeplane.plugin.workspace.actions.FileNodeNewMindmapAction;
import org.freeplane.plugin.workspace.actions.NodeCopyAction;
import org.freeplane.plugin.workspace.actions.NodeCutAction;
import org.freeplane.plugin.workspace.actions.NodeNewFolderAction;
import org.freeplane.plugin.workspace.actions.NodeNewLinkAction;
import org.freeplane.plugin.workspace.actions.NodeOpenLocationAction;
import org.freeplane.plugin.workspace.actions.NodePasteAction;
import org.freeplane.plugin.workspace.actions.NodeRefreshAction;
import org.freeplane.plugin.workspace.actions.NodeRemoveAction;
import org.freeplane.plugin.workspace.actions.NodeRenameAction;
import org.freeplane.plugin.workspace.actions.PhysicalFolderSortOrderAction;
import org.freeplane.plugin.workspace.actions.WorkspaceCollapseAction;
import org.freeplane.plugin.workspace.actions.WorkspaceExpandAction;
import org.freeplane.plugin.workspace.actions.WorkspaceNewMapAction;
import org.freeplane.plugin.workspace.actions.WorkspaceNewProjectAction;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.creator.DefaultFileNodeCreator;
import org.freeplane.plugin.workspace.handler.DefaultFileNodeIconHandler;
import org.freeplane.plugin.workspace.handler.LinkTypeFileIconHandler;
import org.freeplane.plugin.workspace.io.AFileNodeCreator;
import org.freeplane.plugin.workspace.io.FileReadManager;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;
import org.freeplane.plugin.workspace.model.project.ProjectSelectionEvent;
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
	private AWorkspaceProject currentSelectedProject = null;
	private IProjectSelectionListener projectSelectionListener;

	public MModeWorkspaceController(ModeController modeController) {
		super(modeController);
	}
	
	public void start(ModeController modeController) {
		setupController(modeController);
		setupSettings(modeController);
		setupActions(modeController);
		setupModel(modeController);
		setupView(modeController);
	}
	
	private void setupController(ModeController modeController) {
		modeController.removeExtension(UrlManager.class);
		UrlManager.install(new MModeWorkspaceUrlManager());
		
		modeController.removeExtension(LinkController.class);
		LinkController.install(new MModeWorkspaceLinkController());		
	}

	private void setupSettings(ModeController modeController) {
		loadSettings(getSettingsPath());
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
		
	private void setupActions(ModeController modeController) {
		Controller controller = modeController.getController();
		controller.addAction(new WorkspaceExpandAction());
		controller.addAction(new WorkspaceCollapseAction());
		controller.addAction(new WorkspaceNewProjectAction());
		controller.addAction(new NodeNewFolderAction());
		controller.addAction(new NodeNewLinkAction());
		controller.addAction(new NodeOpenLocationAction());
		
		//FIXME: #332
		controller.addAction(new NodeCutAction());
		controller.addAction(new NodeCopyAction());
		controller.addAction(new NodePasteAction());
		controller.addAction(new NodeRenameAction());
		controller.addAction(new NodeRemoveAction());
		controller.addAction(new NodeRefreshAction());
//		
		controller.addAction(new WorkspaceNewMapAction());
		controller.addAction(new FileNodeNewMindmapAction());
		controller.addAction(new FileNodeNewFileAction());
		controller.addAction(new FileNodeDeleteAction());
		
		controller.addAction(new PhysicalFolderSortOrderAction());
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
			this.view.setMinimumSize(new Dimension(100, 40));
			int width = 150;
			try {
				width = Integer.parseInt(settings.getProperty(WORKSPACE_VIEW_WIDTH, "150"));
			}
			catch (Exception e) {
				// blindly accept
			}
			this.view.setPreferredSize(new Dimension(width, 40));
			this.view.addProjectSelectionListener(getProjectSelectionListener());
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
		File home = WorkspaceController.resolveFile(WorkspaceController.getApplicationHome());
		home = new File(home, "projects");
		return  home.toURI();
	}

	public void shutdown() {
		saveSettings(getSettingsPath());
	}
	
	private String getSettingsPath() {
		return WorkspaceController.resolveFile(WorkspaceController.getApplicationSettingsHome()).getPath() + File.separator + "users"+File.separator+"default";
	}

	private IProjectSelectionListener getProjectSelectionListener() {
		if(this.projectSelectionListener == null) {
			this.projectSelectionListener = new IProjectSelectionListener() {
				public void selectionChanged(ProjectSelectionEvent event) {
					LogUtils.info("now selected project: "+ event.getSelectedProject());
					currentSelectedProject = event.getSelectedProject();				
				}
			};
		}
		return this.projectSelectionListener;
	}
	
	@Override
	public AWorkspaceProject getCurrentProject() {
		return currentSelectedProject ;		
	}

}
