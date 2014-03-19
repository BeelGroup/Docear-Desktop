package org.docear.plugin.pdfutilities.listener;

import java.io.File;
import java.net.URI;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.nodes.ALinkNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;

public class WorkspaceNodeOpenDocumentListener implements IWorkspaceNodeActionListener {
	
	public void handleAction(WorkspaceActionEvent event) {
		
		URI uri = null;
		if(event.getSource() instanceof DefaultFileNode) {
			uri = ((DefaultFileNode) event.getSource()).getFile().toURI();
		}
		else if(event.getSource() instanceof ALinkNode) {
			uri = ((ALinkNode) event.getSource()).getLinkURI();
		}

		if(uri == null || !CoreUtils.resolveURI(uri).getName().toLowerCase().endsWith(".pdf")) {
			return;
		}
		File file = CoreUtils.resolveURI(uri);
		if(file!=null  && !file.exists()) {
			//WORKSPACE - todo: implement workspace view utils, e.g. with different types of default dialogs
			//WorkspaceUtils.showFileNotFoundMessage(file);
			event.consume();
			return;
		}
		
		
		boolean openOnPage = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
		
		if (openOnPage) {
			PdfUtilitiesController.getController().openPdfOnPage(uri, 1);
			event.consume();
		}
	}
}
