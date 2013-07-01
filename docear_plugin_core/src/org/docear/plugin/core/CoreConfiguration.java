package org.docear.plugin.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOExceptionWithCause;
import org.docear.plugin.core.actions.DocearAboutAction;
import org.docear.plugin.core.actions.DocearOpenUrlAction;
import org.docear.plugin.core.actions.DocearQuitAction;
import org.docear.plugin.core.actions.DocearSetNodePrivacyAction;
import org.docear.plugin.core.actions.DocearShowDataPrivacyStatementAction;
import org.docear.plugin.core.actions.DocearShowDataProcessingTermsAction;
import org.docear.plugin.core.actions.DocearShowTermsOfUseAction;
import org.docear.plugin.core.actions.SaveAction;
import org.docear.plugin.core.actions.SaveAsAction;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearMapWriter;
import org.docear.plugin.core.features.DocearNodeModifiedExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController;
import org.docear.plugin.core.listeners.DocearCoreOmniListenerAdapter;
import org.docear.plugin.core.listeners.MapLifeCycleAndViewListener;
import org.docear.plugin.core.listeners.PropertyListener;
import org.docear.plugin.core.listeners.PropertyLoadListener;
import org.docear.plugin.core.listeners.WorkspaceOpenDocumentListener;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.ui.ribbons.DocearNodePrivacyContributorFactory;
import org.docear.plugin.core.workspace.actions.DocearAddRepositoryPathAction;
import org.docear.plugin.core.workspace.actions.DocearImportProjectAction;
import org.docear.plugin.core.workspace.actions.DocearLibraryNewMindmap;
import org.docear.plugin.core.workspace.actions.DocearLibraryOpenLocation;
import org.docear.plugin.core.workspace.actions.DocearNewProjectAction;
import org.docear.plugin.core.workspace.actions.DocearRemoveRepositoryPathAction;
import org.docear.plugin.core.workspace.actions.DocearRenameAction;
import org.docear.plugin.core.workspace.controller.DocearProjectLoader;
import org.docear.plugin.core.workspace.model.DocearWorspaceProjectCreator;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.ConfigurationUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.help.OnlineDocumentationAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IControllerExecuteExtension;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.MapConversionException;
import org.freeplane.features.url.MapVersionInterpreter;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.mindmapmode.FileFolderDropHandler;
import org.freeplane.plugin.workspace.mindmapmode.VirtualFolderDropHandler;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;


public class CoreConfiguration extends ALanguageController {

	private static final String DOCEAR = "Docear";
	private static final String APPLICATION_NAME = "ApplicationName";
	private static final String DOCUMENTATION_ACTION = "DocumentationAction";
	private static final String DOCEAR_WEB_DOCU_LOCATION = "docear_webDocuLocation";
	private static final String WEB_DOCU_LOCATION = "webDocuLocation";
	private static final String REQUEST_FEATURE_ACTION = "RequestFeatureAction";
	private static final String DOCEAR_FEATURE_TRACKER_LOCATION = "docear_featureTrackerLocation";
	private static final String FEATURE_TRACKER_LOCATION = "featureTrackerLocation";
	private static final String ASK_FOR_HELP = "AskForHelp";
	private static final String HELP_FORUM_LOCATION = "helpForumLocation";
	private static final String REPORT_BUG_ACTION = "ReportBugAction";
	private static final String DOCEAR_BUG_TRACKER_LOCATION = "docear_bugTrackerLocation";
	private static final String BUG_TRACKER_LOCATION = "bugTrackerLocation";
	private static final String OPEN_FREEPLANE_SITE_ACTION = "OpenFreeplaneSiteAction";
	private static final String WEB_DOCEAR_LOCATION = "webDocearLocation";
	private static final String WEB_FREEPLANE_LOCATION = "webFreeplaneLocation";
	

	public static final String DOCUMENT_REPOSITORY_PATH = "@@literature_repository@@";
	public static final String LIBRARY_PATH = "@@library_mindmaps@@"; 
	private IControllerExecuteExtension docearExecutor; 
		
