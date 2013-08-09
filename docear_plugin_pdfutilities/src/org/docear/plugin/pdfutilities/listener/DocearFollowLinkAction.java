package org.docear.plugin.pdfutilities.listener;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.FollowLinkAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;

public class DocearFollowLinkAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static String KEY="FollowLinkAction";
	
	public DocearFollowLinkAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		
		if (nodes == null || nodes.size() == 0) {
			return;
		}
		
		for (NodeModel node : nodes) {
			URI uri = URIUtils.getAbsoluteURI(node);
			if(uri == null) { 
				continue;
			}
			
			IAnnotation annotation = null;
			try {
				annotation = node.getExtension(AnnotationModel.class);
			}
			catch(Exception ex) {				
			}
			
			boolean openOnPage = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
			if (openOnPage) {
				PdfUtilitiesController.getController().openPdfOnPage(uri, annotation);				
			}
			else {
				new FollowLinkAction().actionPerformed(e);
			}
			
		}
	}

}
