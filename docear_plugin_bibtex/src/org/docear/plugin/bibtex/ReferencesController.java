package org.docear.plugin.bibtex;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.JabRefPreferences;

import org.docear.plugin.bibtex.actions.AddExistingReferenceAction;
import org.docear.plugin.bibtex.actions.AddNewReferenceAction;
import org.docear.plugin.bibtex.actions.AddOrUpdateReferenceEntryWorkspaceAction;
import org.docear.plugin.bibtex.actions.AddRecommendedDocumentAction;
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
import org.freeplane.core.ui.IKeyStrokeInterceptor;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.features.ProjectURLHandler;
import org.freeplane.plugin.workspace.model.WorkspaceModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceModelListener;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;
import org.freeplane.plugin.workspace.model.project.ProjectSelectionEvent;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.view.swing.map.NodeView;

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
	private static final String ADD_NEW_REFERENCE_LANG_KEY = "menu_add_new_reference";
	private static final String ADD_EXISTING_REFERENCES_LANG_KEY = "menu_add_existing_references";
	private static final String REMOVE_REFERENCE_LANG_KEY = "menu_remove_references";
	private static final String UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY = "menu_update_references_all_open_maps";
	private static final String UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY = "menu_update_references_current_map";
//	private static final String CONVERT_SPLMM_REFERENCES_LANG_KEY = "menu_update_splmm_references_current_map";
	

	private ModeController modeController;
	private AFreeplaneAction UpdateReferencesCurrentMap = new UpdateReferencesCurrentMapAction(
			UPDATE_REFERENCES_CURRENT_MAP_LANG_KEY);
	private AFreeplaneAction UpdateReferencesAllOpenMaps = new UpdateReferencesAllOpenMapsAction(
			UPDATE_REFERENCES_ALL_OPEN_MAPS_LANG_KEY);
	private AFreeplaneAction UpdateReferencesInLibrary = new UpdateReferencesInLibrary();
	private AFreeplaneAction UpdateReferencesAllMaps = new UpdateReferencesAllMapsAction();
//	private AFreeplaneAction ConvertSplmmReferences = new ConvertSplmmReferencesAction(CONVERT_SPLMM_REFERENCES_LANG_KEY);
	private AFreeplaneAction AddExistingReference = new AddExistingReferenceAction(ADD_EXISTING_REFERENCES_LANG_KEY);
	private AFreeplaneAction RemoveReference = new RemoveReferenceAction(REMOVE_REFERENCE_LANG_KEY);
	private AFreeplaneAction AddNewReference = new AddNewReferenceAction(ADD_NEW_REFERENCE_LANG_KEY);
	private AFreeplaneAction CopyBibtex = new CopyBibtexToClipboard();
	private AFreeplaneAction CopyCiteKey = new CopyCiteKeyToClipboard();
	
	//private AFreeplaneAction ShowJabrefPreferences = new ShowJabrefPreferencesAction("show_jabref_preferences");
	private IDocearProjectListener projectListener;
	private IProjectSelectionListener projectSelectionListener;

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
		this.modeController.getMapController().addMapLifeCycleListener(changeListenerAdapter);
		
		DocearController.getController().addDocearEventListener(this);
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
					 
					modeController.getUserInputListenerFactory().getMenuBar().addKeyStrokeInterceptor(new KeyBindInterceptor());
					createOptionPanel(jabrefWrapper.getJabrefFrame());
					
					WorkspaceController.getModeExtension(modeController).getView().addProjectSelectionListener(getProjectSelectionListener());
				}
			});
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
		Controller.getCurrentController().addAction(new ShowJabrefPreferencesAction("show_jabref_preferences"));
		
		NodeSelectionListener nodeSelectionListener = new NodeSelectionListener();
		nodeSelectionListener.init();
		
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
			}
		});
		
		//insert some extra actions to file nodes
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//removeSelf();
				WorkspacePopupMenu popupMenu = new DefaultFileNode("temp", new File("temp.tmp")).getContextMenu();
				WorkspacePopupMenuBuilder.insertAction(popupMenu, "workspace.action.addOrUpdateReferenceEntry", 0);
				WorkspacePopupMenuBuilder.insertAction(popupMenu, WorkspacePopupMenuBuilder.SEPARATOR, 1);
				popupMenu = new LinkTypeFileNode().getContextMenu();
				WorkspacePopupMenuBuilder.insertAction(popupMenu, "workspace.action.addOrUpdateReferenceEntry", 0);
				WorkspacePopupMenuBuilder.insertAction(popupMenu, WorkspacePopupMenuBuilder.SEPARATOR, 1);
			}
		});
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
						final File file = URIUtils.getAbsoluteFile(((IBibtexDatabase)event.getObject()).getUri());
						if(file == null) {
							return;
						}
						final DocearWorkspaceProject project = event.getSource();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {								
								ReferencesController contr = ReferencesController.getController();
								JabrefWrapper wrapper = contr.getJabrefWrapper();								
								JabRefBaseHandle handle = wrapper.openDatabase(file, true);
								addOrUpdateProjectExtension(project, handle);
							}
						});
					}
				}
			};
		}
		return projectListener;
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
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}
	
	private void addMenuEntries() {
		Controller.getCurrentController().addAction(new AddOrUpdateReferenceEntryWorkspaceAction());

		this.modeController.addMenuContributor(new IMenuContributor() {

			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				
				String referencesCategory = PdfUtilitiesController.getParentCategory(builder, PdfUtilitiesController.REFERENCE_CATEGORY);
				
				builder.addMenuItem(MENU_BAR + TOOLS_MENU, new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)),
						MENU_BAR + REFERENCE_MANAGEMENT_MENU, MenuBuilder.BEFORE);

				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, new ShowInReferenceManagerAction(),	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, CopyBibtex,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, CopyCiteKey,	MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, new ImportMetadateForNodeLink(), MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU, RemoveReference, MenuBuilder.AS_CHILD);

				JMenu updRefMenuBar = new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY));
				updRefMenuBar.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						UpdateReferencesInLibrary.setEnabled();
						UpdateReferencesAllMaps.setEnabled();
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {}
				});
				
				builder.addMenuItem(MENU_BAR + REFERENCE_MANAGEMENT_MENU,
						updRefMenuBar, MENU_BAR + REFERENCE_MANAGEMENT_MENU
								+ UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesCurrentMap,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllOpenMaps,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesInLibrary,
						MenuBuilder.AS_CHILD);
				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, UpdateReferencesAllMaps,
						MenuBuilder.AS_CHILD);