	public CoreConfiguration() {			
		LogUtils.info("org.docear.plugin.core.CoreConfiguration() initializing...");
	}
	
	protected void initController(Controller controller) {
		Controller.getCurrentController().addExtension(IControllerExecuteExtension.class, getDocearCommandExecutor());
		loadAndStoreVersion(controller);
		adjustProperties(controller);
		
		AWorkspaceProject.setCurrentProjectCreator(new DocearWorspaceProjectCreator());
		if(DocearController.getPropertiesController().getProperty("ApplicationName", "Docear").equals("Docear")) {
			try {
			DefaultFileNode.setApplicationIcon(new ImageIcon(CoreConfiguration.class.getResource("/images/docear16.png")));
			}
			catch (Exception e) {
				LogUtils.warn("ERROR: default file application icon has not been replaced");
			}
		}
		
		WorkspaceController.replaceAction(new DocearAboutAction());
		WorkspaceController.replaceAction(new DocearQuitAction());
		WorkspaceController.replaceAction(new DocearImportProjectAction());
		copyInfoIfNecessary();		
	}

	private IControllerExecuteExtension getDocearCommandExecutor() {
		if(docearExecutor == null) {
			docearExecutor = new IControllerExecuteExtension() {
				
				@Override
				public void exec(String command, boolean waitFor) throws IOException {
					if (Compat.isWindowsOS()) {
						LogUtils.info("using jna to execute " + command);
						windowsNativeExec(command, waitFor);
					}
					else {
						LogUtils.info("execute " + command);
						Process proc = Runtime.getRuntime().exec(command);
						waiting(waitFor, proc);
					}
				}
				
				public void exec(String[] command, boolean waitFor) throws IOException {
					if (Compat.isWindowsOS()) {
						String commandString = command[0];
						for (int i=1; i<command.length; i++) {
							commandString += " " + command[i];
						}
						LogUtils.info("using jna to execute: " + commandString);
						try {
							windowsNativeExec(commandString, waitFor);
						} catch (Exception e) {
							throw new IOException(e.getMessage()+" for command: "+commandString);
						}
					}
					else {
						LogUtils.info("execute: " + Arrays.toString(command));
						Process proc = Runtime.getRuntime().exec(command);
						waiting(waitFor, proc);
					}
				}
				
				private void windowsNativeExec(String command, boolean waitFor) throws IllegalStateException {
					WinBase.PROCESS_INFORMATION.ByReference processInfo = new WinBase.PROCESS_INFORMATION.ByReference();
					WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();

					try {
			    		if (!Kernel32.INSTANCE.CreateProcess(
			    		    null,           // Application name, not needed if supplied in command line
			    		    command,        // Command line
			    		    null,           // Process security attributes
			    		    null,           // Thread security attributes
			    		    true,           // Inherit handles
			    		    new WinDef.DWORD(0) ,              // Creation flags
			    		    null,           // Environment
			    		    null,           // Directory
			    		    startupInfo,
			    		    processInfo)) {
			    		    throw new IllegalStateException("Error creating process. Last error: " +
			    		        Kernel32.INSTANCE.GetLastError());
			    		}
			    
			    		if (waitFor) {
			    			Kernel32.INSTANCE.WaitForSingleObject(processInfo.hProcess, Kernel32.INFINITE);
			    		}
					}
					finally {
			    		// The CreateProcess documentation indicates that it is very important to 
			    		// close the returned handles
			    		Kernel32.INSTANCE.CloseHandle(processInfo.hThread);
			    		Kernel32.INSTANCE.CloseHandle(processInfo.hProcess);
					}
				}
				
				private void waiting(boolean waitFor, Process proc)
						throws IOExceptionWithCause {
					if(waitFor) {
						try {
							proc.waitFor();
						} catch (InterruptedException e) {
							throw new IOExceptionWithCause(e);
						}
					}
				}
				
			};
		}
		return docearExecutor;
	}

