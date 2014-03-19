package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.search.SearchMatcher;

import org.docear.plugin.bibtex.JabRefProjectExtension;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class SelectInReferenceManagerAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "SelectInRefManagerAction";
	
	
	public SelectInReferenceManagerAction() {
		super(KEY);
	}
	
	public void setEnabled() {
		Collection<NodeModel> selection = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (NodeModel node : selection) {
			AWorkspaceProject project = WorkspaceController.getMapProject(node.getMap());
			if(project != null && project.isLoaded()) {
				final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
				if (bibtexKey != null && bibtexKey.length()>0) {
					setEnabled(true);
					return;
				}
			}
		}
		setEnabled(false);
		
//		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
//		if (node == null) {
//			setEnabled(false);
//			return;
//		}
//		AWorkspaceProject project = WorkspaceController.getMapProject(node.getMap());
//		if(project == null || !project.isLoaded()) {
//			setEnabled(false);
//		}
//		else {
//			final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
//			
//			if (bibtexKey != null && bibtexKey.length()>0) {
//				setEnabled(true);
//			}
//			else {
//				setEnabled(false);
//			}
//		}
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Collection<NodeModel> selection = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		BasePanel panel = ReferencesController.getController().getJabrefWrapper().getBasePanel();
		panel.getMainTable().clearSelection();
		if(panel != null) {
			List<BibtexEntry> entries = new ArrayList<BibtexEntry>();
			for (NodeModel node : selection) {
				BibtexEntry entry = selectReference(node, true); 
				if(entry != null) {
					entries.add(entry);
				}
			}
		}
		//filterEntries(entries);
		//NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
	}

	public void filterEntries(Collection<BibtexEntry> entries) {
		
		BasePanel panel = ReferencesController.getController().getJabrefWrapper().getBasePanel();
		if(panel != null) {
			Thread t = new Thread() {
				public void run() {
					JabRefCommons.clearSearchFilter();
				}
			};
			// do this after the button action is over
			SwingUtilities.invokeLater(t);
			panel.getMainTable().clearSelection();
			List<BibtexEntry> list = panel.getMainTable().getTableRows();
			for (BibtexEntry entry : list) {
				entry.setSearchHit(entries.contains(entry));
			}
			//panel.setSearchMatcher(SearchMatcher.INSTANCE);
			panel.mainTable.showFloatSearch(SearchMatcher.INSTANCE);
		}
	}
	
	public BibtexEntry selectReference(NodeModel node, boolean keepPreviousSelection) {
		if (node == null) {
			return null;
		}
		BibtexEntry entry = null;
		WorkspaceMapModelExtension modelExt = WorkspaceController.getMapModelExtension(node.getMap(), false);
		if(modelExt != null) {
			AWorkspaceProject project = modelExt.getProject();
			JabRefProjectExtension ext = (JabRefProjectExtension) project.getExtensions(JabRefProjectExtension.class);
			ext.selectBasePanel();
		}
		//if(ReferencesController.getController().getJabrefWrapper().getBasePanel().getSelectedEntries().length <= 1) {
			final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
			JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
			OneTouchCollapseResizer resizer = wrapper.getResizer();
			if(resizer != null && !resizer.isExpanded()) {
				resizer.setExpanded(true);
			}
			entry = JabRefCommons.showInReferenceManager(bibtexKey, keepPreviousSelection);		
		//}
		return entry;
	}
	
	

}