//				builder.addAction(MENU_BAR + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, ConvertSplmmReferences,
//						MenuBuilder.AS_CHILD);
				
				
				builder.addMenuItem(referencesCategory,
						new JMenu(TextUtils.getText(REFERENCE_MANAGEMENT_MENU_LANG_KEY)), referencesCategory
								+ REFERENCE_MANAGEMENT_MENU, MenuBuilder.AS_CHILD);
				builder.addSeparator(referencesCategory + REFERENCE_MANAGEMENT_MENU, MenuBuilder.AFTER);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, new ShowInReferenceManagerAction(), MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, CopyBibtex, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, CopyCiteKey, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, AddNewReference, MenuBuilder.AS_CHILD);
//				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, new ImportMetadateForNodeLink(), MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, AddExistingReference, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU, RemoveReference, MenuBuilder.AS_CHILD);
				
				
				JMenu updRefMenu = new JMenu(TextUtils.getText(UPDATE_REFERENCES_MENU_LANG_KEY));
				updRefMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
					
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						UpdateReferencesInLibrary.setEnabled();
						UpdateReferencesAllMaps.setEnabled();
					}
					
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
					
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {}
				});
				builder.addMenuItem(referencesCategory + REFERENCE_MANAGEMENT_MENU,
						updRefMenu, referencesCategory
								+ REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesCurrentMap, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesAllOpenMaps, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesInLibrary, MenuBuilder.AS_CHILD);
				builder.addAction(referencesCategory + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU,
						UpdateReferencesAllMaps, MenuBuilder.AS_CHILD);
//				builder.addAction(parentMenu + REFERENCE_MANAGEMENT_MENU + UPDATE_REFERENCES_MENU, 
//						ConvertSplmmReferences,	MenuBuilder.AS_CHILD);
				
//				builder.addAction(MENU_BAR + TOOLS_MENU, ShowJabrefPreferences, MenuBuilder.AS_CHILD);

			}
		});
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
	
	private class KeyBindInterceptor implements IKeyStrokeInterceptor {
		
		public boolean interceptKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
			Object source = e.getSource();
			if(hasPackageNameOrAncestor(source, "net.sf.jabref")) {
				if(jabrefWrapper.getJabrefFrame().getMenuBar().processKeyBinding(ks, e, condition, pressed)) {
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
}