	private void copyInfoIfNecessary() {	
		File _welcomeFile = new File(URIUtils.getFile(WorkspaceController.getApplicationSettingsHome()), "docear-welcome.mm");
		if(!_welcomeFile.exists()) {
			createAndCopy(_welcomeFile, "/conf/docear-welcome.mm");			
		}
		
		File _docearLogo = new File(URIUtils.getFile(WorkspaceController.getApplicationSettingsHome()), "docear-logo.png");
		if(!_docearLogo.exists()) {
			createAndCopy(_docearLogo, "/images/docear_logo.png");			
		}
		
		ResourceController resController = Controller.getCurrentController().getResourceController();
		if (resController.getProperty("ApplicationName").equals("Docear")) {
			String mapPath = _welcomeFile.toURI().getPath();
			resController.setProperty("first_start_map", mapPath);
			resController.setProperty("tutorial_map", mapPath);
		}
	}
	
	private void createAndCopy(File file, String resourcePath) {
		try {
			createFile(file);
			FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream(resourcePath), file);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}	
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	private void createFile(File file) throws IOException {
		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			return;
		}
		file.createNewFile();
	}
	
	protected void initMode(ModeController modeController) {
		WorkspaceController.replaceAction(new DocearAddRepositoryPathAction());
		WorkspaceController.replaceAction(new DocearRemoveRepositoryPathAction());
		WorkspaceController.replaceAction(new DocearLibraryOpenLocation());
		WorkspaceController.replaceAction(new DocearNewProjectAction());
		WorkspaceController.replaceAction(new DocearLibraryNewMindmap());
		
		DocearProjectLoader docearProjectLoader = new DocearProjectLoader();
		WorkspaceController.getModeExtension(modeController).setProjectLoader(docearProjectLoader);
		
		IWorkspaceView view = WorkspaceController.getModeExtension(modeController).getView();
		if(view != null) {
			view.getTransferHandler().registerNodeDropHandler(FolderTypeLibraryNode.class, new VirtualFolderDropHandler());
			view.getTransferHandler().registerNodeDropHandler(LiteratureRepositoryPathNode.class, new FileFolderDropHandler());
		}
		
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.APPLICATION_STARTED);
		Toolkit.getDefaultToolkit();		
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OS_OPERATING_SYSTEM, System.getProperty("os.name"));
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OS_LANGUAGE_CODE, System.getProperty("user.language"));
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OS_COUNTRY_CODE, System.getProperty("user.country"));
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OS_TIME_ZONE, System.getProperty("user.timezone"));
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OS_SCREEN_RESOLUTION, Toolkit.getDefaultToolkit().getScreenSize().toString());
		
		MapVersionInterpreter.addMapVersionInterpreter(new MapVersionInterpreter("Docear", 1, "docear", false, false, "Docear", "url", null, new org.freeplane.features.url.IMapConverter() {
			public void convert(NodeModel root) throws MapConversionException {
				final MapModel mapModel = root.getMap();				
				DocearMapModelExtension docearMapModel = mapModel.getExtension(DocearMapModelExtension.class);
				if (docearMapModel == null) {
					DocearMapModelController.setModelWithCurrentVersion(mapModel);
				}		
			}
		}));
		
		// set up context menu for workspace
		//WORKSPACE - info: test if this works without
