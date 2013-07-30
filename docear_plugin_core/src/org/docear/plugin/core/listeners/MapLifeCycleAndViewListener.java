package org.docear.plugin.core.listeners;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.actions.ChooseMapProjectAffiliationAction;
import org.docear.plugin.core.features.DocearFileBackupController;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearRequiredConversionController;
import org.docear.plugin.core.features.DocearWorkspaceLinkConverted;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.ui.CreateProjectPagePanel;
import org.docear.plugin.core.ui.MapIdsConflictsPanel;
import org.docear.plugin.core.ui.SelectProjectPagePanel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardContext;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.workspace.actions.DocearNewProjectAction;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class MapLifeCycleAndViewListener implements IMapLifeCycleListener, IMapViewChangeListener {

	public void onCreate(MapModel map) {
		if (map instanceof MMapModel) {
			List<AWorkspaceProject> projects = WorkspaceController.getCurrentModel().getProjects();
			AWorkspaceProject project = WorkspaceController.getMapProject(map);
			if (!DocearWorkspaceProject.isCompatible(project)) {
				showProjectSelectionWizard(map);
			}
			
			File f = map.getFile();
			if (f!=null) {
				showMapIdConflictingDialogIfNeeded(map);
				
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_OPENED, f);
				//WORKSPACE - todo: save welcome mind map only once for any project --> one location in workspace
				File installWelcomeMap = new File(System.getProperty("user.dir"), "doc" + File.separator + "docear-welcome.mm");
				File userdirWelcomeMap = new File(URIUtils.getFile(WorkspaceController.getApplicationHome()), "docear-welcome.mm");
				if(f.equals(installWelcomeMap) || f.equals(userdirWelcomeMap)){
					map.setReadOnly(true);
				}
			}
			else {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_NEW);
			}
			
			if(DocearRequiredConversionController.hasRequiredConversion(DocearWorkspaceLinkConverted.class, map)) {
				try {
					DocearFileBackupController.createBackupForConversion(map);
				} catch (IOException e) {
					LogUtils.warn(e);
				}
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				WorkspaceMapModelExtension mapExt = WorkspaceController.getMapModelExtension(map, false);
				if(mapExt == null || mapExt.getProject() == null) {
					ChooseMapProjectAffiliationAction.showChooser(map);
				}
				map.setSaved(false);
				mapIO.save(map);
			}
			
			setMapAttributesIfNeeded(map);
		}
	}

	private void showProjectSelectionWizard(final MapModel map) {		
		final Wizard wizard = new Wizard(UITools.getFrame());		
		initWizard(wizard, map);
		
		int ret = wizard.show();
		if(ret == Wizard.OK_OPTION) {
			DocearWorkspaceProject project = wizard.getContext().get(DocearWorkspaceProject.class);
			Boolean contextObject = wizard.getContext().get(Boolean.class);
			if (wizard.getContext().get(Boolean.class) != null) {
				if (contextObject) {
					DocearNewProjectAction.createProject(project);
				}
				else {					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							map.setSaved(true);
							Controller.getCurrentController().close(false);
						}
					});
					return;
				}
			}
			if (project != null) {
				WorkspaceController.getMapModelExtension(map).setProject(project);
				if (map.getFile() != null) {
					map.setSaved(false);
					((MFileManager) UrlManager.getController()).save(map, true);
				}				
				try {
					LogUtils.info("set project \""+project+"\" for map: \""+map.getTitle());
				}
				catch (Exception e) {					
				}
			}
			
		}
	}

	private void initWizard(Wizard wizard, MapModel map) {
		WizardPageDescriptor desc = new WizardPageDescriptor("page.project.select", new SelectProjectPagePanel(map)) {
			
    		public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {    			
    			AWorkspaceProject project = ((SelectProjectPagePanel)getPage()).getSelectedProject();
    			context.set(DocearWorkspaceProject.class, project);
    			return Wizard.FINISH_PAGE;
    		}
    		
    		public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {    			
    			return context.getModel().getPage("page.project.create");
    		}
    		
    		public WizardPageDescriptor getSkipPageDescriptor(WizardContext context) {
    			context.set(Boolean.class, false);
    			return Wizard.FINISH_PAGE;
    		}
		};		
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
//		wizard.setCancelEnabled(false);
		
		//new project page
		desc = new WizardPageDescriptor("page.project.create", new CreateProjectPagePanel()) {
			public WizardPageDescriptor getNextPageDescriptor(WizardContext context) {
				context.set(DocearWorkspaceProject.class, ((CreateProjectPagePanel) getPage()).getProject());
				context.set(Boolean.class, true);
				return Wizard.FINISH_PAGE;
			}
			
			public WizardPageDescriptor getBackPageDescriptor(WizardContext context) {
				return context.getModel().getPage("page.project.select");
			}
			
			@Override
			public void aboutToDisplayPage(WizardContext context) {
				super.aboutToDisplayPage(context);
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

			@Override
			public void displayingPage(WizardContext context) {
				super.displayingPage(context);
				context.setWizardTitle(TextUtils.getText("workspace.action.node.new.project.dialog.title"));
				context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
			}

		};
		desc.getPage().setPreferredSize(new Dimension(640, 450));
		wizard.registerWizardPanel(desc);
			
	}

	public void onRemove(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f != null && f.isFile()) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_CLOSED, f);
				touchFileForAutoSaveBug(f);
			}
			
		}
	}

	private void touchFileForAutoSaveBug(File f) {
		try {
			FileUtils.touch(f);
		} catch (IOException e) {
			//LogUtils.warn(e);
		}		
	}

	public void onSavedAs(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_SAVED, f);
			}
		}
	}

	public void onSaved(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_SAVED, f);				
			}
		}
	}
		
	public void afterViewChange(Component oldView, Component newView) {		
	}

	public void afterViewClose(Component oldView) {
	}

	public void afterViewCreated(Component mapView) {
		MapModel map = Controller.getCurrentController().getMapViewManager().getModel(mapView);
		setMapAttributesIfNeeded(map);		
	}

	public void beforeViewChange(Component oldView, Component newView) {
	}

	private void setMapAttributesIfNeeded(MapModel map) {
		if(map == null) {
			return;
		}
		
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);		
		if (dmme == null) {
			dmme = DocearMapModelController.setModelWithCurrentVersion(map);			
		}
		else if (dmme.getMapId() == null || dmme.getMapId().trim().length()==0) {
			dmme.setMapId(DocearMapModelController.createMapId());
		}
		
		//WORKSPACE: implement in DocearProjectLoader
