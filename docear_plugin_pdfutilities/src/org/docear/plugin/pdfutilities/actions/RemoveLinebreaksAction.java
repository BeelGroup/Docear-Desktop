package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.DocumentReadOnlyException;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.ReadOnlyExceptionWarningHandler;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;

import de.intarsys.pdf.cos.COSRuntimeException;

@EnabledAction(checkOnNodeChange = true )
public class RemoveLinebreaksAction extends ImportAnnotationsAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String KEY = "RemoveLinebreaksAction";

	@SuppressWarnings("serial")
	public RemoveLinebreaksAction() {
		super(KEY);
		this.setEnableType(new ArrayList<AnnotationType>(){{ add(AnnotationType.BOOKMARK); 
															 add(AnnotationType.BOOKMARK_WITH_URI);
															 add(AnnotationType.BOOKMARK_WITHOUT_DESTINATION);
															 add(AnnotationType.COMMENT);
															 add(AnnotationType.HIGHLIGHTED_TEXT);
														   }});
	}

	public void actionPerformed(ActionEvent evt) {
		Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
		if(selection == null){
			return;
		}
		
		else{
			for(NodeModel selected : selection){
				if(selected == null){
					continue;
				}
				else{
					removeLinebreaks(selected);	
				}
			}					
		}
	}

	private void removeLinebreaks(NodeModel selected) {
		String text = PdfAnnotationImporter.removeLinebreaks(selected.getText(), true);
		if(text.equals(selected.getText())) return;
		AnnotationNodeModel model = AnnotationController.getAnnotationNodeModel(selected);
		if(model != null){
			NodeChangeEvent event = new NodeChangeEvent(selected, NodeModel.NODE_TEXT, selected.getText(), text);
			try {
				ReadOnlyExceptionWarningHandler warningHandler = new ReadOnlyExceptionWarningHandler();
				warningHandler.prepare();
				while(warningHandler.retry()) {
					try {
						if(new PdfAnnotationImporter().renameAnnotation(model, text)){					
							selected.setText(text);
							selected.fireNodeChanged(event);
						}
		                warningHandler.consume();
					} catch (DocumentReadOnlyException e) {
						if(warningHandler.skip()) {
							break;
						}					
						warningHandler.showDialog(URIUtils.getFile(model.getSource()));
					}
						
				}
			} catch (IOException e) {
				if(e.getMessage().equals("destination is read only")){ //$NON-NLS-1$
					Object[] options = { TextUtils.getText("DocearRenameAnnotationListener.1"), TextUtils.getText("DocearRenameAnnotationListener.2"),TextUtils.getText("DocearRenameAnnotationListener.3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					int result = JOptionPane.showOptionDialog(Controller.getCurrentController().getMapViewManager().getComponent(selected), TextUtils.getText("DocearRenameAnnotationListener.4"), TextUtils.getText("DocearRenameAnnotationListener.5"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); //$NON-NLS-1$ //$NON-NLS-2$
					if( result == JOptionPane.OK_OPTION){
						removeLinebreaks(selected);			
					}
					else if( result == JOptionPane.CANCEL_OPTION ){						
						selected.setText("" + event.getOldValue());							 //$NON-NLS-1$
					}
					else if( result == JOptionPane.NO_OPTION ){						
						selected.setText(text);
						selected.fireNodeChanged(event);
					}
				}
				else{
					LogUtils.severe("RemoveLinebreaksAction IOException at Target("+selected.getText()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} 
			catch (COSRuntimeException e) {
				LogUtils.severe("RemoveLinebreaksAction COSRuntimeException at Target("+selected.getText()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}	

}
