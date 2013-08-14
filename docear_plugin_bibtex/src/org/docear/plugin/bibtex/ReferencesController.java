package org.docear.plugin.bibtex;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.JabRefPreferences;

import org.docear.plugin.bibtex.actions.AddExistingReferenceAction;
import org.docear.plugin.bibtex.actions.AddNewReferenceAction;
import org.docear.plugin.bibtex.actions.AddOrUpdateReferenceEntryWorkspaceAction;
import org.docear.plugin.bibtex.actions.AddRecommendedDocumentAction;
import org.docear.plugin.bibtex.actions.ChangeBibtexDatabaseAction;
import org.docear.plugin.bibtex.actions.CopyBibtexToClipboard;
import org.docear.plugin.bibtex.actions.CopyCiteKeyToClipboard;
import org.docear.plugin.bibtex.actions.ReferenceQuitAction;
import org.docear.plugin.bibtex.actions.RemoveReferenceAction;
import org.docear.plugin.bibtex.actions.ShowInReferenceManagerAction;
import org.docear.plugin.bibtex.actions.ShowJabrefPreferencesAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesAllOpenMapsAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesCurrentMapAction;
import org.docear.plugin.bibtex.actions.UpdateReferencesInLibrary;
import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.bibtex.jabref.JabRefBaseHandle;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.docear.plugin.bibtex.jabref.labelPattern.ILabelPattern;
import org.docear.plugin.bibtex.listeners.BibtexNodeDropListener;
import org.docear.plugin.bibtex.listeners.JabRefChangeListener;
import org.docear.plugin.bibtex.listeners.MapChangeListenerAdapter;
import org.docear.plugin.bibtex.listeners.NodeAttributeListener;
import org.docear.plugin.bibtex.listeners.NodeSelectionListener;
import org.docear.plugin.bibtex.listeners.SplmmMapsConvertListener;
import org.docear.plugin.core.ALanguageController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.workspace.model.DocearProjectChangedEvent;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.model.IDocearProjectListener;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.map.MapConverter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IKeyStrokeProcessor;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.KeyBindingProcessor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.features.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.features.ProjectURLHandler;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelListener;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;
import org.freeplane.plugin.workspace.model.project.ProjectSelectionEvent;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.view.swing.map.NodeView;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class ReferencesController extends ALanguageController implements IDocearEventListener {
	
	//mapModel with reference which is currently changed
	private MapModel inChange = null;
	//MapModel with reference which is currently added
	private MapModel inAdd = null;
	private BibtexEntry addedEntry = null;
	
	private final static JabRefChangeListener jabRefChangeListener = new JabRefChangeListener();	
	
	private static ReferencesController referencesController = null;	
	
	JabrefWrapper jabrefWrapper;
	
	private JabRefAttributes jabRefAttributes;
	private SplmmAttributes splmmAttributes;
	
	private final NodeAttributeListener attributeListener = new NodeAttributeListener();
	private final SplmmMapsConvertListener splmmMapsConvertedListener = new SplmmMapsConvertListener();

	public static final String MENU_BAR = "/menu_bar"; //$NON-NLS-1$
	public static final String NODE_POPUP_MENU = "/node_popup"; //$NON-NLS-1$
	public static final String NODE_FEATURES_MENU = "/node_features"; //$NON-NLS-1$
	public static final String TOOLS_MENU = "/extras"; //$NON-NLS-1$
	public static final String REFERENCE_MANAGEMENT_MENU = "/reference_management";
	public static final String UPDATE_REFERENCES_MENU = "/update_references";

	public static final String REFERENCE_MANAGEMENT_MENU_LANG_KEY = "menu_reference_management";
	public static final String UPDATE_REFERENCES_MENU_LANG_KEY = "menu_update_references";
//	private static final String CONVERT_SPLMM_REFERENCES_LANG_KEY = "menu_update_splmm_references_current_map";
	

	private ModeController modeController;
	private AFreeplaneAction updateReferencesCurrentMap = new UpdateReferencesCurrentMapAction();
	private AFreeplaneAction updateReferencesAllOpenMaps = new UpdateReferencesAllOpenMapsAction();
	private AFreeplaneAction updateReferencesInLibrary = new UpdateReferencesInLibrary();
	private AFreeplaneAction updateReferencesAllMaps = new UpdateReferencesAllMapsAction();
	private AFreeplaneAction addExistingReference = new AddExistingReferenceAction();
	private AFreeplaneAction changeBibtexDatabase = new ChangeBibtexDatabaseAction();
	private AFreeplaneAction removeReference = new RemoveReferenceAction();
	private AFreeplaneAction addNewReference = new AddNewReferenceAction();

	private AFreeplaneAction copyBibtex = new CopyBibtexToClipboard();
	private AFreeplaneAction copyCiteKey = new CopyCiteKeyToClipboard();
	
	private IDocearProjectListener projectListener;
	private IProjectSelectionListener projectSelectionListener;
	private Runnable runOnce;

	public ReferencesController(ModeController modeController) {
		super();
		new ReferencesPreferences(modeController);
		
		setReferencesController(this);
		setPreferencesForDocear();
		this.modeController = modeController;
		LogUtils.info("starting DocearReferencesController(ModeController)"); //$NON-NLS-1$

		this.addPluginDefaults();
		this.addMenuEntries();
		this.registerListeners();

		this.initJabref();
		//setupInitialProjects(modeController);
	}

	private void setPreferencesForDocear() {
		JabRefPreferences.getInstance(JabrefWrapper.class).put("groupAutoShow", "false");
		JabRefPreferences.getInstance(JabrefWrapper.class).put("searchPanelVisible", "false");
		JabRefPreferences.getInstance(JabrefWrapper.class).setLabelPatternSavePackage(ILabelPattern.class);
	}
	

	private void registerListeners() {		
		MapConverter.addMapsConvertedListener(splmmMapsConvertedListener);
		
		
		this.modeController.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener() {
			
			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView) nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new BibtexNodeDropListener());
				dropTarget.setActive(true);				
			}

			public void onViewRemoved(Container nodeView) {
			}
		});
		
		MapChangeListenerAdapter changeListenerAdapter = new MapChangeListenerAdapter();		
		this.modeController.getMapController().addNodeChangeListener(changeListenerAdapter);
		this.modeController.getMapController().addMapChangeListener(changeListenerAdapter);
		DocearController.getController().getLifeCycleObserver().addMapLifeCycleListener(changeListenerAdapter);
		
		DocearController.getController().getEventQueue().addEventListener(this);
		Controller.getCurrentController().addAction(new AddRecommendedDocumentAction());
	}
	
	

	public static ReferencesController getController() {
		return referencesController;
	}

	public static void setReferencesController(ReferencesController referencesController) {
		ReferencesController.referencesController = referencesController;
	}

	private void createOptionPanel(JPanel comp) {
		try {
			final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format")
					.getComponent(1);
			Dimension fixSize =  new Dimension(tabs.getComponent(0).getWidth(), 32000);
			comp.setPreferredSize(fixSize);
			tabs.add(TextUtils.getText("jabref"), comp);
			tabs.setSelectedComponent(comp);
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
	}

	
	private void initJabref() {
		this.jabRefAttributes = new JabRefAttributes();
		this.splmmAttributes = new SplmmAttributes();
		
		final ClassLoader classLoader = getClass().getClassLoader();
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					Thread.currentThread().setContextClassLoader(classLoader);
					
					jabrefWrapper = new JabrefWrapper(Controller.getCurrentController().getViewController().getJFrame());
					 
					modeController.getExtension(KeyBindingProcessor.class).addKeyStrokeProcessor(new KeyBindInterceptor());
					createOptionPanel(jabrefWrapper.getJabrefFrame());
					
					WorkspaceController.getModeExtension(modeController).getView().addProjectSelectionListener(getProjectSelectionListener());
				}
			});
		}
		catch (Throwable e) {
			LogUtils.severe(e);
		}
		Controller.getCurrentController().addAction(new ShowJabrefPreferencesAction());
		
		NodeSelectionListener nodeSelectionListener = new NodeSelectionListener();
		nodeSelectionListener.init();
		
		runOnce = new Runnable() {
			public void run() {
				//removeSelf();
				WorkspacePopupMenu popupMenu = new DefaultFileNode("temp", new File("temp.tmp")).getContextMenu();
				WorkspacePopupMenuBuilder.insertAction(popupMenu, "workspace.action.addOrUpdateReferenceEntry", 0);
				WorkspacePopupMenuBuilder.insertAction(popupMenu, WorkspacePopupMenuBuilder.SEPARATOR, 1);
//				popupMenu = new LinkTypeFileNode().getContextMenu();
//				WorkspacePopupMenuBuilder.insertAction(popupMenu, "workspace.action.addOrUpdateReferenceEntry", 0);
//				WorkspacePopupMenuBuilder.insertAction(popupMenu, WorkspacePopupMenuBuilder.SEPARATOR, 1);
			}
		};
		
		WorkspaceController.getModeExtension(modeController).getModel().addWorldModelListener(new WorkspaceModelListener() {
			
			public void treeStructureChanged(TreeModelEvent arg0) {}
			
			@Override
			public void treeNodesRemoved(TreeModelEvent arg0) {}
			
			@Override
			public void treeNodesInserted(TreeModelEvent arg0) {}
			
			@Override
			public void treeNodesChanged(TreeModelEvent arg0) {}
			
			@Override
			public void projectRemoved(WorkspaceModelEvent event) {
				if(event.getProject() instanceof DocearWorkspaceProject) {
					try {
						final File file = URIUtils.getFile(ProjectURLHandler.resolve(event.getProject(), ((DocearWorkspaceProject)event.getProject()).getBibtexDatabase().toURL()).toURI());
						if(file == null) {
							return;
						}
						addOrUpdateProjectExtension((DocearWorkspaceProject) event.getProject(), null);
					} catch (Exception e) {
						LogUtils.warn(e);
					}
				}
			}
			
			@Override
			public void projectAdded(WorkspaceModelEvent event) {
				if(event.getProject() instanceof DocearWorkspaceProject) {
					((DocearWorkspaceProject) event.getProject()).addProjectListener(getProjectListener());
				}
				initContextMenusOnce();
			}
		});
		
		
	}
	
	protected void initContextMenusOnce() {
		//insert some extra actions to file nodes
		synchronized(this) {
    		if(runOnce != null) {
    			SwingUtilities.invokeLater(runOnce);
    			runOnce = null;
    		}
		}
		
	}
	
	private void addOrUpdateProjectExtension(DocearWorkspaceProject project, JabRefBaseHandle handle) {
		JabRefProjectExtension ext = (JabRefProjectExtension) project.getExtensions(JabRefProjectExtension.class);
		if(handle == null) {			
			project.removeExtension(JabRefProjectExtension.class);
			if(ext != null) {
				final JabRefBaseHandle extHandle = ext.getBaseHandle();
				extHandle.removeProjectConnection(project);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ReferencesController contr = ReferencesController.getController();
						JabrefWrapper wrapper = contr.getJabrefWrapper();
						extHandle.getBasePanel().getSaveAction().run();
						wrapper.closeDatabase(extHandle);						
					}
				});
			}
			return;
		}		
		
		if(ext == null) {
			ext = new JabRefProjectExtension(handle);
			project.addExtension(JabRefProjectExtension.class, ext);
		}
		else {
			ext.setBaseHandle(handle);
		}
		handle.addProjectConnection(project);
	}
	
	private void showBasePanel(DocearWorkspaceProject project) {
		final JabRefProjectExtension ext = (JabRefProjectExtension) project.getExtensions(JabRefProjectExtension.class);
			if(ext != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ReferencesController contr = ReferencesController.getController();
					JabrefWrapper wrapper = contr.getJabrefWrapper();
					wrapper.getJabrefFrame().showBasePanel(ext.getBaseHandle().getBasePanel());						
				}
			});
		}
	}

	
	private IDocearProjectListener getProjectListener() {
		if(projectListener == null) {
			projectListener = new IDocearProjectListener() {
				@Override
				public void changed(DocearProjectChangedEvent event) {					
					if(DocearEventType.LIBRARY_CHANGED.equals(event.getDescriptor()) && event.getObject() instanceof IBibtexDatabase) {
						addOrUpdateProjectExtension((DocearWorkspaceProject) event.getSource(), null);
						loadDatabase(event.getSource());
					}
				}
			};
		}
		return projectListener;
	}
	
	private void loadDatabase(final DocearWorkspaceProject project) {
		final File file = URIUtils.getAbsoluteFile(project.getBibtexDatabase());
		if(file == null) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			private int count = 0;
			public void run() {
				ClassLoader old = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(ResourceController.class.getClassLoader());
				try {
					ReferencesController contr = ReferencesController.getController();
					JabrefWrapper wrapper = contr.getJabrefWrapper();
					JabRefBaseHandle handle = wrapper.openDatabase(file, true);
					addOrUpdateProjectExtension(project, handle);
				}
				catch (Throwable e) {
					if(count < 5) {
						count++;
						try {
							Thread.sleep(200);
						}
						catch (InterruptedException e1) {
						}
						SwingUtilities.invokeLater(this);
						System.out.println(count+": "+file);
						LogUtils.warn(e.getClass()+": "+e.getMessage());
					}
					else {
						throw new RuntimeException(e);
					}
				}
				finally {
					Thread.currentThread().setContextClassLoader(old);
				}
			}
		});
	}
	
	private IProjectSelectionListener getProjectSelectionListener() {
		if(this.projectSelectionListener == null) {
			this.projectSelectionListener = new IProjectSelectionListener() {
				public void selectionChanged(ProjectSelectionEvent event) {
					if(event.getSelectedProject() instanceof DocearWorkspaceProject) {
						showBasePanel((DocearWorkspaceProject) event.getSelectedProject());
					}
					
				}
			};
		}
		return this.projectSelectionListener;
	}

	public JabRefAttributes getJabRefAttributes() {
		return jabRefAttributes;
	}
	
	public SplmmAttributes getSplmmAttributes() {
		return splmmAttributes;
	}

	public JabrefWrapper getJabrefWrapper() {
		return jabrefWrapper;
	}

	public void setJabrefWrapper(JabrefWrapper jabrefWrapper) {
		this.jabrefWrapper = jabrefWrapper;
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE); //$NON-NLS-1$
		ResourceController res = ResourceController.getResourceController();
		res.addDefaults(defaults);
		res.setDefaultProperty(ChangeBibtexDatabaseAction.KEY+".icon", "/images/docear/project/Project-ChangeReferenceDB.png");
		
		res.setDefaultProperty(AddExistingReferenceAction.KEY+".icon", "/images/docear/references/References-AddExisting.png");
		res.setDefaultProperty(RemoveReferenceAction.KEY+".icon", "/images/docear/references/References-Remove.png");
		res.setDefaultProperty(CopyBibtexToClipboard.KEY+".icon", "/images/docear/references/References-CopyReferenceKeys.png");
		res.setDefaultProperty(CopyCiteKeyToClipboard.KEY+".icon", "/images/docear/references/References-CopyReferenceKeysLaTeX.png");
		res.setDefaultProperty(AddOrUpdateReferenceEntryWorkspaceAction.KEY+".icon", "/images/docear/references/References-CreateOrUpdate.png");
		res.setDefaultProperty(AddNewReferenceAction.KEY+".icon", "/images/docear/references/References-CreateOrUpdate.png");
		res.setDefaultProperty(UpdateReferencesCurrentMapAction.KEY+".icon", "/images/docear/references/References-RefreshInCurrentMap.png");
		res.setDefaultProperty(ShowInReferenceManagerAction.KEY+".icon", "/images/docear/references/References-ShowInReferenceManager.png");
		
		res.setDefaultProperty("ShowAllAttributesAction.icon", "/images/docear/references/ReferencesSettings-ShowAllAttributes.png");
		res.setDefaultProperty("HideAllAttributesAction.icon", "/images/docear/references/ReferencesSettings-HideAllAttributes.png");
		
		res.setDefaultProperty("show_jabref_preferences.icon", "/images/docear/tools/tools-PreferencesReferences.png");
		
	}
	
	private void addMenuEntries() {
		final ShowInReferenceManagerAction showRefAction = new ShowInReferenceManagerAction();
		WorkspaceController.addAction(changeBibtexDatabase);
		WorkspaceController.addAction(addExistingReference);
		WorkspaceController.addAction(removeReference);
		WorkspaceController.addAction(copyBibtex);
		WorkspaceController.addAction(copyCiteKey);
		WorkspaceController.addAction(addNewReference);
		WorkspaceController.addAction(addExistingReference);
		WorkspaceController.addAction(showRefAction);
		WorkspaceController.addAction(updateReferencesAllMaps);
		WorkspaceController.addAction(updateReferencesAllOpenMaps);
		WorkspaceController.addAction(updateReferencesCurrentMap);
//		WorkspaceController.addAction(updateReferencesInLibrary);		
		WorkspaceController.addAction(new AddOrUpdateReferenceEntryWorkspaceAction());
		
		
		this.modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				
				String referencesCategory = PdfUtilitiesController.getParentCategory(builder, PdfUtilitiesController.REFERENCE_CATEGORY);
				
				
				//RIBBONS builder.addMenuItem
				builder.addMenuItem(MENU_BAR + TOOLS_MENU, new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)),
						MENU_BAR + REFERENCE_MANAGEMENT_MENU, MenuBuilder.BEFORE);

				
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, showRefAction,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, copyBibtex,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, copyCiteKey,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, addNewReference, MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, new ImportMetadateForNodeLink(), MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, addExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, removeReference, MenuBuilder.AS_CHILD);

				JMenu updRefMenuBar = new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY));
				updRefMenuBar.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						updateReferencesInLibrary.setEnabled();
						updateReferencesAllOpenMaps.setEnabled();
						updateReferencesAllMaps.setEnabled();
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {}
				});
				
				builder.addMenuItem(MENU_BAR + REFERENCE_MANAGEMENT_MENU,
						updRefMenuBar, MENU_BAR + REFERENCE_MANAGEMENT_MENU
								+ UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, updateReferencesCurrentMap,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, updateReferencesAllOpenMaps,
						MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, updateReferencesInLibrary,
//						MenuBuilder.AS_CHILD);

				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, updateReferencesAllMaps,
						MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, ConvertSplmmReferences,
//						MenuBuilder.AS_CHILD);
				
				JMenu refMenu = new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY));
				builder.addMenuItem(referencesCategory, refMenu , referencesCategory + REFERENCE_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addSeparator(referencesCategory + REFERENCE_MANAGEMENT_MENU, MenuBuilder.AFTER);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, showRefAction, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, copyBibtex, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, copyCiteKey, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, addNewReference, MenuBuilder.AS_CHILD);
