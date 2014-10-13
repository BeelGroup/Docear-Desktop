package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.DuplicateResolver;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.core.util.NodeUtilities;
import org.docear.plugin.pdfutilities.listener.DocearProjectModelListener;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class RenameByMetaData extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String KEY = "workspace.action.renameByMetaData";
	
	final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
	static {
	    Arrays.sort(illegalChars);
	}
	
	private BibtexEntry currentEntry = null;
	
	public RenameByMetaData(){
		super(KEY);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(currentEntry == null) return;
		String newName = cleanFileName(currentEntry.getField("year") + "--" + currentEntry.getField("title") + ".pdf".replaceAll("[^\\x00-\\x7F]", ""));		
		AWorkspaceTreeNode targetNode = this.getNodeFromActionEvent(e);
		if(targetNode != null){
			String oldName = targetNode.getName();
			if (targetNode instanceof IMutableLinkNode) {
				if (((IMutableLinkNode) targetNode).changeName(newName, true)) {
					targetNode.refresh();
				} 
				else {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file"), 
							TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);
					targetNode.setName(oldName);
				}
			}
		}
		else{
			NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
			AWorkspaceProject project = WorkspaceController.getMapProject(node.getMap());
			File oldFile = CoreUtils.resolveURI(NodeUtilities.getLink(node)).getAbsoluteFile();
			File newFile = new File(oldFile.getParentFile(), newName);
			if(oldFile.renameTo(newFile)) {
				refreshWorkspaceNodes(project.getModel().getRoot(), oldFile, newFile);				
				AnnotationController.getController().updateIndex(newFile, oldFile);
				DocearProjectModelListener.updateMaps(project, newFile, oldFile, true);
			}
			else {
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("error_rename_file"), 
						TextUtils.getText("error_rename_file_title"), JOptionPane.ERROR_MESSAGE);				
			}
		}
		System.out.println();
	}
	
	private void refreshWorkspaceNodes(AWorkspaceTreeNode root, File oldFile, File newFile) {
		Enumeration<AWorkspaceTreeNode> children = root.children();
		while(children != null && children.hasMoreElements()){
			AWorkspaceTreeNode child = children.nextElement();
			File file = null;
			if(child instanceof IFileSystemRepresentation) {
				file = ((IFileSystemRepresentation) child).getFile();
			}
			else if(child instanceof LinkTypeFileNode) {				
				file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) child).getLinkURI());				
			}
			try {
				if(file != null && file.getCanonicalPath().equals(oldFile.getCanonicalPath())){
					((IMutableLinkNode) child).changeName(newFile.getName(), false);					
					child.refresh();
				}
			} catch (IOException e) {
				LogUtils.warn(e.getMessage());
			}
			refreshWorkspaceNodes(child, oldFile, newFile);
		}		
	}

	public void setEnabledFor(AWorkspaceTreeNode node, TreePath[] selectedPaths) {
		File file = null;
		if(node instanceof IFileSystemRepresentation) {
			file = ((IFileSystemRepresentation) node).getFile();
		}
		else {
			if(node instanceof LinkTypeFileNode) {				
				file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
			}
		}
		
		if(file == null || !file.getName().toLowerCase().endsWith(".pdf") || !hasMetaData(file)) {
			setEnabled(false);
			return;
		}	
		else{
			setEnabled(true);
			return;
		}			
	}
	
	public void setEnabled() {		
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
			
		if (MonitoringUtils.isPdfLinkedNode(node)) {
			File file = CoreUtils.resolveURI(NodeUtilities.getLink(node)).getAbsoluteFile();
			setEnabled(hasMetaData(file));			
		} else {
			setEnabled(false);
		}

	}

	private boolean hasMetaData(File file) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		if(database == null) return false;
		for(BibtexEntry entry : database.getEntries()){
			URL entryUrl = null;
			String urlString = entry.getField("url");
			try {
				if (urlString != null) {
					entryUrl = new URL(urlString);
				}
				if (file.toURI().toURL().equals(entryUrl)) {
					if(hasRequiredFields(entry)){
						currentEntry = entry;
						return true;
					}
					return false;
				}
			}
			catch (MalformedURLException e) {
				LogUtils.info(urlString + ": " + e.getMessage());
			}					
			for (String jabrefPath : DuplicateResolver.getDuplicateResolver().retrieveFileLinksFromEntry(entry)) {
				File jabrefFile = new File(jabrefPath);
				if (jabrefFile != null && jabrefFile.getName().equals(file.getName())) {
					if(hasRequiredFields(entry)){
						currentEntry = entry;
						return true;
					}
					return false;
				}
			}			
		}
		return false;
	}

	private boolean hasRequiredFields(BibtexEntry entry) {
		//if(entry.getField("author") == null || entry.getField("author").isEmpty()) return false;
		if(entry.getField("title") == null || entry.getField("title").isEmpty()) return false;
		if(entry.getField("year") == null || entry.getField("year").isEmpty()) return false;
		return true;
	}	
	
	private String cleanFileName(String badFileName) {
	    StringBuilder cleanName = new StringBuilder();
	    for (int i = 0; i < badFileName.length(); i++) {
	        int c = (int)badFileName.charAt(i);
	        if (Arrays.binarySearch(illegalChars, c) < 0) {
	            cleanName.append((char)c);
	        }
	    }
	    return cleanName.toString();
	}

}