//		WorkspaceController.getController().addWorkspaceListener(WORKSPACE_CHANGE_LISTENER);		
		
		addPluginDefaults(Controller.getCurrentController());
		addMenus(modeController);
		
		registerListeners(modeController);
		//prepareWorkspace();
		
		replaceFreeplaneStringsAndActions(modeController);
		DocearMapModelController.install(new DocearMapModelController(modeController));
		
		setDocearMapWriter(modeController);
		
		registerController(modeController);
		UrlManager.getController().setLastCurrentDir(URIUtils.getAbsoluteFile(WorkspaceController.getModeExtension(modeController).getDefaultProjectHome()));	
	}
	
	private void loadAndStoreVersion(Controller controller) {
		//DOCEAR: has to be called before the splash is showing
		final Properties versionProperties = new Properties();
		InputStream in = null;
		try {
			in = this.getClass().getResource("/version.properties").openStream();
			versionProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		final Properties buildProperties = new Properties();
		in = null;
		try {
			in = this.getClass().getResource("/build.number").openStream();
			buildProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		final String versionNumber = versionProperties.getProperty("docear_version");
		final String versionStatus = versionProperties.getProperty("docear_version_status");
		final String versionStatusNumber = versionProperties.getProperty("docear_version_status_number");
		final int versionBuild = Integer.parseInt(buildProperties.getProperty("build.number")) -1;
		controller.getResourceController().setProperty("docear_version", versionNumber);
		controller.getResourceController().setProperty("docear_status", versionStatus+" "+versionStatusNumber+" build "+versionBuild);
		
	}
	
	private void adjustProperties(Controller controller) {
		final Properties coreProperties = new Properties();
		InputStream in = null;
		try {
			in = this.getClass().getResource("/core.properties").openStream();
			coreProperties.load(in);
		}
		catch (final IOException e) {			
		}
		
		ResourceController resController = controller.getResourceController();
		
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		resController.addDefaults(defaults);
		
		resController.setProperty(WEB_FREEPLANE_LOCATION, coreProperties.getProperty(WEB_DOCEAR_LOCATION));
		resController.setProperty(BUG_TRACKER_LOCATION, coreProperties.getProperty(DOCEAR_BUG_TRACKER_LOCATION));
		resController.setProperty(HELP_FORUM_LOCATION, coreProperties.getProperty("docear_helpForumLocation"));
		resController.setProperty(FEATURE_TRACKER_LOCATION, coreProperties.getProperty(DOCEAR_FEATURE_TRACKER_LOCATION));
		resController.setProperty(WEB_DOCU_LOCATION, coreProperties.getProperty(DOCEAR_WEB_DOCU_LOCATION));
		resController.setProperty("docu-online", "http://www.docear.org/wp-content/uploads/2012/04/docear-welcome.mm");
		
//		if (resController.getProperty("ApplicationName").equals("Docear")) {
//			resController.setProperty("first_start_map", "/doc/docear-welcome.mm");
//			resController.setProperty("tutorial_map", "/doc/docear-welcome.mm");
//		}
		
		
		if (!resController.getProperty(APPLICATION_NAME, "").equals(DOCEAR)) {
			return;
		}

		//replace if application name is docear
		replaceResourceBundleStrings();
	}
		
	private void addMenus(ModeController modeController) {
		//RIBBONS implement
		if("true".equals(System.getProperty("docear.debug", "false"))) {
			modeController.addAction(new DocearSetNodePrivacyAction());
		}
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				//add entries to project menu
				final String MENU_PROJECT_KEY = "/menu_bar/project";
				builder.addSeparator(MENU_PROJECT_KEY, MenuBuilder.AS_CHILD);
				final AWorkspaceAction addAction = new DocearAddRepositoryPathAction();
				builder.addAction(MENU_PROJECT_KEY, addAction, MenuBuilder.AS_CHILD);
				final AWorkspaceAction removeAction = new DocearRemoveRepositoryPathAction();
				builder.addAction(MENU_PROJECT_KEY, removeAction, MenuBuilder.AS_CHILD);
				Object obj = builder.get(MENU_PROJECT_KEY).getUserObject();
				if(obj instanceof JMenu) {
					((JMenu) obj).getPopupMenu().addPopupMenuListener(new PopupMenuListener() {					
						public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
							addAction.setEnabled();
							removeAction.setEnabled();
						}
						
						public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
						
						public void popupMenuCanceled(PopupMenuEvent e) {}
					});
				}
				
				//add entries to
				builder.addAction("/menu_bar/help", new DocearShowTermsOfUseAction(),	MenuBuilder.AS_CHILD);
				builder.addAction("/menu_bar/help", new DocearShowDataPrivacyStatementAction(),	MenuBuilder.AS_CHILD);
				builder.addAction("/menu_bar/help", new DocearShowDataProcessingTermsAction(),	MenuBuilder.AS_CHILD);
				//builder.addAction("/menu_bar/help", new DocearShowNotificationBar(),	MenuBuilder.AS_CHILD);
				
				//add node privacy actions
				if("true".equals(System.getProperty("docear.debug", "false"))) {
					builder.addSeparator("/menu_bar/edit", MenuBuilder.AS_CHILD);
					builder.addAction("/menu_bar/edit", new DocearSetNodePrivacyAction(),	MenuBuilder.AS_CHILD);
					builder.addSeparator("/node_popup", MenuBuilder.AS_CHILD);
					builder.addAction("/node_popup", new DocearSetNodePrivacyAction(),	MenuBuilder.AS_CHILD);
					
				}
			}
		});
		File file = new File(Compat.getApplicationUserDirectory(), "docear_core_ribbon.xml");		
		if (file.exists()) {
			LogUtils.info("using alternative ribbon configuration file: "+file.getAbsolutePath());
			try {				
				modeController.getUserInputListenerFactory().getRibbonBuilder().updateRibbon(file.toURI().toURL());
			}
			catch (MalformedURLException e) {				
				LogUtils.severe("MModeControllerFactory.createStandardControllers(): "+e.getMessage());
			}
		}
		else {
			modeController.getUserInputListenerFactory().getRibbonBuilder().updateRibbon(DocearController.class.getResource("/xml/ribbons.xml"));
		}
		
	}



	private void setDocearMapWriter(ModeController modeController) {
		DocearMapWriter mapWriter = new DocearMapWriter(modeController.getMapController());
		mapWriter.setMapWriteHandler();		
	}

	private void registerController(ModeController modeController) {
		DocearNodeModifiedExtensionController.install(modeController);
		DocearNodePrivacyExtensionController.install(modeController);
	}

	private void replaceFreeplaneStringsAndActions(ModeController modeController) {
		disableAutoUpdater();
		
		//replace this actions if docear_core is present
		modeController.removeAction("SaveAsAction");
		modeController.addAction(new SaveAsAction());
		modeController.removeAction("SaveAction");
		modeController.addAction(new SaveAction());
		
		
		//remove sidepanel switcher
		//Controller.getCurrentModeController().removeAction("ShowFormatPanel");
		ResourceController resourceController = ResourceController.getResourceController();		
		
		if (!resourceController.getProperty(APPLICATION_NAME, "").equals(DOCEAR)) {
			return;
		}

		//replace if application name is docear
		replaceResourceBundleStrings();

		replaceActions();
	}

	private void disableAutoUpdater() {
		final OptionPanelController optionController = Controller.getCurrentController().getOptionPanelController();		
		optionController.addPropertyLoadListener(new OptionPanelController.PropertyLoadListener() {
			
			public void propertiesLoaded(Collection<IPropertyControl> properties) {
				((IPropertyControl) optionController.getPropertyControl("check_updates_automatically")).setEnabled(false);
			}
		});
	}

	private void replaceActions() {
		ResourceController resourceController = DocearController.getPropertiesController();		
		
		WorkspaceController.replaceAction(new DocearOpenUrlAction(REQUEST_FEATURE_ACTION, resourceController.getProperty(FEATURE_TRACKER_LOCATION)));
		WorkspaceController.replaceAction(new DocearOpenUrlAction(ASK_FOR_HELP, resourceController.getProperty(HELP_FORUM_LOCATION)));
		WorkspaceController.replaceAction(new DocearOpenUrlAction(REPORT_BUG_ACTION, resourceController.getProperty(BUG_TRACKER_LOCATION)));
		WorkspaceController.replaceAction(new DocearOpenUrlAction(OPEN_FREEPLANE_SITE_ACTION, resourceController.getProperty(WEB_FREEPLANE_LOCATION)));
		WorkspaceController.replaceAction(new DocearOpenUrlAction(DOCUMENTATION_ACTION, resourceController.getProperty(WEB_DOCU_LOCATION)));
		WorkspaceController.replaceAction(new GettingStartedAction());
		WorkspaceController.replaceAction(new OnlineDocumentationAction("OnlineReference", "docu-online"));
		
		WorkspaceController.replaceAction(new DocearShowTermsOfUseAction());
		WorkspaceController.replaceAction(new DocearShowDataPrivacyStatementAction());
		WorkspaceController.replaceAction(new DocearShowDataProcessingTermsAction());
	}

	private void replaceResourceBundleStrings() {
		ResourceController resourceController = ResourceController.getResourceController();
		ResourceBundles bundles = ((ResourceBundles) resourceController.getResources());
		Controller controller = Controller.getCurrentController();

		for (Enumeration<?> i = bundles.getKeys(); i.hasMoreElements();) {
			String key = i.nextElement().toString();
			String value = bundles.getResourceString(key);
			if (value.matches(".*[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee].*")) {
				value = value.replaceAll("[Ff][Rr][Ee][Ee][Pp][Ll][Aa][Nn][Ee]", DOCEAR);
				bundles.putResourceString(key, value);
				if (key.matches(".*[.text]")) {
					key = key.replace(".text", "");
					AFreeplaneAction action = controller.getAction(key);
					if (action != null) {
						MenuBuilder.setLabelAndMnemonic(action, value);
					}
				}
			}
		}		
	}

	private void addPluginDefaults(Controller controller) {		
		ResourceController resController = controller.getResourceController();
		if (resController.getProperty("ApplicationName").equals("Docear") && DocearController.getController().isDocearFirstStart()) {			
			resController.setProperty("selection_method", "selection_method_by_click");
			//resController.setProperty("links", "relative_to_workspace");
			resController.setProperty("save_folding", "always_save_folding");
			resController.setProperty("leftToolbarVisible", "false");			
			resController.setProperty("styleScrollPaneVisible", "true");
			resController.setProperty(DocearController.DOCEAR_FIRST_RUN_PROPERTY, true);			
		}
		WorkspaceController.addAction(new DocearRenameAction());
	}
	
	private void registerListeners(ModeController modeController) {
		Controller.getCurrentController().getOptionPanelController().addPropertyLoadListener(new PropertyLoadListener());
		Controller.getCurrentController().getResourceController().addPropertyChangeListener(new PropertyListener());
		modeController.getMapController().addMapLifeCycleListener(new MapLifeCycleAndViewListener());
		DocearCoreOmniListenerAdapter adapter = new DocearCoreOmniListenerAdapter();
		modeController.getMapController().addMapLifeCycleListener(adapter);
		modeController.getMapController().addMapChangeListener(adapter);
		modeController.getMapController().addNodeChangeListener(adapter);
		modeController.getMapController().addNodeSelectionListener(adapter);
		DocearController.getController().addDocearEventListener(adapter);
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(adapter);
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(new MapLifeCycleAndViewListener());
		WorkspaceController.getModeExtension(modeController).getIOController().registerNodeActionListener(AWorkspaceTreeNode.class, WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT, new WorkspaceOpenDocumentListener());
	}	
	
	class GettingStartedAction extends AFreeplaneAction {
		
		public GettingStartedAction() {
			super("GettingStartedAction");			
		}

		private static final long serialVersionUID = 1L;

		public void actionPerformed(final ActionEvent e) {
			final ResourceController resourceController = DocearController.getPropertiesController();
			final File baseDir = new File(resourceController.getResourceBaseDir()).getAbsoluteFile().getParentFile();
			final String languageCode = resourceController.getLanguageCode();
			final File file = ConfigurationUtils.getLocalizedFile(new File[]{baseDir}, Controller.getCurrentController().getResourceController().getProperty("tutorial_map"), languageCode);
			try {
				final URL endUrl = file.toURI().toURL();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							if (endUrl.getFile().endsWith(".mm")) {
								 Controller.getCurrentController().selectMode(MModeController.MODENAME);
								 MMapIO mapIO = (MMapIO) MModeController.getMModeController().getExtension(MapIO.class);
								 mapIO.newDocumentationMap(endUrl);
							}
							else {
								Controller.getCurrentController().getViewController().openDocument(endUrl);
							}
						}
						catch (final Exception e1) {
							LogUtils.severe(e1);
						}
					}
				});
			}
			catch (final MalformedURLException e1) {
				LogUtils.warn(e1);
			}
			
			DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.SHOW_HELP);
		}
	}

}