//		//DOCEAR - hack to prevent old trash maps from not having the type "trash"
//		File f = map.getFile();
//		File libraryPath = WorkspaceController.resolveFile(DocearController.getController().getLibraryPath());
//		if (f != null) {
//			if ("trash.mm".equals(f.getName())) {				
//				if (f.getAbsolutePath().startsWith(libraryPath.getAbsolutePath())) {
//					dmme.setType(DocearMapType.trash);
//					DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
//					if(ext == null) {
//						DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
//					}
//				}
//			}
//			else if ("temp.mm".equals(f.getName())) {				
//				if (f.getAbsolutePath().startsWith(libraryPath.getAbsolutePath())) {
//					dmme.setType(DocearMapType.temp);
//					DocearNodePrivacyExtension ext = DocearNodePrivacyExtensionController.getExtension(map.getRootNode());
//					if(ext == null) {
//						DocearNodePrivacyExtensionController.getController().setPrivacyLevel(map.getRootNode(), DocearPrivacyLevel.DEMO);
//					}
//				}
//			}
//		}
		
	}
	
	private void showMapIdConflictingDialogIfNeeded(MapModel map) {
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);		
		if (dmme == null) {
			return;
		}
		
		File pathInXml = UrlManager.getController().getAbsoluteFile(map, dmme.getUri());
		File physicalPath = map.getFile();
		
		if (pathInXml == null || physicalPath==null) {
			return;
		}
		
		if (!pathInXml.equals(physicalPath)) {
			if (pathInXml.exists()) {
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);				
				try {
					MMapModel otherMap = new MMapModel();
					mapIO.load(Compat.fileToUrl(pathInXml), otherMap);
					
					DocearMapModelExtension otherDmme = otherMap.getExtension(DocearMapModelExtension.class);
					if (dmme.getMapId().equals(otherDmme.getMapId())) {
						MapIdsConflictsPanel panel = new MapIdsConflictsPanel(physicalPath, pathInXml);
						JOptionPane.showMessageDialog(UITools.getFrame(), panel, TextUtils.getText("docear.conflicting_map_ids.title"), JOptionPane.QUESTION_MESSAGE);
						if (panel.isThisMapNew()) {							
							dmme.setMapId(DocearMapModelController.createMapId());
							map.setSaved(false);
							mapIO.save(map);
						}
						else if (panel.isOtherMapNew()) {							
							otherDmme.setMapId(DocearMapModelController.createMapId());
							otherMap.setSaved(false);
							mapIO.save(otherMap);							
						}
						else {
							map.setSaved(false);
							mapIO.save(map);
						}
					}
				} 
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		}
	}
	


}
