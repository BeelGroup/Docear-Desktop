package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class ChangeBibtexDatabaseAction extends AWorkspaceAction {
   
   public static final String KEY = "ChangeBibtexDatabaseAction";
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ChangeBibtexDatabaseAction() {
       super(KEY);
   }

   public void actionPerformed(ActionEvent arg0) {
       if (WorkspaceController.getCurrentProject() instanceof DocearWorkspaceProject) {
           DocearWorkspaceProject project = (DocearWorkspaceProject) WorkspaceController.getCurrentProject();
           JFileChooser fileChooser = new JFileChooser(URIUtils.getAbsoluteFile(project.getBibtexDatabase()));
           fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
           fileChooser.setMultiSelectionEnabled(false);
           fileChooser.setFileHidingEnabled(true);
           fileChooser.addChoosableFileFilter(new FileFilter() {
               public String getDescription() {
                   return "*.bib (" + TextUtils.getText("locationdialog.filefilter.bib") + ")";
               }

               public boolean accept(File f) {
                   return (f.isDirectory() || f.getName().endsWith(".bib"));
               }
           });
           int result = fileChooser.showOpenDialog(UITools.getFrame());
           if (result == JFileChooser.APPROVE_OPTION) {
               URI uri = project.getRelativeURI(fileChooser.getSelectedFile().toURI()); 
               project.changeBibtexURI(uri);
           }
       }
   }

   @Override
   public void setEnabled() {
       setEnabled(WorkspaceController.getCurrentProject() != null);
   }

}

