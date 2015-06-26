package org.docear.plugin.pdfutilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;

import org.apache.commons.io.FilenameUtils;
import org.docear.addons.highlights.IHighlightsImporter;
import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.ui.LinkRadioButtonProperty;
import org.docear.plugin.core.ui.LinkLabelProperty;
import org.docear.plugin.core.util.CompareVersion;
import org.docear.plugin.core.util.DirectoryFileFilter;
import org.docear.plugin.core.util.MapUtils;
import org.docear.plugin.core.util.WinRegistry;
import org.docear.plugin.pdfutilities.actions.AbstractMonitoringAction;
import org.docear.plugin.pdfutilities.actions.AddMonitoringFolderAction;
import org.docear.plugin.pdfutilities.actions.DeleteFileAction;
import org.docear.plugin.pdfutilities.actions.DeleteMonitoringFolderAction;
import org.docear.plugin.pdfutilities.actions.DocearPasteAction;
import org.docear.plugin.pdfutilities.actions.DocearSendPdfxcRegistryAction;
import org.docear.plugin.pdfutilities.actions.EditMonitoringFolderAction;
import org.docear.plugin.pdfutilities.actions.ImportAllAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.ImportAllChildAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.ImportNewAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.ImportNewChildAnnotationsAction;
import org.docear.plugin.pdfutilities.actions.MonitoringFlattenSubfoldersAction;
import org.docear.plugin.pdfutilities.actions.MonitoringGroupRadioButtonAction;
import org.docear.plugin.pdfutilities.actions.RadioButtonAction;
import org.docear.plugin.pdfutilities.actions.RemoveLinebreaksAction;
import org.docear.plugin.pdfutilities.actions.ShowInstalledPdfReadersDialogAction;
import org.docear.plugin.pdfutilities.actions.ShowPdfReaderDefinitionDialogAction;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.addons.DocearAddonController;
import org.docear.plugin.pdfutilities.features.DocearNodeMonitoringExtensionController;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.PDFReaderHandle;
import org.docear.plugin.pdfutilities.features.PDFReaderHandle.RegistryBranch;
import org.docear.plugin.pdfutilities.listener.DocearAutoMonitoringListener;
import org.docear.plugin.pdfutilities.listener.DocearFollowLinkAction;
import org.docear.plugin.pdfutilities.listener.DocearNodeDropListener;
import org.docear.plugin.pdfutilities.listener.DocearNodeMouseMotionListener;
import org.docear.plugin.pdfutilities.listener.DocearNodeSelectionListener;
import org.docear.plugin.pdfutilities.listener.DocearProjectModelListener;
import org.docear.plugin.pdfutilities.listener.MonitorungNodeUpdater;
import org.docear.plugin.pdfutilities.listener.PdfNodeChangeListener;
import org.docear.plugin.pdfutilities.listener.WorkspaceNodeOpenDocumentListener;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.map.MapConverter;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.docear.plugin.pdfutilities.ui.InstalledPdfReadersDialog;
import org.docear.plugin.pdfutilities.ui.JDocearInvisibleMenu;
import org.docear.plugin.pdfutilities.ui.JMonitoringMenu;
import org.docear.plugin.pdfutilities.ui.ViewerSettingsChangeErrorDialog;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.docear.plugin.pdfutilities.workspace.action.IncomingReReadMonitoringAction;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.OptionPanelController;
import org.freeplane.core.resources.OptionPanelController.PropertyLoadListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.resources.components.RadioButtonProperty;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.MultipleImage;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.CurrentState;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.link.LinkController.LinkType;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.features.url.MapVersionInterpreter;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.mindmapmode.MModeWorkspaceUrlManager;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelListener;
import org.freeplane.plugin.workspace.model.project.IProjectModelListener;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class PdfUtilitiesController extends ALanguageController {

	private static final String AUTOUPDATE_MENU = "/Autoupdate"; //$NON-NLS-1$
	public static final String DELETE_ACTION = "/DeleteAction"; //$NON-NLS-1$
	public static final String SUBFOLDERS_MENU = "/subfolders"; //$NON-NLS-1$
	public static final String MON_SUBDIRS = "mon_subdirs"; //$NON-NLS-1$
	public static final String MON_AUTO = "mon_auto"; //$NON-NLS-1$
	public static final String MON_FLATTEN_DIRS = "mon_flatten_dirs"; //$NON-NLS-1$
	public static final String MON_MINDMAP_FOLDER = "mon_mindmap_folder"; //$NON-NLS-1$
	public static final String MON_INCOMING_FOLDER = "mon_incoming_folder"; //$NON-NLS-1$
	public static final String MON_EXTRA_INCOMING = "mon_add_extra_incoming_node";
	public static final String SETTINGS_MENU = "/Settings"; //$NON-NLS-1$	
	public static final String OPEN_ON_PAGE_READER_COMMAND_KEY = "docear_open_on_page_reader_command";
	public static final String OPEN_PDF_VIEWER_ON_PAGE_KEY = "docear_open_on_page"; //$NON-NLS-1$	
	public static final String OPEN_INTERNAL_PDF_VIEWER_KEY = "docear_open_internal"; //$NON-NLS-1$
	public static final String OPEN_STANDARD_PDF_VIEWER_KEY = "docear_open_standard"; //$NON-NLS-1$
	public static final String AUTO_IMPORT_ANNOTATIONS_KEY = "docear_automatic_annotation_import"; //$NON-NLS-1$
	public static final String IMPORT_BOOKMARKS_KEY = "docear_import_bookmarks"; //$NON-NLS-1$
	public static final String IMPORT_COMMENTS_KEY = "docear_import_comments"; //$NON-NLS-1$
	public static final String REMOVE_LINEBREAKS_BOOKMARKS_KEY = "docear_remove_linebreaks_bookmarks"; //$NON-NLS-1$
	public static final String REMOVE_LINEBREAKS_COMMENTS_KEY = "docear_remove_linebreaks_comments"; //$NON-NLS-1$
	public static final String REMOVE_LINEBREAKS_HIGHLIGHTED_KEY = "docear_remove_linebreaks_highlighted_text"; //$NON-NLS-1$
	public static final String KEEP_DOUBLE_LINEBREAKS_KEY = "docear_keep_double_linebreaks"; //$NON-NLS-1$
	public static final String REMOVE_LINEBREAKS_KEY = "docear_remove_linebreaks"; //$NON-NLS-1$
	public static final String ADD_SPACES_KEY = "docear_add_spaces"; //$NON-NLS-1$
	public static final String REMOVE_DASHES_KEY = "docear_remove_dashes"; //$NON-NLS-1$
	public static final String IMPORT_HIGHLIGHTED_TEXTS_KEY = "docear_import_highlighted_text"; //$NON-NLS-1$
	public static final String IMPORT_ONLY_POP_UP_KEY = "docear_import_only_pop_up"; //$NON-NLS-1$
	public static final String IMPORT_ONLY_HIGHLIGHTED_KEY = "docear_import_only_highlighted"; //$NON-NLS-1$
	public static final String IMPORT_POP_UP_HIGHLIGHTED_KEY = "docear_import_pop_up_highlighted"; //$NON-NLS-1$
	public static final String OPEN_ON_PAGE_WARNING_KEY = "OptionPanel.docear_open_on_page_reader_path_warning"; //$NON-NLS-1$
	public static final String OPEN_ON_PAGE_ERROR_KEY = "OptionPanel.docear_open_on_page_reader_path_error"; //$NON-NLS-1$<https://sourceforge.net/apps/trac/docear/ticket/659

	public static final String MENU_BAR = "/menu_bar"; //$NON-NLS-1$
	public static final String NODE_POPUP_MENU = "/node_popup"; //$NON-NLS-1$
	public static final String PDF_CATEGORY = "pdf_management";
	public static final String REFERENCE_CATEGORY = "reference_management";
	public static final String MONITORING_CATEGORY = "monitoring";
	public static final String TOOLS_MENU = "/extras"; //$NON-NLS-1$
	public static final String PDF_MANAGEMENT_MENU = "/pdf_management"; //$NON-NLS-1$
	public static final String MONITORING_MENU = "/monitoring"; //$NON-NLS-1$
	public static final String PDF_MANAGEMENT_MENU_LANG_KEY = "menu_pdf_utilities"; //$NON-NLS-1$
	public static final String MONITORING_MENU_LANG_KEY = "menu_monitoring_utilities"; //$NON-NLS-1$

	private ImportAllAnnotationsAction importAllAnnotationsAction;
	private ImportNewAnnotationsAction importNewAnnotationsAction;
	private DeleteFileAction deleteFileAction;
	private AbstractMonitoringAction addMonitoringFolderAction;
	private EditMonitoringFolderAction editMonitoringFolderAction;
	private UpdateMonitoringFolderAction updateMonitoringFolderAction;
	private DeleteMonitoringFolderAction deleteMonitoringFolderAction;
	private ImportAllChildAnnotationsAction importAllChildAnnotationsAction;
	private ImportNewChildAnnotationsAction importNewChildAnnotationsAction;
	private RemoveLinebreaksAction removeLinebreaksAction;
	private MonitoringFlattenSubfoldersAction monitoringFlattenSubfoldersAction;
	private MonitoringGroupRadioButtonAction flattenOnAction;
	private MonitoringGroupRadioButtonAction flattenOffAction;
	private MonitoringGroupRadioButtonAction flattenDefaultAction;
	private MonitoringGroupRadioButtonAction autoOnAction;
	private MonitoringGroupRadioButtonAction autoOffAction;
	private MonitoringGroupRadioButtonAction autoDefaultAction;
	private MonitoringGroupRadioButtonAction subdirsOnAction;
	private MonitoringGroupRadioButtonAction subdirsOffAction;
	private MonitoringGroupRadioButtonAction subdirsDefaultAction;
	
	private List<PDFReaderHandle> pdfViewerList = null;
	private PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
	private FileFilter appFilter = new FileFilter() {
		public boolean accept(File file) {
			if(file.getName().endsWith(".app")) {
				return true;
			}
			return false;
		}
	};
	private UIIcon refreshMonitoringIcon ;
	private IProjectModelListener projectModelListener;
	private PropertyChangeListener iconChangeListener;
	
	private static PdfUtilitiesController controller;
	public static final Icon REFRESH_MONITORING_ICON = new ImageIcon(PdfUtilitiesController.class.getResource("/icons/view-refresh-3.png"));

	public PdfUtilitiesController(ModeController modeController) {
		super();
		controller = this;

		LogUtils.info("starting DocearPdfUtilitiesStarter(ModeController)..."); //$NON-NLS-1$
		//this.modecontroller = modeController;
		this.addPropertiesToOptionPanel(modeController);
		this.addPluginDefaults(modeController);
		this.registerController(modeController);
		this.registerActions(modeController);
		this.registerListener(modeController);
		this.addMenuEntries(modeController);
		
		MapVersionInterpreter.addMapVersionInterpreter(new MapVersionInterpreter("SciploreMM", 1, "0.9.0\" software_name=\"SciPlore_", false, false, "SciploreMM", "http://sciplore.org", null, new MapConverter()));
		
	}

	public static PdfUtilitiesController getController() {
		return controller;
	}

	public void showViewerSelectionIfNecessary() {
		ExecutorService execSrv = Executors.newSingleThreadExecutor();
		Future<?> f = execSrv.submit(new Runnable() {
			public void run() {
		showViewerSelectionIfNecessary(false);
	}
		});
		try {
			f.get(2, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtils.info("ABORTED - finding pdf readers");
		}
		execSrv.shutdown();
	}

	public void showViewerSelectionIfNecessary(boolean force) {
		List<PDFReaderHandle> readers = getPdfViewers();
		Boolean showReaderDialog = showReaderDialogNewPdfReader(readers);
		if (force || showReaderDialog != null) {
			InstalledPdfReadersDialog dialog = new InstalledPdfReadersDialog(readers.toArray(new PDFReaderHandle[] {}), showReaderDialog);

			if (JOptionPane.showConfirmDialog(UITools.getFrame(), dialog, TextUtils.getText("docear.validate_pdf_xchange.headline"), JOptionPane.OK_CANCEL_OPTION)  == JOptionPane.OK_OPTION) {
				PDFReaderHandle reader = (PDFReaderHandle) dialog.getReaderChoiceBox().getSelectedItem();
				setReaderPreferences(reader.getExecFile());
				
				if(reader.getName().equals(TextUtils.getText("docear.default_reader"))){
					PdfUtilitiesController.getController().setToStandardPdfViewer();
					for(PDFReaderHandle handle : readers){
						if(handle.isDefault()){					
							reader = handle;
							break;
						}
					}			
				}
				if (readerFilter.isPdfXChange(new File(reader.getExecFile()))) {
					try {
						if(reader.getRegistryBranch().equals(RegistryBranch.WOW6432NODE)) {
							importRegistrySettings(getClass().getResource("/conf/PdfXChangeViewerSettingsWow6432Node.reg"));
						}
						else {
							importRegistrySettings(getClass().getResource("/conf/PdfXChangeViewerSettings.reg"));
						}
					}
					catch (IOException e) {
						LogUtils.severe(e.getMessage());
						showViewerSelectionIfNecessary();
					}					
				}				
				else if(readerFilter.isAcrobat(new File(reader.getExecFile()))){
					try {
						importRegistrySettings(getClass().getResource("/conf/AdobeAcrobatSettings.reg"));
					}
					catch (IOException e) {
						LogUtils.severe(e.getMessage());
						showViewerSelectionIfNecessary();
					}
				}
				
				try {
					if (!hasCompatibleSettings(reader.getExecFile())) {
						if(ViewerSettingsChangeErrorDialog.showWarningEnabled()) {
							JOptionPane.showMessageDialog(UITools.getFrame(), new ViewerSettingsChangeErrorDialog(reader.getExecFile()), TextUtils.getText("docear.validate_pdf_xchange.settings_change_error.title"), JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				catch (IOException e) {
					if(ViewerSettingsChangeErrorDialog.showWarningEnabled()) {
						JOptionPane.showMessageDialog(UITools.getFrame(), new ViewerSettingsChangeErrorDialog(reader.getExecFile()), TextUtils.getText("docear.validate_pdf_xchange.headline"), JOptionPane.WARNING_MESSAGE);
					}
				}		
			}
			else {
				PdfUtilitiesController.getController().setToStandardPdfViewer();				
			}

		}
	}

	private void parseExportedRegistryFile(Map<String, PDFReaderHandle> viewers, File file, RegistryBranch branch) throws NumberFormatException, IOException {
		String line;
		PDFReaderHandle handle = new PDFReaderHandle(branch);

		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("[")) {
					handle = new PDFReaderHandle(branch);
					continue;
				}
				else if (line.startsWith("\"DisplayName\"")) {
					String s = line.substring(15);
					if (s.startsWith("PDF-Viewer") || s.startsWith("PDF-XChange Viewer")) {
						handle.setName("PDF-Viewer");
					}
					else if (s.startsWith("PDF-XChange Editor")) {
						handle.setName("PDF-XChange Editor");
					}
					else if (s.startsWith("Foxit Reader")) {
						handle.setName("Foxit Reader");
					}
					else if (s.startsWith("Adobe Acrobat")) {
						handle.setName("Acrobat");
					}
					else if (s.startsWith("Adobe Reader")) {
						handle.setName("Adobe Reader");
					}
				}
				else if (line.startsWith("\"DisplayVersion")) {
					String version = line.substring(18, line.length() - 1);
					handle.setVersion(version);					
				}
				else if (line.startsWith("\"InstallLocation")) {
					String installPath = line.substring(19, line.length() - 1);
					handle.setExecFile(installPath);
				}
				else if (!viewers.containsKey("PDF-Viewer") && line.contains("PDFXCview.exe")){
					String installPath = line.substring(5, line.length() - 2);
					handle.setName("PDF-Viewer");
					handle.setExecFile(installPath);
					handle.setVersion("0");
				}
				if(handle.isComplete()) {
					if (handle.compare(viewers.get(handle.getName())) == CompareVersion.GREATER) {
						if(!Compat.isWindowsOS() || new File(handle.getExecFile()).exists()) {
							viewers.put(handle.getName(),handle);
						}
					}
				}
			}
		}
		finally {
			reader.close();
		}
	}

	public List<PDFReaderHandle> getPdfViewers() {
		if(this.pdfViewerList != null){
			return this.pdfViewerList;
		}
		List<PDFReaderHandle> handles = new ArrayList<PDFReaderHandle>();
		try {
			// Map<Name, List<String>{Version, InstallDir>
			Map<String, PDFReaderHandle> viewers = new HashMap<String, PDFReaderHandle>();		
	
			if(Compat.isMacOsX()) {
				lookForReadersMacOs(viewers, new File("/Applications"));
			}
			else {
				File winFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "win_uninstall.reg");
				try {
					exportRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall", winFile);
					parseExportedRegistryFile(viewers, winFile, RegistryBranch.DEFAULT);
				}
				catch (IOException e1) {
					LogUtils.info("Read registry (default): "+ e1.toString());
				}
		
				File winWOW6432NODEFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "win_wow64_uninstall.reg");
				try {
					exportRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall", winWOW6432NODEFile);
					parseExportedRegistryFile(viewers, winWOW6432NODEFile, RegistryBranch.WOW6432NODE);
				}
				catch (IOException e1) {
					LogUtils.info("Read registry (wow6432): "+ e1.toString());
				}
				if(!viewers.containsKey("PDF-Viewer") && Compat.isWindowsOS()){
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					try {					
						WinRegistry.exportKey(WinRegistry.HKEY_CURRENT_USER, "Software\\Tracker Software\\PDFViewer", outputStream);
						String trackerSettings = outputStream.toString();
						BufferedReader reader = new BufferedReader(new StringReader(trackerSettings));
						String line;
						try{
							while ((line = reader.readLine()) != null) {
								if (line.startsWith("\"InstallPath")) {
									PDFReaderHandle handle = new PDFReaderHandle(RegistryBranch.DEFAULT);
									handle.setName("PDF-Viewer");
									String installPath = line.substring(15, line.length() - 1);
									handle.setExecFile(installPath);
									handle.setVersion("0");
									if(!Compat.isWindowsOS() || new File(handle.getExecFile()).exists()) {
										viewers.put(handle.getName(),handle);
									}
									break;
								}
							}
						}
						finally{
							reader.close();
						}
						if(!viewers.containsKey("PDF-Viewer")){
							File currentUserClassesFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "current_user_classes.reg");
							try {
								exportRegistryKey("HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\CLSID", currentUserClassesFile);
								parseExportedRegistryFile(viewers, currentUserClassesFile, RegistryBranch.DEFAULT);
							}
							catch (IOException e1) {
								LogUtils.info("Read registry (default): "+ e1.toString());
								LogUtils.warn(e1);
							}
						}
					}
					catch (IOException e) {
						LogUtils.info("Could not read PDF X-Change Viewer settings.");
					}
					finally{
						try {
							outputStream.close();
						}
						catch (IOException e) {
							LogUtils.info("Could not close Tracker Software settings stream.");
						}
					}
				}			
			}
			PDFReaderHandle handle;
			if ((handle = viewers.get("PDF-Viewer")) != null) {
				handle.setName("PDF-XChange Viewer");
				if(!handle.getExecFile().endsWith("PDFXCview.exe")) {
					if(!(handle.getExecFile().endsWith("PDF Viewer") || handle.getExecFile().endsWith("PDF Viewer\\"))) {
						handle.setExecFile(handle.getExecFile()+"\\PDF Viewer\\");
					}
					handle.setExecFile(handle.getExecFile()+"PDFXCview.exe");
				}
				handle.setExecFile(convertPath(handle.getExecFile()));
				handles.add(handle);
			}
			if ((handle = viewers.get("Foxit Reader")) != null) {
				handle.setName("Foxit Reader");
				handle.setExecFile(convertPath(handle.getExecFile() + "Foxit Reader.exe"));
				handles.add(handle);
			}
			if ((handle = viewers.get("Adobe Reader")) != null) {
				handle.setName("Adobe Reader");
				handle.setExecFile(convertPath(handle.getExecFile() + "AcroRd32.exe"));
				handles.add(handle);
			}
			if ((handle = viewers.get("Acrobat")) != null) {
				handle.setName("Acrobat");
				handle.setExecFile(convertPath(handle.getExecFile() + "Acrobat\\Acrobat.exe"));
				handles.add(handle);
			}
			if ((handle = viewers.get("Adobe Reader.app")) != null) {
				handles.add(handle);
			}
			if ((handle = viewers.get("Adobe Acrobat Pro.app")) != null) {
				handles.add(handle);
			}
			if ((handle = viewers.get("Skim.app")) != null) {
				handles.add(handle);
			}
			if ((handle = viewers.get("Preview.app")) != null) {
				handles.add(handle);
			}		
		
			checkForDefaultReader(handles);
			this.pdfViewerList = handles;
		}
		catch (RuntimeException ignore) {
			LogUtils.info("Unable to retrieve pdf viewer information: "+ ignore.getMessage());
		}
		return handles;
	}

	@SuppressWarnings("unused")
	private void getPdfViewersByExt() {
		try {
			//HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\.pdf\\(default)
			Set<Entry<String, Object>> entries = WinRegistry.getKeyValues(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\.pdf");
			for (Entry<String, Object> entry : entries) {
				if(entry.getKey().trim().length() == 0) {
					//HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\{ProgId}\\shell\\open\\command\\(default)
					String exec = stripPath(WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\"+entry.getValue()+"\\shell\\open\\command\\", ""));
					//HKEY_CURRENT_USER\\Software\\Classes\\Local Settings\\Software\\Microsoft\\Windows\\Shell\\MuiCache\\{progPath}
					String friendlyName = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\Classes\\Local Settings\\Software\\Microsoft\\Windows\\Shell\\MuiCache", exec);
					LogUtils.info(friendlyName+": "+exec);
				}
			}
			//HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\.pdf\\OpenWithList\\{appName}\\
			for(String appName : WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\.pdf\\OpenWithList")) {
				//HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Applications\\{appName}\\FriendlyAppName
				String friendlyName = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\Applications\\"+appName, "FriendlyAppName");
				//HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Applications\\{appName}\\shell\\open\\command\\
				String exec = stripPath(WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\Applications\\"+appName+"\\shell\\open\\command\\", ""));
				LogUtils.info(friendlyName+": "+exec);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String stripPath(String openPath) {
		if(openPath != null) {
			int i = openPath.indexOf("\"");
			if(i > -1) {
				openPath = openPath.substring(i+1);
				i = openPath.indexOf("\"");
				if(i > -1) {
					openPath = openPath.substring(0, i); 
					
				}
			}
			return openPath;
		}
		return null;
	}

	private void checkForDefaultReader(List<PDFReaderHandle> handles) {
		if (!Compat.isWindowsOS()) {
			return;
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BufferedReader reader = new BufferedReader(new StringReader(""));
		try {					
			WinRegistry.exportKey(WinRegistry.HKEY_CLASSES_ROOT, ".pdf", outputStream);
			String pdfDefaultSettings = outputStream.toString();
			reader = new BufferedReader(new StringReader(pdfDefaultSettings));
			String defaultReaderKey = null;
			String line;			
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("\"\"")) {
					defaultReaderKey = line.substring(4, line.length() - 1);
					break;
				}
			}
			if(defaultReaderKey != null && defaultReaderKey.length() > 0){
				outputStream.reset();
				WinRegistry.exportKey(WinRegistry.HKEY_CLASSES_ROOT, defaultReaderKey+"\\shell\\open\\command", outputStream);
				String defaultReaderOpenCommand = outputStream.toString();
				reader = new BufferedReader(new StringReader(defaultReaderOpenCommand));
				line = null;			
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("\"\"")) {
						String openCommand = line.substring(4, line.length() - 1);
						for(PDFReaderHandle handle : handles){
							if(openCommand.contains(handle.getExecFile())){
								handle.setDefault(true);
								break;
							}
						}
						break;
					}
				}
			}
			
		}catch (IOException e) {
			LogUtils.info("Could not read default pdf reader settings.");
		}
		finally{
			try {
				outputStream.close();
				reader.close();
			}
			catch (IOException e) {
				LogUtils.info("Could not close default pdf reader settings stream and reader.");
			}
		}
	}

	private void lookForReadersMacOs(Map<String, PDFReaderHandle> viewers, File appDirectory) {	
		for (File app : appDirectory.listFiles(appFilter)) {
			if(app.getName().startsWith("Adobe Reader")) {
				viewers.put("Adobe Reader.app", new PDFReaderHandle("Adobe Reader", app.getAbsolutePath(), null));
			}
			else if(app.getName().startsWith("Adobe Acrobat")) {
				viewers.put("Adobe Acrobat Pro.app", new PDFReaderHandle("Adobe Acrobat Pro", app.getAbsolutePath(), null));
			}
//			else if(app.getName().startsWith("Skim")) {
//				viewers.put("Skim.app", new PDFReaderHandle("Skim", app.getAbsolutePath(), null));
//			}
			else if(app.getName().startsWith("Preview")) {
				viewers.put("Preview.app", new PDFReaderHandle("Preview", app.getAbsolutePath(), null));
			}
		}
		for(File subDir : appDirectory.listFiles(new DirectoryFileFilter())){
			if(!subDir.getName().endsWith(".app")){
				lookForReadersMacOs(viewers, subDir);
			}
		}
	}

	private Boolean showReaderDialogNewPdfReader(List<PDFReaderHandle> readers) {
		if (readers == null || readers.size() == 0) {
			return null;
		}
		String s = "";
		for (PDFReaderHandle reader : readers) {
			s += reader.getExecFile() + "|";
		}
		String savedReaders = DocearController.getPropertiesController().getProperty("installed_pdf_readers");
		DocearController.getPropertiesController().setProperty("installed_pdf_readers", s);
		if (!s.equals(savedReaders)) {
			return true;
		}

		String viewerCommand = Controller.getCurrentController().getResourceController().getProperty(OPEN_ON_PAGE_READER_COMMAND_KEY);
		if (viewerCommand != null) {
			try {
				if (!hasCompatibleSettings(viewerCommand) && ViewerSettingsChangeErrorDialog.showWarningEnabled()) {
					return false;
				}
			}
			catch (IOException e) {
				return false;
			}
		}

		return null;
	}

	private String convertPath(String path) {
		if (!Compat.isMacOsX() && !Compat.isWindowsOS()) {
			path = (System.getProperty("user.home") + "\\.wine\\drive_c\\" + path.substring(2));
		}

		return FilenameUtils.separatorsToSystem(FilenameUtils.normalize(path));
	}

	private void setReaderPreferences(String executablePath) {
		OptionPanelController opc = Controller.getCurrentController().getOptionPanelController();

		File reader = new File(executablePath);		

		String readerCommand = "";
		
		if (!executablePath.isEmpty() && reader != null && reader.exists()) {
			try {
				((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(false);
				((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
				((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(true);				
				readerCommand = buildCommandString(reader);
			}
			catch (NullPointerException e) {
				Controller.getCurrentController().getResourceController().setProperty(OPEN_STANDARD_PDF_VIEWER_KEY, false);
				Controller.getCurrentController().getResourceController().setProperty(OPEN_INTERNAL_PDF_VIEWER_KEY, false);
				Controller.getCurrentController().getResourceController().setProperty(OPEN_PDF_VIEWER_ON_PAGE_KEY, true);
				readerCommand = buildCommandString(reader);
			}
			DocearController.getPropertiesController().setProperty(OPEN_ON_PAGE_READER_COMMAND_KEY, readerCommand);
		}
		else {
			try {
				((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(true);
				((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
				((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(false);
			}
			catch (NullPointerException e) {
				Controller.getCurrentController().getResourceController().setProperty(OPEN_STANDARD_PDF_VIEWER_KEY, true);
				Controller.getCurrentController().getResourceController().setProperty(OPEN_INTERNAL_PDF_VIEWER_KEY, false);
				Controller.getCurrentController().getResourceController().setProperty(OPEN_PDF_VIEWER_ON_PAGE_KEY, false);
			}			
		}		
	}

	private void registerController(ModeController modeController) {
		AnnotationController.install(new AnnotationController(modeController));
		IconController.getController(modeController).addStateIconProvider(new IStateIconProvider() {			
			public UIIcon getStateIcon(NodeModel node) {
				adjustDefaultLinkIcon(node);
				if(MonitoringUtils.isMonitoringNode(node)) {
					if (refreshMonitoringIcon  == null) {
						refreshMonitoringIcon = new UIIcon(TextUtils.getText("docear.monitoring.reload.name"), "/icons/refresh icon.png", TextUtils.getText("docear.monitoring.reload.desc")) {	
							public Icon getIcon() {
								return REFRESH_MONITORING_ICON;
							}

							public KeyStroke getKeyStroke() {
								return null;
							}

							public String getImagePath() {
								return "/icons";
							}

							public URL getUrl() {
								return PdfUtilitiesController.class.getResource(getFileName());
							}

							public String getPath() {
								return getUrl().toExternalForm();
							}
						};
					}
					return refreshMonitoringIcon;
				}
				return null;
			}
		});
		
		DocearNodeMonitoringExtensionController.install(new DocearNodeMonitoringExtensionController(modeController));
		
	}

	private void adjustDefaultLinkIcon(NodeModel node) {
		for (INodeView view : node.getViewers()) {
			if(view instanceof NodeView) {
				Icon newIcon = MonitoringUtils.getAnnotationModelIcon(node);				
				if(newIcon != null) {
					((NodeView)view).getMainView().addPropertyChangeListener("icon", getIconChangeListener());
				}
				
			}
		}
		
	}

	private PropertyChangeListener getIconChangeListener() {
		if(iconChangeListener == null) {
			iconChangeListener = new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					if(evt.getSource() instanceof MainView) {
						final MainView view = (MainView)evt.getSource();
						MultipleImage icon = (MultipleImage) view.getIcon();
    					if(icon != null) {
    						NodeModel node = view.getNodeView().getModel();
    						Icon newIcon = MonitoringUtils.getAnnotationModelIcon(node);
    						if(newIcon !=  null) {
    							icon.addOrReplaceIcon(LinkType.DEFAULT.icon, newIcon);
    						}
						}
    					SwingUtilities.invokeLater(new Runnable() {    						
    						public void run() {
    							view.removePropertyChangeListener("icon", iconChangeListener);
    							view.repaint();
    						}
    					});
					}
					
					
				}
			};
		}
		return iconChangeListener;
	}

	private void registerActions(ModeController modeController) {
		ResourceController res = ResourceController.getResourceController();
		res.setDefaultProperty(ImportAllAnnotationsAction.KEY+".icon", "/images/docear/monitoring/PDF-ImportAllAnnotations.png");
		res.setDefaultProperty(ImportNewAnnotationsAction.KEY+".icon", "/images/docear/monitoring/PDF-ImportNewAnnotations.png");
		
		res.setDefaultProperty(AddMonitoringFolderAction.KEY+".icon", "/images/docear/monitoring/Monitoring-add.png");
		res.setDefaultProperty(UpdateMonitoringFolderAction.KEY+".icon", "/images/docear/monitoring/Monitoring-Re-read.png");
		res.setDefaultProperty(DeleteMonitoringFolderAction.KEY+".icon", "/images/docear/monitoring/Monitoring-remove.png");
		//res.setDefaultProperty(EditMonitoringFolderAction.KEY+".icon", "/images/docear/monitoring/Project -- Main -- Import.png");

		res.setDefaultProperty(MonitoringFlattenSubfoldersAction.KEY+".icon", "/images/docear/monitoring/MonitoringSettings-FlattenDirectory.png");
		res.setDefaultProperty("flatten_dirs.icon", "/images/docear/monitoring/MonitoringSettings-FlattenDirectory.png");
		res.setDefaultProperty("auto_monitoring.icon", "/images/docear/monitoring/MonitoringSettings-AutoupdateOnOpeningMaps.png");
		res.setDefaultProperty("subfolders.icon", "/images/docear/monitoring/MonitoringSettings-ReadSubfolders.png");
		
		res.setDefaultProperty("DocearBackupOpenLocation.icon", "/images/docear/help/help-openBackupFolder.png");
		
		
		this.importAllAnnotationsAction = new ImportAllAnnotationsAction();
		modeController.addAction(importAllAnnotationsAction);
		this.importNewAnnotationsAction = new ImportNewAnnotationsAction();
		modeController.addAction(importNewAnnotationsAction);
		this.deleteFileAction = new DeleteFileAction();
		modeController.addAction(deleteFileAction);
		
		this.addMonitoringFolderAction = new AddMonitoringFolderAction();
		modeController.addAction(addMonitoringFolderAction);
		this.updateMonitoringFolderAction = new UpdateMonitoringFolderAction();
		modeController.addAction(updateMonitoringFolderAction);
		this.importAllChildAnnotationsAction = new ImportAllChildAnnotationsAction();
		modeController.addAction(importAllChildAnnotationsAction);
		this.importNewChildAnnotationsAction = new ImportNewChildAnnotationsAction();
		modeController.addAction(importNewChildAnnotationsAction);
		this.removeLinebreaksAction = new RemoveLinebreaksAction();
		modeController.addAction(removeLinebreaksAction);
		this.deleteMonitoringFolderAction = new DeleteMonitoringFolderAction();
		modeController.addAction(deleteMonitoringFolderAction);
		this.editMonitoringFolderAction = new EditMonitoringFolderAction();
		modeController.addAction(editMonitoringFolderAction);
		this.monitoringFlattenSubfoldersAction = new MonitoringFlattenSubfoldersAction();
		modeController.addAction(monitoringFlattenSubfoldersAction);
		
		flattenOnAction = new MonitoringGroupRadioButtonAction("mon_flatten_on", MON_FLATTEN_DIRS, 1, modeController);
		flattenOffAction = new MonitoringGroupRadioButtonAction("mon_flatten_off", MON_FLATTEN_DIRS, 0, modeController);
		flattenDefaultAction = new MonitoringGroupRadioButtonAction("mon_flatten_default", MON_FLATTEN_DIRS, 2, modeController);
		WorkspaceController.addAction(flattenOnAction);
		WorkspaceController.addAction(flattenOffAction);
		WorkspaceController.addAction(flattenDefaultAction);
		
		autoOnAction = new MonitoringGroupRadioButtonAction("mon_auto_on", MON_AUTO, 1, modeController);
		autoOffAction = new MonitoringGroupRadioButtonAction("mon_auto_off", MON_AUTO, 0, modeController);
		autoDefaultAction = new MonitoringGroupRadioButtonAction("mon_auto_default", MON_AUTO, 2, modeController);
		WorkspaceController.addAction(autoOnAction);
		WorkspaceController.addAction(autoOffAction);
		WorkspaceController.addAction(autoDefaultAction);
		
		subdirsOnAction = new MonitoringGroupRadioButtonAction("mon_subdirs_on", MON_SUBDIRS, 1, modeController);
		subdirsOffAction = new MonitoringGroupRadioButtonAction("mon_subdirs_off", MON_SUBDIRS, 0, modeController);
		subdirsDefaultAction = new MonitoringGroupRadioButtonAction("mon_subdirs_default", MON_SUBDIRS, 2, modeController);
		WorkspaceController.addAction(subdirsOnAction);
		WorkspaceController.addAction(subdirsOffAction);
		WorkspaceController.addAction(subdirsDefaultAction);
		

		WorkspaceController.addAction(new ShowInstalledPdfReadersDialogAction());
		WorkspaceController.addAction(new ShowPdfReaderDefinitionDialogAction());
		WorkspaceController.addAction(new IncomingReReadMonitoringAction());
		WorkspaceController.addAction(new DocearSendPdfxcRegistryAction());
				
		WorkspaceController.replaceAction(Controller.getCurrentModeController(), new DocearFollowLinkAction());

		modeController.removeAction("PasteAction"); //$NON-NLS-1$
		modeController.addAction(new DocearPasteAction());

		WorkspaceController.getCurrentModeExtension().getIOController().registerNodeActionListener(DefaultFileNode.class, WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT,
				new WorkspaceNodeOpenDocumentListener());
		WorkspaceController.getCurrentModeExtension().getIOController().registerNodeActionListener(LinkTypeFileNode.class, WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT,
				new WorkspaceNodeOpenDocumentListener());
	}	
	
	private void addMenuEntries(ModeController modeController) {
		modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				if(!Compat.isMacOsX()){
					builder.addAction("/menu_bar/help/Web resources", Controller.getCurrentController().getAction(DocearSendPdfxcRegistryAction.KEY), MenuBuilder.AS_CHILD);
				}
				
				String monitoringCategory = PdfUtilitiesController.getParentCategory(builder, MONITORING_CATEGORY);

				builder.addMenuItem(MENU_BAR + TOOLS_MENU, new JMenu(TextUtils.getText(PDF_MANAGEMENT_MENU_LANG_KEY)), MENU_BAR + PDF_MANAGEMENT_MENU,
						MenuBuilder.BEFORE);

				builder.addRadioItem(MENU_BAR + PDF_MANAGEMENT_MENU, new RadioButtonAction("menu_auto_import_annotations", AUTO_IMPORT_ANNOTATIONS_KEY),
						DocearController.getPropertiesController().getBooleanProperty(AUTO_IMPORT_ANNOTATIONS_KEY));
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importAllAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importNewAnnotationsAction, MenuBuilder.AS_CHILD);				
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importAllChildAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, importNewChildAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, removeLinebreaksAction, MenuBuilder.AS_CHILD);
				builder.addSeparator(MENU_BAR + PDF_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + PDF_MANAGEMENT_MENU, deleteFileAction, MenuBuilder.AS_CHILD);

				builder.addMenuItem(MENU_BAR + PDF_MANAGEMENT_MENU, new JMenu(TextUtils.getText(MONITORING_MENU_LANG_KEY)), MENU_BAR + MONITORING_MENU,
						MenuBuilder.AFTER);
				builder.addAction(MENU_BAR + MONITORING_MENU, updateMonitoringFolderAction, MenuBuilder.AS_CHILD);				
				builder.addSeparator(MENU_BAR + MONITORING_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + MONITORING_MENU, addMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + MONITORING_MENU, deleteMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + MONITORING_MENU, editMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addSeparator(MENU_BAR + MONITORING_MENU, MenuBuilder.AS_CHILD);

				builder.addMenuItem(monitoringCategory, new JMenu(TextUtils.getText(MONITORING_MENU_LANG_KEY)), monitoringCategory + MONITORING_MENU,
						MenuBuilder.AS_CHILD);
				builder.addSeparator(monitoringCategory + MONITORING_MENU, MenuBuilder.AFTER);
				builder.addAction(monitoringCategory + MONITORING_MENU, updateMonitoringFolderAction, MenuBuilder.AS_CHILD);				
				builder.addSeparator(monitoringCategory + MONITORING_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(monitoringCategory + MONITORING_MENU, addMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(monitoringCategory + MONITORING_MENU, deleteMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addAction(monitoringCategory + MONITORING_MENU, editMonitoringFolderAction, MenuBuilder.AS_CHILD);
				builder.addSeparator(monitoringCategory + MONITORING_MENU, MenuBuilder.AS_CHILD);

				JMonitoringMenu settingsMenu1 = new JMonitoringMenu("Settings", modeController); //$NON-NLS-1$
				JMonitoringMenu settingsMenu2 = new JMonitoringMenu("Settings", modeController); //$NON-NLS-1$
				// modecontroller.getMapController().addNodeSelectionListener(settingsMenu1);
				// modecontroller.getMapController().addNodeSelectionListener(settingsMenu2);

				builder.addMenuItem(monitoringCategory + MONITORING_MENU, settingsMenu1, monitoringCategory + MONITORING_MENU + SETTINGS_MENU,
						MenuBuilder.AS_CHILD);
				builder.addMenuItem(MENU_BAR + MONITORING_MENU, settingsMenu2, MENU_BAR + MONITORING_MENU + SETTINGS_MENU, MenuBuilder.AS_CHILD);

				builder.addMenuItem(
						MENU_BAR + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("flatten_dirs")), MENU_BAR + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", //$NON-NLS-1$
						MenuBuilder.AS_CHILD);
				builder.addMenuItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("flatten_dirs")), monitoringCategory + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", //$NON-NLS-1$
						MenuBuilder.AS_CHILD);
				
				builder.addMenuItem(
						MENU_BAR + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("PdfUtilitiesController_12")), MENU_BAR + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, //$NON-NLS-1$
						MenuBuilder.AS_CHILD);
				builder.addMenuItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("PdfUtilitiesController_12")), monitoringCategory + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, //$NON-NLS-1$
						MenuBuilder.AS_CHILD);

				builder.addMenuItem(
						MENU_BAR + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("PdfUtilitiesController_14")), MENU_BAR + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, //$NON-NLS-1$
						MenuBuilder.AS_CHILD);
				builder.addMenuItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU,
						new JMenu(TextUtils.getText("PdfUtilitiesController_14")), monitoringCategory + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, //$NON-NLS-1$
						MenuBuilder.AS_CHILD);
				
				
//				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU, monitoringFlattenSubfoldersAction, false);
//				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU, monitoringFlattenSubfoldersAction, false);
//				monitoringFlattenSubfoldersAction.initView(builder);
				
				flattenOnAction.addGroupItem(flattenDefaultAction);
				flattenOnAction.addGroupItem(flattenOffAction);
				flattenOffAction.addGroupItem(flattenDefaultAction);
				flattenOffAction.addGroupItem(flattenOnAction);
				flattenDefaultAction.addGroupItem(flattenOffAction);
				flattenDefaultAction.addGroupItem(flattenOnAction);

				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenOnAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenOnAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenOffAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenOffAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenDefaultAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + "FlattenDirs", flattenDefaultAction, false);

				flattenOnAction.initView(builder);
				flattenOffAction.initView(builder);
				flattenDefaultAction.initView(builder);
				
				
				autoOnAction.addGroupItem(autoDefaultAction);
				autoOnAction.addGroupItem(autoOffAction);
				autoOffAction.addGroupItem(autoDefaultAction);
				autoOffAction.addGroupItem(autoOnAction);
				autoDefaultAction.addGroupItem(autoOffAction);
				autoDefaultAction.addGroupItem(autoOnAction);

				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoOnAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoOnAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoOffAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoOffAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoDefaultAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + AUTOUPDATE_MENU, autoDefaultAction, false);

				autoOnAction.initView(builder);
				autoOffAction.initView(builder);
				autoDefaultAction.initView(builder);

				subdirsOnAction.addGroupItem(subdirsDefaultAction);
				subdirsOnAction.addGroupItem(subdirsOffAction);
				subdirsOffAction.addGroupItem(subdirsDefaultAction);
				subdirsOffAction.addGroupItem(subdirsOnAction);
				subdirsDefaultAction.addGroupItem(subdirsOffAction);
				subdirsDefaultAction.addGroupItem(subdirsOnAction);

				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsOnAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsOffAction, false);
				builder.addRadioItem(monitoringCategory + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsDefaultAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsOnAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsOffAction, false);
				builder.addRadioItem(MENU_BAR + MONITORING_MENU + SETTINGS_MENU + SUBFOLDERS_MENU, subdirsDefaultAction, false);

				subdirsOnAction.initView(builder);
				subdirsOffAction.initView(builder);
				subdirsDefaultAction.initView(builder);

				JDocearInvisibleMenu pdfManagementPopupMenu = new JDocearInvisibleMenu(TextUtils.getText(PDF_MANAGEMENT_MENU_LANG_KEY), false, true);

				String pdfCategory = PdfUtilitiesController.getParentCategory(builder, PDF_CATEGORY);
				builder.addMenuItem(pdfCategory, pdfManagementPopupMenu, pdfCategory + PDF_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addSeparator(pdfCategory + PDF_MANAGEMENT_MENU, MenuBuilder.AFTER);

				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, importAllAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, importNewAnnotationsAction, MenuBuilder.AS_CHILD);				
				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, importAllChildAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, importNewChildAnnotationsAction, MenuBuilder.AS_CHILD);
				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, removeLinebreaksAction, MenuBuilder.AS_CHILD);
				builder.addSeparator(pdfCategory + PDF_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(pdfCategory + PDF_MANAGEMENT_MENU, deleteFileAction, MenuBuilder.AS_CHILD);

				importAllAnnotationsAction.initView(builder);
				importAllAnnotationsAction.addPropertyChangeListener(pdfManagementPopupMenu);
				importNewAnnotationsAction.initView(builder);
				importNewAnnotationsAction.addPropertyChangeListener(pdfManagementPopupMenu);				
				importAllChildAnnotationsAction.initView(builder);
				importAllChildAnnotationsAction.addPropertyChangeListener(pdfManagementPopupMenu);
				importNewChildAnnotationsAction.initView(builder);
				importNewChildAnnotationsAction.addPropertyChangeListener(pdfManagementPopupMenu);
				removeLinebreaksAction.initView(builder);
				removeLinebreaksAction.addPropertyChangeListener(pdfManagementPopupMenu);
				deleteFileAction.initView(builder);
				deleteFileAction.addPropertyChangeListener(pdfManagementPopupMenu);
			}
		});
		modeController.getUserInputListenerFactory().getRibbonBuilder().registerContributorFactory("pdf_annotations", new DocearImportAnnotationsActionContributorFactory());
		modeController.getUserInputListenerFactory().getRibbonBuilder().registerContributorFactory("pdf_monitoring", new DocearPdfMonitoringContributorFactory());
		File file = new File(Compat.getApplicationUserDirectory(), "docear_pdfutilities_ribbon.xml");
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
			modeController.getUserInputListenerFactory().getRibbonBuilder().updateRibbon(PdfUtilitiesController.class.getResource("/xml/ribbons.xml"));
		}
	}

	public static String getParentCategory(MenuBuilder builder, String parentMenu) {
		if (builder.contains(parentMenu))
			return parentMenu;
		else
			return NODE_POPUP_MENU;
	}

	private void registerListener(final ModeController modeController) {
		MapConverter.addMapsConvertedListener(new MonitorungNodeUpdater(TextUtils.getText("MapConverter.1")));
		AnnotationController.addAnnotationImporter(new PdfAnnotationImporter());
		modeController.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener() {

			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView) nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new DocearNodeDropListener());
				dropTarget.setActive(true);

				IMouseListener defaultMouseListener = modeController.getUserInputListenerFactory().getNodeMouseMotionListener();
				IMouseListener docearMouseListener = new DocearNodeMouseMotionListener(defaultMouseListener);
				node.getMainView().removeMouseMotionListener(defaultMouseListener);
				node.getMainView().addMouseMotionListener(docearMouseListener);
				node.getMainView().removeMouseListener(defaultMouseListener);
				node.getMainView().addMouseListener(docearMouseListener);
			}

			public void onViewRemoved(Container nodeView) {
			}

		});

		final OptionPanelController opc = Controller.getCurrentController().getOptionPanelController();
		opc.addButtonListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source != null && source instanceof JRadioButton) {
					JRadioButton radioButton = (JRadioButton) event.getSource();
					if (radioButton.getName().equals(OPEN_STANDARD_PDF_VIEWER_KEY)) {
						((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(true);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(false);						
						opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
						opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(false);
						List<PDFReaderHandle> readers = getPdfViewers();
						PDFReaderHandle pdfxc = null;
						for(PDFReaderHandle reader : readers){
							if(reader.getName().equals("PDF-XChange Viewer")){
								pdfxc = reader;
								break;
							}
						}
						if(pdfxc != null){							
							try {
								if(pdfxc != null && pdfxc.isDefault() && !hasCompatibleSettings(pdfxc.getExecFile())){
									int result = JOptionPane.showConfirmDialog(Controller.getCurrentController().getViewController().getJFrame(), TextUtils.getText("docear.help.pdf_xchange_viewer.warning2"), TextUtils.getText("docear.help.pdf_xchange_viewer.warning.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
									if(result == JOptionPane.OK_OPTION){
										if(pdfxc.getRegistryBranch().equals(RegistryBranch.WOW6432NODE)) {
											importRegistrySettings(getClass().getResource("/conf/PdfXChangeViewerSettingsWow6432Node.reg"));
										}
										else {
											importRegistrySettings(getClass().getResource("/conf/PdfXChangeViewerSettings.reg"));
										}
									}
								}
							}
							catch (IOException e) {
								LogUtils.severe(e);
							}
						}
					}
					if (radioButton.getName().equals(OPEN_INTERNAL_PDF_VIEWER_KEY)) {
						((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(true);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(false);						
						opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
						opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(false);
					}
					if (radioButton.getName().equals(OPEN_PDF_VIEWER_ON_PAGE_KEY)) {
						((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY)).setValue(false);
						((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY)).setValue(true);
						if(Compat.isMacOsX()) {
							opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
						}
						else {
							opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(true);
						}
						opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(true);
					}
				}
				else if(source != null && source instanceof JCheckBox) {
					JCheckBox chkBox = (JCheckBox) source;
					boolean checked = chkBox.isSelected();
					if (chkBox.getName().equals(REMOVE_LINEBREAKS_KEY)) {
						((BooleanProperty) opc.getPropertyControl(REMOVE_DASHES_KEY)).setEnabled(checked);
						((BooleanProperty) opc.getPropertyControl(KEEP_DOUBLE_LINEBREAKS_KEY)).setEnabled(checked);
						((BooleanProperty) opc.getPropertyControl(ADD_SPACES_KEY)).setEnabled(checked);
						
//						((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_BOOKMARKS_KEY)).setEnabled(checked);
//						((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_COMMENTS_KEY)).setEnabled(checked);
//						((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_HIGHLIGHTED_KEY)).setEnabled(checked);
					}
				}
			}
		});

		opc.addPropertyLoadListener(new PropertyLoadListener() {
			public void propertiesLoaded(Collection<IPropertyControl> properties) {
				if(Compat.isMacOsX()) {
					opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
				}
				((RadioButtonProperty) opc.getPropertyControl(OPEN_STANDARD_PDF_VIEWER_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals("true")) { //$NON-NLS-1$
									opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
									opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(false);
								}
							}
						});

				((RadioButtonProperty) opc.getPropertyControl(OPEN_INTERNAL_PDF_VIEWER_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals("true")) {									
									opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
									opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(false);
								}
							}
						});

				((RadioButtonProperty) opc.getPropertyControl(OPEN_PDF_VIEWER_ON_PAGE_KEY))
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getNewValue().equals("true")) {
									if(Compat.isMacOsX()) {
										opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(false);
									}
									else {
										opc.getPropertyControl(ShowPdfReaderDefinitionDialogAction.KEY).setEnabled(true);
									}
									
									opc.getPropertyControl("docear.show_install_pdf_readers").setEnabled(true);
								}
							}
						});
				((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_KEY))
				.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						boolean checked = false;
						if (evt.getNewValue().equals("true")) {
							checked = true;
						}
						changeLinebreakOptions(checked);
					}
				});				
				changeLinebreakOptions(((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_KEY)).getBooleanValue());
				
				
				((BooleanProperty) opc.getPropertyControl(IMPORT_HIGHLIGHTED_TEXTS_KEY))
				.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_POP_UP_KEY)).setEnabled(Boolean.parseBoolean(""+ evt.getNewValue()));
						if(DocearAddonController.getController().hasPlugin(IHighlightsImporter.class)){
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_HIGHLIGHTED_KEY)).setEnabled(Boolean.parseBoolean(""+ evt.getNewValue()));
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_POP_UP_HIGHLIGHTED_KEY)).setEnabled(Boolean.parseBoolean(""+ evt.getNewValue()));
						}						
					}
				});	
			
				((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_POP_UP_KEY))
				.addPropertyChangeListener(new PropertyChangeListener() {					
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getNewValue().equals("true")) {
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_HIGHLIGHTED_KEY)).setValue(false);
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_POP_UP_HIGHLIGHTED_KEY)).setValue(false);
						}						
					}
				});	
				((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_HIGHLIGHTED_KEY))
				.addPropertyChangeListener(new PropertyChangeListener() {					
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getNewValue().equals("true")) {
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_POP_UP_KEY)).setValue(false);
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_POP_UP_HIGHLIGHTED_KEY)).setValue(false);
						}						
					}
				});
				((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_POP_UP_HIGHLIGHTED_KEY))
				.addPropertyChangeListener(new PropertyChangeListener() {					
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getNewValue().equals("true")) {
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_HIGHLIGHTED_KEY)).setValue(false);
							((LinkRadioButtonProperty) opc.getPropertyControl(IMPORT_ONLY_POP_UP_KEY)).setValue(false);
						}						
					}
				});
				
							
			}
			
			private void changeLinebreakOptions(boolean checked) {
				((BooleanProperty) opc.getPropertyControl(REMOVE_DASHES_KEY)).setEnabled(checked);
				((BooleanProperty) opc.getPropertyControl(KEEP_DOUBLE_LINEBREAKS_KEY)).setEnabled(checked);
				((BooleanProperty) opc.getPropertyControl(ADD_SPACES_KEY)).setEnabled(checked);
				
//				((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_BOOKMARKS_KEY)).setEnabled(checked);
//				((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_COMMENTS_KEY)).setEnabled(checked);
//				((BooleanProperty) opc.getPropertyControl(REMOVE_LINEBREAKS_HIGHLIGHTED_KEY)).setEnabled(checked);
			}
		});

		modeController.getMapController().addNodeSelectionListener(new DocearNodeSelectionListener());

		DocearController.getController().getEventQueue().addEventListener(new IDocearEventListener() {

			public void handleEvent(DocearEvent event) {
				//DOCEAR - old
//				if (DocearEventType.NEW_INCOMING.equals(event.getType())) {
//					MapModel map = (MapModel) event.getEventObject();
//					
//					if(MonitoringUtils.setupMonitoringNode(map.getRootNode(), CoreConfiguration.DOCUMENT_REPOSITORY_PATH)) { 
//						map.setSaved(false);
//						MapUtils.saveMap(map, map.getFile());
//					}
//					
//				}
				if (DocearEventType.NEW_LITERATURE_ANNOTATIONS.equals(event.getType())) {
					MapModel map = (MapModel) event.getEventObject();
					
					if(MonitoringUtils.setupMonitoringNode(map.getRootNode(), CoreConfiguration.DOCUMENT_REPOSITORY_PATH)) { 
						map.setSaved(false);
						MapUtils.saveMap(map, map.getFile());
					}
					
				}
			}
		});
		showViewerSelectionIfNecessary();
		
		WorkspaceController.getCurrentModel().addWorldModelListener(new DefaultWorkspaceModelListener());
		//setupInitialProjects(modeController);

		modeController.getMapController().addNodeChangeListener(new PdfNodeChangeListener());		
		DocearAutoMonitoringListener autoMonitoringListener = new DocearAutoMonitoringListener();
		DocearController.getController().getLifeCycleObserver().addMapLifeCycleListener(autoMonitoringListener);
		Controller.getCurrentController().getViewController().getJFrame().addWindowFocusListener(autoMonitoringListener);
	}

	private IProjectModelListener getProjectModelListener() {
		if(this.projectModelListener == null) {
			this.projectModelListener = new DocearProjectModelListener();
		}
		return this.projectModelListener;
	}

	private void addPluginDefaults(ModeController modeController) {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null) throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE); //$NON-NLS-1$
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
		
		if (DocearController.getController().hasOutdatedConfigFiles()) {
			ResourceController.getResourceController().setProperty("docear_auto_monitoring", false);
		}
		if(!DocearAddonController.getController().hasPlugin(IHighlightsImporter.class)){
			DocearController.getPropertiesController().setProperty(IMPORT_ONLY_POP_UP_KEY, true);
			DocearController.getPropertiesController().setProperty(IMPORT_ONLY_HIGHLIGHTED_KEY, false);
			DocearController.getPropertiesController().setProperty(IMPORT_POP_UP_HIGHLIGHTED_KEY, false);
		}
	}
	
	public String buildCommandString(File reader) {
		String readerCommand = "";
		
		if (!Compat.isWindowsOS() && !Compat.isMacOsX() && reader.getName().toLowerCase().endsWith(".exe")) {
			readerCommand = "wine*";
		}
				
		String fileFunctor = "$FILE";
		if (Compat.isWindowsOS()) {
			fileFunctor = "\"$FILE\"";
		}
		
		PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
		if(Compat.isMacOsX()) {
			readerCommand += reader.getAbsolutePath() + "*"+fileFunctor+"*$PAGE";
		}
		else if (readerFilter.isPdfXChange(reader)) {
			readerCommand += reader.getAbsolutePath() + "*/A*page=$PAGE*"+fileFunctor; 
		}
		else if (readerFilter.isFoxit(reader)) {
			readerCommand += reader.getAbsolutePath() + "*"+fileFunctor+"*/A*page=$PAGE";
		}
		else if (readerFilter.isAdobe(reader)) {
			readerCommand += reader.getAbsolutePath() + "*/A*page=$PAGE*"+fileFunctor;
		}
		else if (readerFilter.isAcrobat(reader)) {
			readerCommand += reader.getAbsolutePath() + "*/A*page=$PAGE*"+fileFunctor;
		}
		else {
			readerCommand += reader.getAbsolutePath() + "*"+fileFunctor;
		}
		
		return readerCommand;
	}	

	private void addPropertiesToOptionPanel(ModeController modeController) {
		final URL preferences = this.getClass().getResource("preferences.xml"); //$NON-NLS-1$
		if (preferences == null) throw new RuntimeException("cannot open docear.pdf_utilities plugin preferences"); //$NON-NLS-1$
		
		OptionPanelBuilder builder = ((MModeController)modeController).getOptionPanelBuilder();
		builder.load(preferences);		
		builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
			
			public IPropertyControl createControl() {				
				return new LinkLabelProperty(TextUtils.getText("OptionPanel.docear_highlight_addon_text"));			
			}
		}, IndexedTree.AS_CHILD);		
		builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
			
			public IPropertyControl createControl() {						
				LinkRadioButtonProperty prop = new LinkRadioButtonProperty(IMPORT_ONLY_POP_UP_KEY, TextUtils.getText("OptionPanel.docear_import_pop_up"));				
				prop.setEnabled(true);
				return prop;
			}
		}, IndexedTree.AS_CHILD);
		if(DocearAddonController.getController().hasPlugin(IHighlightsImporter.class)){
			builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
				
				public IPropertyControl createControl() {						
					LinkRadioButtonProperty prop = new LinkRadioButtonProperty(IMPORT_ONLY_HIGHLIGHTED_KEY, TextUtils.getText("OptionPanel.docear_import_highlight_activated"));				
					prop.setEnabled(true);
					return prop;
				}
			}, IndexedTree.AS_CHILD);
			builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
				
				public IPropertyControl createControl() {						
					LinkRadioButtonProperty prop = new LinkRadioButtonProperty(IMPORT_POP_UP_HIGHLIGHTED_KEY, TextUtils.getText("OptionPanel.docear_import_pop_up_highlight_activated"));				
					prop.setEnabled(true);
					return prop;
				}
			}, IndexedTree.AS_CHILD);
		}
		else{
			builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
				
				public IPropertyControl createControl() {						
					LinkRadioButtonProperty prop = new LinkRadioButtonProperty(IMPORT_ONLY_HIGHLIGHTED_KEY, TextUtils.getText("OptionPanel.docear_import_highlight_deactivated"));				
					prop.setEnabled(false);
					return prop;
				}
			}, IndexedTree.AS_CHILD);
			builder.addCreator("pdf_management/annotations_import", new IPropertyControlCreator() {
				
				public IPropertyControl createControl() {						
					LinkRadioButtonProperty prop = new LinkRadioButtonProperty(IMPORT_POP_UP_HIGHLIGHTED_KEY, TextUtils.getText("OptionPanel.docear_import_pop_up_highlight_deactivated"));				
					prop.setEnabled(false);
					return prop;
				}
			}, IndexedTree.AS_CHILD);
		}		
	}

	public void importRegistrySettings(URL regResource) throws IOException {
		if (Compat.isMacOsX()) {
			return;
		}

		File importFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "import.reg");
		OutputStream os = new FileOutputStream(importFile);
		InputStream is = regResource.openStream();
		try {
			FileUtils.copyStream(is, os);
		}
		finally {
			os.close();
			is.close();
		}

		String[] command;
		if (Compat.isWindowsOS()) {
			try {
				WinRegistry.importFile(importFile.getAbsolutePath());
				return;
			}
			catch (Exception e) {
				command = new String[] { "regedit", "/s", importFile.getAbsolutePath() };
			}
		}
		else {
			// Linux/Unix
			command = new String[] { "wine", "regedit", "/s", importFile.getAbsolutePath() };
		}

		try {
			LogUtils.info(Arrays.toString(command));
			if (Runtime.getRuntime().exec(command).waitFor() != 0) {
				throw new IOException("Could not import document settings!");
			}
		}
		catch (InterruptedException e) {
		}
	}

	public void exportRegistryKey(String key, File exportFile) throws IOException {
		if (Compat.isMacOsX()) {
			return;
		}
		//HKEY_CURRENT_USER\Software\Tracker Software\PDFViewer\Documents
		String[] command;
		if (Compat.isWindowsOS()) {
			Object[] keyTokens = WinRegistry.parseKey(key);
			WinRegistry.exportKey((Integer)keyTokens[0], keyTokens[1].toString(), exportFile.getAbsolutePath()); 
			return;  //command = new String[] { "regedit", "/a", exportFile.getAbsolutePath(), key };
		}
		else {
			// Linux/Unix
			command = new String[] { "wine", "regedit", "/e", exportFile.getAbsolutePath(), key };
		}

		try {
			if (Runtime.getRuntime().exec(command).waitFor() != 0) {
				throw new IOException("Could not retrieve document settings!");
			}
		}
		catch (InterruptedException e) {
		}
	}

	public boolean hasCompatibleSettings(final String readerCommand) throws IOException {
		LogUtils.info("checking pdf reader settings ...");
		
		final PdfReaderFileFilter filter = new PdfReaderFileFilter();
		if (Compat.isMacOsX() || (!filter.isPdfXChange(readerCommand) && !filter.isAcrobat(readerCommand))) {
			return true;
		}		
		

		
		File exportFile = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "exported.reg");
		try {
			
			String line;
			if(filter.isPdfXChange(readerCommand)){				
				exportRegistryKey("HKEY_CURRENT_USER\\Software\\Tracker Software\\PDFViewer\\Documents", exportFile);
				BufferedReader reader = new BufferedReader(new FileReader(exportFile));
				
				exportRegistryKey("HKEY_CURRENT_USER\\Software\\Tracker Software\\PDFViewer\\Commenting", exportFile);
				reader = new BufferedReader(new FileReader(exportFile));
				int found = 0;
				try {
					while ((line = reader.readLine()) != null) {
						if ("\"CopySelTextToDrawingPopup\"=dword:00000001".equals(line.trim())) {
							found++;
						}
						else if ("\"CopySelTextToHilightPopup\"=dword:00000001".equals(line.trim())) {
							found++;
						}
						if (found == 2) {
							return true;
						}
					}
				}
				finally {
					reader.close();
				}
				
			}
			if(filter.isAcrobat(readerCommand)){
				exportRegistryKey("HKEY_CURRENT_USER\\Software\\Adobe\\Adobe Acrobat\\10.0\\Annots\\cPrefs", exportFile);
				BufferedReader reader = new BufferedReader(new FileReader(exportFile));
				try {
					while ((line = reader.readLine()) != null) {
						if ("\"bcopyTextToMarkupAnnot\"=dword:00000001".equals(line.trim())) {
							return true;
						}
					}
				}
				finally {
					reader.close();
				}
			}
		}
		catch (IOException e) {
			LogUtils.warn("org.docear.plugin.pdfutilities.PdfUtilitiesController.hasCompatibleSettings(): " + e.getMessage());
			throw new IOException("Could not validate PDF-X-Change Viewer settings!");
		}
		return false;
	}	
	
	
	public String[] getPdfReaderExecCommand(URI uriToFile, int page, String title) {
		File file = null;
		try {
			file = new File(MModeWorkspaceUrlManager.getController().getAbsoluteUri(Controller.getCurrentController().getMap(), uriToFile));
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		if (file == null) {
			return null;
		}
		
		String readerCommandProperty = DocearController.getPropertiesController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY);
		if (readerCommandProperty == null || readerCommandProperty.isEmpty()) {
			setToStandardPdfViewer();
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText(OPEN_ON_PAGE_ERROR_KEY), TextUtils.getText("warning"), JOptionPane.WARNING_MESSAGE);
			return null;
		}
				
		ArrayList<String> commandList = new ArrayList<String>(); 
		for (String s : readerCommandProperty.trim().split("\\*")) {
			commandList.add(s.replace("*", ""));
		}		
		
		String fileString = file.getAbsolutePath();
		if (!Compat.isWindowsOS() && !Compat.isMacOsX()) {
			if (commandList.get(0).endsWith("wine")) {
				fileString = "Z:" + fileString.replace("/", "\\") + "";
			}
		}
		
		if (title == null) {
			title = "";
		}
		for (int i=0; i<commandList.size(); i++) {
			commandList.set(i, commandList.get(i).replace("$PAGE", ""+page).replace("$TITLE", title).replace("$FILE", fileString));
		}
		
		return commandList.toArray(new String[]{});
	}

	public String[] getPdfReaderExecCommand(URI uriToFile, int page) {
		return getPdfReaderExecCommand(uriToFile, page, null);
	}
	
	public String[] getPdfReaderExecCommand(URI uriToFile, IAnnotation annotation) {
		int page = 1;
		String title = null;
		if (annotation != null) {
			annotation.updatePage();
			page = annotation.getPage() != null ? annotation.getPage() : 1;
			title = annotation.getTitle();
		}
		
		return getPdfReaderExecCommand(uriToFile, page, title);
	}
	
	public boolean openPdfOnPage(URI uriToFile, IAnnotation annotation) {
		int page = 1;
		String title = null;
		if (annotation != null) {
			annotation.updatePage();
			page = annotation.getPage() != null ? annotation.getPage() : 1;
			title = annotation.getTitle();
		}
		
		return openPdfOnPage(uriToFile, page, title);
	}
	
	public boolean openPdfOnPage(URI uriToFile, int page) {
		return openPdfOnPage(uriToFile, page, null);
	}
	
	public boolean openPdfOnPage(URI uriToFile, int page, String title) {
		String[] readerCommand = getPdfReaderExecCommand(uriToFile, page, title);
		
		if (readerCommand != null) {
			try {
				if (Compat.isMacOsX()) {
					return openPageMacOs(readerCommand);
				}
				else {
					Controller.exec(readerCommand);
				}
			}			
			catch (final Exception x) {
				UITools.errorMessage("Could not invoke Pdf Reader.\n\nDocear excecuted the following statement on a command line:\n\"" + Arrays.toString(readerCommand) + "\"."); //$NON-NLS-1$ //$NON-NLS-2$				
			}
		}
		
		return true;
	}
	
	public void setToStandardPdfViewer() {
		if(Compat.isMacOsX()) {
			try {
				String filePath = detectDefaultPdfReader();
				if(filePath != null) {
					setReaderPreferences(filePath);
				}
			} catch (ScriptException e) {
				//DOCEAR - todo: show error message dialog
				LogUtils.warn(e);
			} catch (IOException e) {
				//DOCEAR - todo: show error message dialog
				LogUtils.warn(e);
			}
		}
		else {
			DocearController.getPropertiesController().setProperty(OPEN_STANDARD_PDF_VIEWER_KEY, true);
			DocearController.getPropertiesController().setProperty(OPEN_INTERNAL_PDF_VIEWER_KEY, false);
			DocearController.getPropertiesController().setProperty(OPEN_PDF_VIEWER_ON_PAGE_KEY, false);
		}		
	}
	
	public boolean openPageMacOs(String[] command) {
		/*final String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY);
		if (readerPath == null || readerPath.length() == 0) {
			this.mouseListener.mouseClicked(e);
			return;
		}*/
			
		if (!command[1].toLowerCase().endsWith(".pdf")) {			
			return false;
		}
		

		//IAnnotation annotation = null;
		try {
			//annotation = new PdfAnnotationImporter().searchAnnotation(uri, command);
			//System.gc();
			//if (annotation == null || annotation.getPage() == null) {
				//Controller.exec(getExecCommandMacOs(readerPath, uri, 1));
				//runAppleScript(readerPath, uri, 1);
				//return;
			/*}
			else {
				runAppleScript(readerPath, uri, annotation.getPage());
				//Controller.exec(getExecCommandMacOs(readerPath, uri, annotation));
				return;
			}//*/
			runAppleScript(command[0], command[1], Integer.parseInt(command[2]));
		}		
		/*catch (COSLoadException x) {
			UITools.errorMessage("Could not find page because the document\n" + uri.toString() + "\nthrew a COSLoadExcpetion.\nTry to open file with standard options."); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("Caught: " + x); //$NON-NLS-1$
		}//*/
		catch (Exception x) {
			LogUtils.warn(x);
			return false;
		}
		
		return true;
	}

	private void runAppleScript(String readerPath, String filePath, int page) throws ScriptException, IOException {
    	StringBuilder builder = new StringBuilder();
    	builder.append("global pdfPath\n");
    	builder.append("global page\n");
    	builder.append("set pdfPath to POSIX file \""+filePath+"\"\n");
    	builder.append("set page to "+ page +" as text\n");
    	if(readerPath.endsWith(".app")) {
    		builder.append("set pdfReaderPath to \""+readerPath+"\"\n\n");
    	}
    	else{
    		builder.append("set pdfReaderPath to null\n\n");
    	}
    	
    	URL url = PdfUtilitiesController.class.getResource("/mac_os/OpenOnPageHead.appleScript");
    	appendResourceContent(builder, url);
    	if(readerPath.endsWith("Skim.app")) {
    		url = PdfUtilitiesController.class.getResource("/mac_os/OpenOnPageSkim.appleScript");
        	appendResourceContent(builder, url);
    	}
    	else {
    		url = PdfUtilitiesController.class.getResource("/mac_os/OpenOnPageDefault.appleScript");
        	appendResourceContent(builder, url);
    	}
    	
    	final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
       
        ScriptEngineManager mgr = new ScriptEngineManager();
    	ScriptEngine engine = mgr.getEngineByName("AppleScript");
    	
        Thread.currentThread().setContextClassLoader(contextClassLoader);    	
		engine.eval(builder.toString());		
		LogUtils.info("Successfully ran apple script");
	}
	
	private String detectDefaultPdfReader() throws ScriptException, IOException {
    	StringBuilder builder = new StringBuilder();
    	File file = new File("tmp/pdffile.pdf");
    	org.apache.commons.io.FileUtils.copyURLToFile(PdfUtilitiesController.class.getResource("/mac_os/temp.pdf"), file);
    	builder.append("set FileToExamine to POSIX file \""+file.getPath()+"\"\n");
    	URL url = PdfUtilitiesController.class.getResource("/mac_os/DetectDefaultPdfReader.appleScript");
    	appendResourceContent(builder, url);
    	    	
    	final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
       
        ScriptEngineManager mgr = new ScriptEngineManager();
    	ScriptEngine engine = mgr.getEngineByName("AppleScript");
    	
        Thread.currentThread().setContextClassLoader(contextClassLoader);   
		return (String) engine.eval(builder.toString());
	}
	
	

	private void appendResourceContent(StringBuilder builder, URL url) throws IOException {
		if (url != null) {    		
            InputStream input = url.openStream();
    		try{	    		
	            BufferedReader inStream = new BufferedReader(new InputStreamReader(input));
	            String inputLine;
	
	            while ((inputLine = inStream.readLine()) != null) {
	            	builder.append(inputLine + "\n");
	            }
    		}
    		finally{
    			input.close();
    		}
        }
    	else{
    		throw new IOException("Could not read applescript file.");
    	}
	}

	
	private class DefaultWorkspaceModelListener implements WorkspaceModelListener {
		public void projectRemoved(WorkspaceModelEvent event) {
			event.getProject().getModel().removeProjectModelListener(getProjectModelListener());
		}

		public void projectAdded(WorkspaceModelEvent event) {
			event.getProject().getModel().addProjectModelListener(getProjectModelListener());
		}

		public void treeStructureChanged(TreeModelEvent e) {}

		public void treeNodesRemoved(TreeModelEvent e) {}

		public void treeNodesInserted(TreeModelEvent e) {}

		public void treeNodesChanged(TreeModelEvent e) {}
	}
	
	private class DocearPdfMonitoringContributorFactory implements IRibbonContributorFactory {

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		public ARibbonContributor getContributor(final Properties attributes) {
			return new ARibbonContributor() {
				
				@Override
				public String getKey() {
					return attributes.getProperty("name");
				}
				
				@Override
				public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
					final JCommandButton flattenButton = RibbonActionContributorFactory.createCommandButton(RibbonActionContributorFactory.getDummyAction("flatten_dirs"));
					flattenButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
					flattenButton.setPopupCallback(new PopupPanelCallback() {
						
						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
							JCommandToggleMenuButton defaultButton = RibbonActionContributorFactory.createCommandToggleMenuButton(flattenDefaultAction);	
							flattenDefaultAction.setSelected();
							defaultButton.getActionModel().setSelected(flattenDefaultAction.isSelected());
							flattenOnAction.setSelected();
							JCommandToggleMenuButton onButton = RibbonActionContributorFactory.createCommandToggleMenuButton(flattenOnAction);
							onButton.getActionModel().setSelected(flattenOnAction.isSelected());
							flattenOffAction.setSelected();
							JCommandToggleMenuButton offButton = RibbonActionContributorFactory.createCommandToggleMenuButton(flattenOffAction);
							offButton.getActionModel().setSelected(flattenOffAction.isSelected());
							
							popupmenu.addMenuButton(defaultButton);
							popupmenu.addMenuButton(onButton);
							popupmenu.addMenuButton(offButton);
							return popupmenu;
						}
					});
					ChildProperties childProps = new ChildProperties();
					childProps.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
					addDefaultToggleHandler(context, flattenButton);
					parent.addChild(flattenButton, childProps);
					
					final JCommandButton autoMonitoringButton = RibbonActionContributorFactory.createCommandButton(RibbonActionContributorFactory.getDummyAction("auto_monitoring"));					
					autoMonitoringButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
					autoMonitoringButton.setPopupCallback(new PopupPanelCallback() {
						
						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
							JCommandToggleMenuButton defaultButton = RibbonActionContributorFactory.createCommandToggleMenuButton(autoDefaultAction);	
							autoDefaultAction.setSelected();
							defaultButton.getActionModel().setSelected(autoDefaultAction.isSelected());
							autoOnAction.setSelected();
							JCommandToggleMenuButton onButton = RibbonActionContributorFactory.createCommandToggleMenuButton(autoOnAction);
							onButton.getActionModel().setSelected(autoOnAction.isSelected());
							autoOffAction.setSelected();
							JCommandToggleMenuButton offButton = RibbonActionContributorFactory.createCommandToggleMenuButton(autoOffAction);
							offButton.getActionModel().setSelected(autoOffAction.isSelected());
							
							popupmenu.addMenuButton(defaultButton);
							popupmenu.addMenuButton(onButton);
							popupmenu.addMenuButton(offButton);
							return popupmenu;
						}
					});
					childProps = new ChildProperties();
					childProps.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
					addDefaultToggleHandler(context, autoMonitoringButton);
					parent.addChild(autoMonitoringButton, childProps);
					final JCommandButton subfoldersButton = RibbonActionContributorFactory.createCommandButton(RibbonActionContributorFactory.getDummyAction("subfolders")); 
					subfoldersButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
					subfoldersButton.setPopupCallback(new PopupPanelCallback() {
						
						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
							JCommandToggleMenuButton defaultButton = RibbonActionContributorFactory.createCommandToggleMenuButton(subdirsDefaultAction);	
							defaultButton.getActionModel().setSelected(subdirsDefaultAction.isSelected());
							JCommandToggleMenuButton onButton = RibbonActionContributorFactory.createCommandToggleMenuButton(subdirsOnAction);
							onButton.getActionModel().setSelected(subdirsOnAction.isSelected());
							JCommandToggleMenuButton offButton = RibbonActionContributorFactory.createCommandToggleMenuButton(subdirsOffAction);
							offButton.getActionModel().setSelected(subdirsOffAction.isSelected());
							
							popupmenu.addMenuButton(defaultButton);
							popupmenu.addMenuButton(onButton);
							popupmenu.addMenuButton(offButton);
							return popupmenu;
						}
					});
					childProps = new ChildProperties();
					childProps.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
					addDefaultToggleHandler(context, subfoldersButton);
					parent.addChild(subfoldersButton, childProps);
					
					context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
						public void updateState(CurrentState state) {
							NodeModel node = state.get(NodeModel.class);
							if(node != null) {
    							flattenButton.setEnabled(updateMonitoringFolderAction.isEnabled());
    							autoMonitoringButton.setEnabled(MonitoringUtils.isMonitoringNode(node));
    							subfoldersButton.setEnabled(MonitoringUtils.isMonitoringNode(node));
							}
						}
					});
				}
				
				@Override
				public void addChild(Object child, ChildProperties properties) {
				}
			};
		}
	}
	
	private class DocearImportAnnotationsActionContributorFactory implements IRibbonContributorFactory {
		
		private JCommandToggleButton importAnnotationEnabledButton;
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/
		public DocearImportAnnotationsActionContributorFactory() {
			ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
				
				public void propertyChanged(String propertyName, String newValue, String oldValue) {
					if(AUTO_IMPORT_ANNOTATIONS_KEY.equals(propertyName)) {
						if(importAnnotationEnabledButton != null) {
							importAnnotationEnabledButton.getActionModel().setSelected(DocearController.getPropertiesController().getBooleanProperty(AUTO_IMPORT_ANNOTATIONS_KEY));
						}
					}
					
				}
				
			});
			
		}
		
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		public ARibbonContributor getContributor(final Properties attributes) {
			
			return new ARibbonContributor() {
				
				@Override
				public String getKey() {
					return attributes.getProperty("name");
				}
				
				@Override
				public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
					
					final RadioButtonAction importAnnotationsAction = new RadioButtonAction("menu_auto_import_annotations", AUTO_IMPORT_ANNOTATIONS_KEY);
					importAnnotationEnabledButton = RibbonActionContributorFactory.createCommandToggleButton(importAnnotationsAction);
					importAnnotationEnabledButton.getActionModel().setSelected(DocearController.getPropertiesController().getBooleanProperty(AUTO_IMPORT_ANNOTATIONS_KEY));
					importAnnotationEnabledButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							importAnnotationsAction.actionPerformed(e);
						}
					});
					ChildProperties childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
					childProps.set(RibbonElementPriority.class, RibbonActionContributorFactory.getPriority(attributes.getProperty("priority", "")));
					addDefaultToggleHandler(context, importAnnotationEnabledButton);
					parent.addChild(importAnnotationEnabledButton, childProps);
					
					importAllAnnotationsAction.setEnabled();
					importAllChildAnnotationsAction.setEnabled();
					final JCommandButton annoButton = RibbonActionContributorFactory.createCommandButton(RibbonActionContributorFactory.getDummyAction("annotations")); 
					annoButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
					annoButton.setPopupCallback(new PopupPanelCallback() {
						
						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
							if(importAllAnnotationsAction.isEnabled()) {
								popupmenu.addMenuButton(RibbonActionContributorFactory.createCommandMenuButton(importAllAnnotationsAction));
								popupmenu.addMenuButton(RibbonActionContributorFactory.createCommandMenuButton(importNewAnnotationsAction));
							}
							else {
								popupmenu.addMenuButton(RibbonActionContributorFactory.createCommandMenuButton(importAllChildAnnotationsAction));
								popupmenu.addMenuButton(RibbonActionContributorFactory.createCommandMenuButton(importNewChildAnnotationsAction));
								popupmenu.addMenuButton(RibbonActionContributorFactory.createCommandMenuButton(removeLinebreaksAction));
							}
							return popupmenu;
						}
					});
					childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
					childProps.set(RibbonElementPriority.class, RibbonActionContributorFactory.getPriority(attributes.getProperty("priority", "")));
					parent.addChild(annoButton, childProps);
					

					final JCommandButton delButton = RibbonActionContributorFactory.createCommandButton(deleteFileAction); 
					deleteFileAction.setEnabled();
					delButton.setEnabled(deleteFileAction.isEnabled());
					
					childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
					childProps.set(RibbonElementPriority.class, RibbonActionContributorFactory.getPriority(attributes.getProperty("priority", "")));
					addDefaultToggleHandler(context, deleteFileAction, delButton);
					parent.addChild(delButton, childProps);
					
					context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
						public void updateState(CurrentState state) {
							importAllAnnotationsAction.setEnabled();
							importAllChildAnnotationsAction.setEnabled();
							annoButton.setEnabled((importAllAnnotationsAction.isEnabled() || importAllChildAnnotationsAction.isEnabled()) && !state.allMapsClosed());
//							deleteFileAction.setEnabled();
//							delButton.setEnabled(deleteFileAction.isEnabled());
						}
					});
					
				}
				
				@Override
				public void addChild(Object child, ChildProperties properties) {
				}
			};
		}
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final AFreeplaneAction action, final Component component) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(CurrentState state) {				
				if(state.allMapsClosed()) {
					if (action.getValue(RibbonActionContributorFactory.MANDATORY_PROPERTY) == null) {
						component.setEnabled(action.isAlwaysEnabled());
					}
				}
				else {					
					action.setEnabled();
					component.setEnabled(action.isEnabled());
				}
			}
		});
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final Component component) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(CurrentState state) {
				if(state.isNodeChangeEvent()) {					
				}
				else if(state.allMapsClosed()) {					
					component.setEnabled(false);
				}
				else if (state.get(TreeSelectionEvent.class) == null) {
					component.setEnabled(true);
				}
			}
		});
	}

}