//				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, new ImportMetadateForNodeLink(), MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, addExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, removeReference, MenuBuilder.AS_CHILD);
				
				
				final JMenu updRefMenu = new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY));
				builder.addMenuItem(referencesCategory + REFERENCE_MANAGEMENT_MENU,	updRefMenu, referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						updateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						updateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
//				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
//						updateReferencesInLibrary, MenuBuilder.AS_CHILD);

				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						updateReferencesAllMaps, MenuBuilder.AS_CHILD);
//				builder.addAction(parentMenu + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, 
//						ConvertSplmmReferences,	MenuBuilder.AS_CHILD);
				
//				builder.addAction(MENU_BAR + TOOLS_MENU, ShowJabrefPreferences, MenuBuilder.AS_CHILD);
				
				updRefMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						updateReferencesInLibrary.setEnabled();
						updateReferencesAllOpenMaps.setEnabled();
						updateReferencesAllMaps.setEnabled();
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {}
				});
				
				refMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						showRefAction.setEnabled();
						try {	
				    		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
				    		WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
				    		updRefMenu.setEnabled(modelExt.getProject() != null && modelExt.getProject().isLoaded());
						}
						catch (Exception cause) {
							updRefMenu.setEnabled(false);
						}
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {}
				});

			}
		});
		modeController.getUserInputListenerFactory().getRibbonBuilder().registerContributorFactory("UpdateReferencesAllMapsAction", new UpdateReferencesAllMapsActionContributorFactory(WorkspaceController.getModeExtension(modeController)));
		modeController.getUserInputListenerFactory().getRibbonBuilder().registerContributorFactory("ChangeBibtexDatabaseAction", new ChangeBibtexDatabaseActionContributorFactory(WorkspaceController.getModeExtension(modeController)));
		File file = new File(Compat.getApplicationUserDirectory(), "docear_references_ribbon.xml");		
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
			modeController.getUserInputListenerFactory().getRibbonBuilder().updateRibbon(ReferencesController.class.getResource("/xml/ribbons.xml"));
		}
	}
	
	

	public void handleEvent(DocearEvent event) {
//		if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST && event.getEventObject() instanceof LinkTypeReferencesNode) {
//			final File file = URIUtils.getAbsoluteFile(((LinkTypeReferencesNode)event.getEventObject()).getUri());
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					ReferencesController contr = ReferencesController.getController();
//					JabrefWrapper wrapper = contr.getJabrefWrapper();
//					wrapper.replaceDatabase(file, true);
//				}
//			});
//		} else 
		if(event.getType() == DocearEventType.APPLICATION_CLOSING) {
			new ReferenceQuitAction().actionPerformed(null);
		}
	}

	public NodeAttributeListener getAttributeListener() {
		return attributeListener;
	}
	
	public static JabRefChangeListener getJabRefChangeListener() {
		return jabRefChangeListener;
	}


	public MapModel getInChange() {
		return inChange;
	}


	public void setInChange(MapModel inChange) {
		this.inChange = inChange;
	}


	public MapModel getInAdd() {
		return inAdd;
	}

	public void setInAdd(MapModel inAdd) {
		this.inAdd = inAdd;
	}
	
	public void setAddedEntry(BibtexEntry entry) {
		this.addedEntry = entry;
	}
	
	public BibtexEntry getAddedEntry() {
		return this.addedEntry;
	}
	
	private class KeyBindInterceptor implements IKeyStrokeProcessor {
		
		public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed, boolean consumed) {
			Object source = e.getSource();
			if(hasPackageNameOrAncestor(source, "net.sf.jabref")) {
				if(jabrefWrapper.getJabrefFrame().getMenuBar().processKeyBinding(ks, e, JComponent.WHEN_IN_FOCUSED_WINDOW, pressed)) {
					e.consume();
				}
				return true;
			}
			return false;
		}
		
		private boolean hasPackageNameOrAncestor(Object obj, String packageName) {
			if(obj == null || packageName == null) {
				return false;
			}
			String str = obj.getClass().getPackage().getName();
			if(str.startsWith(packageName)) {
				return true;
			} 
			else {
				if(obj instanceof Component) {
					return hasPackageNameOrAncestor(((Component) obj).getParent(), packageName);
				}
			}
			return false;
		}
	}
	
	private class UpdateReferencesAllMapsActionContributorFactory implements IRibbonContributorFactory {
		private JCommandButton button;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public UpdateReferencesAllMapsActionContributorFactory(AWorkspaceModeExtension workspaceModeExtension) {
			workspaceModeExtension.getView().addProjectSelectionListener(new IProjectSelectionListener() {
				public void selectionChanged(ProjectSelectionEvent event) {
					boolean enabled = (event.getSelectedProject() != null);
					if(button != null) {
						button.setEnabled(enabled);
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
		
		@Override
		public ARibbonContributor getContributor(final Properties attributes) {
			return new ARibbonContributor() {
				public String getKey() {
					return attributes.getProperty("name");
				}
				
				@Override
				public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
					button = RibbonActionContributorFactory.createCommandButton(updateReferencesAllMaps);
					RibbonActionContributorFactory.updateRichTooltip(button, updateReferencesAllMaps, context.getBuilder().getAcceleratorManager().getAccelerator(updateReferencesAllMaps.getKey()));
					updateReferencesAllMaps.setEnabled();
					button.setEnabled(updateReferencesAllMaps.isEnabled());
					ChildProperties childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
					childProps.set(RibbonElementPriority.class, RibbonActionContributorFactory.getPriority(attributes.getProperty("priority", "")));
					parent.addChild(button, childProps);
				}
				
				@Override
				public void addChild(Object child, ChildProperties properties) {
				}
			};
		}
	}
	
	private class ChangeBibtexDatabaseActionContributorFactory implements IRibbonContributorFactory {
		private JCommandButton button;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public ChangeBibtexDatabaseActionContributorFactory(AWorkspaceModeExtension workspaceModeExtension) {
			workspaceModeExtension.getView().addProjectSelectionListener(new IProjectSelectionListener() {
				public void selectionChanged(ProjectSelectionEvent event) {
					boolean enabled = (event.getSelectedProject() != null);
					if(button != null) {
						button.setEnabled(enabled);
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
		
		@Override
		public ARibbonContributor getContributor(final Properties attributes) {
			return new ARibbonContributor() {
				public String getKey() {
					return attributes.getProperty("name");
				}
				
				@Override
				public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
					button = RibbonActionContributorFactory.createCommandButton(changeBibtexDatabase);
					RibbonActionContributorFactory.updateRichTooltip(button, changeBibtexDatabase, context.getBuilder().getAcceleratorManager().getAccelerator(updateReferencesAllMaps.getKey()));
					changeBibtexDatabase.setEnabled();
					button.setEnabled(changeBibtexDatabase.isEnabled());
					ChildProperties childProps = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
					childProps.set(RibbonElementPriority.class, RibbonActionContributorFactory.getPriority(attributes.getProperty("priority", "")));
					parent.addChild(button, childProps);
				}
				
				@Override
				public void addChild(Object child, ChildProperties properties) {
				}
			};
		}
	}

}
