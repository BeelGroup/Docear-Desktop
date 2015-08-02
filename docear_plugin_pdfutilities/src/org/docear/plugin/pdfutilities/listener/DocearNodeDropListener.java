package org.docear.plugin.pdfutilities.listener;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.MapModificationSession;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.pdf.DocumentReadOnlyException;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.pdf.ReadOnlyExceptionWarningHandler;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.MindMapNodesSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.mindmapmode.MNodeDropListener;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;


public class DocearNodeDropListener extends MNodeDropListener {
	
	public DocearNodeDropListener(){
		super();
	}	
	
	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		LogUtils.info("DocearNodedroplistener Drop activated...."); //$NON-NLS-1$
					
		final MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
		final NodeView targetNodeView = mainView.getNodeView();
		
		Set<NodeModel> nodes = new HashSet<NodeModel>();
		for (NodeModel node : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
			nodes.add(node);
		}
		
		NodeModel node = targetNodeView.getModel();		
		if (!nodes.contains(node)) {
			nodes.clear();
			nodes.add(node);
		}
		DocearMapModelExtension modelExtension = node.getMap().getExtension(DocearMapModelExtension.class);
		try{
			MapModificationSession session = new MapModificationSession();	
    		modelExtension.setMapModificationSession(session);

    		final DataFlavor fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List"); //$NON-NLS-1$
			final DataFlavor uriListFlavor = new DataFlavor("text/uri-list; class=java.lang.String"); //$NON-NLS-1$
			
			if(dtde.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor) ) {
				super.drop(dtde);
				return;
			}
			// do not combine with the previous condition unless you know what you are doing! 
			if (dtde.isDataFlavorSupported(fileListFlavor) || (dtde.isDataFlavorSupported(uriListFlavor))) {
	            				
	            
	            final Transferable transferable = dtde.getTransferable();
	            final boolean isLeft = mainView.dropLeft(dtde.getLocation().getX());
	            mainView.setDraggedOver(NodeView.DRAGGED_OVER_NO);
	            mainView.repaint();
	            
	            List<File> fileList = new ArrayList<File>();
	            if(transferable.isDataFlavorSupported(fileListFlavor)){
	    			dtde.acceptDrop(dtde.getDropAction());
	    		    fileList = (List<File>) (transferable.getTransferData(fileListFlavor));
	    		}
	    		else if(transferable.isDataFlavorSupported(uriListFlavor)){
	    			dtde.acceptDrop(dtde.getDropAction());
	    		    fileList = textURIListToFileList((String) transferable.getTransferData(uriListFlavor));
	    		}
	            
	            Iterator<NodeModel> iter = nodes.iterator();
            	while (iter.hasNext()) {            		
            		pasteFileList(fileList, iter.next(), isLeft);            		
            	}
	            
	            dtde.dropComplete(true);
	            return;		
	        }
		 } catch (final Exception e) {			 
			LogUtils.severe("DocearNodeDropListener Drop exception:", e); //$NON-NLS-1$
			dtde.dropComplete(false);
			return;
		 }
		finally {		
			modelExtension.resetModificationSession();
		}		
		super.drop(dtde);
	}
	
	public static List<File> textURIListToFileList(String data) {
	    List<File> list = new ArrayList<File>();
	    StringTokenizer stringTokenizer = new StringTokenizer(data, "\r\n");
	    while(stringTokenizer.hasMoreTokens()) {
	    	String string = stringTokenizer.nextToken();
	    	// the line is a comment (as per the RFC 2483)
	    	if (string.startsWith("#")) continue;
		    		    
			try {
				URI uri = new URI(string);
				File file = new File(uri);
			    list.add(file);
			} catch (URISyntaxException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an URISyntaxException occured. URI: " + string);
			} catch (IllegalArgumentException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an IllegalArgumentException occured. URI: " + string);
		    }	    
	    }	     
	    return list;
	}

	public static void pasteFileList(final List<File> fileList, final NodeModel targetNode, final boolean isLeft)
			throws UnsupportedFlavorException, IOException, ClassNotFoundException, Exception {	
		
		
		SwingWorker<Void, Void> thread = new SwingWorker<Void, Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				int count = 0;
				firePropertyChange(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				MapModificationSession session = targetNode.getMap().getExtension(DocearMapModelExtension.class).getMapModificationSession();
				if (session == null) {
					session = new MapModificationSession();
					targetNode.getMap().getExtension(DocearMapModelExtension.class).setMapModificationSession(session);
				}
				session.putSessionObject(MapModificationSession.FILE_IGNORE_LIST , new HashSet<String>());
				ReadOnlyExceptionWarningHandler warningHandler = new ReadOnlyExceptionWarningHandler();
				for(final File file : fileList){
					warningHandler.prepare();
					if(Thread.currentThread().isInterrupted()) return null;
					firePropertyChange(SwingWorkerDialog.NEW_FILE, null, file.getName());
		        	boolean importAnnotations = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.AUTO_IMPORT_ANNOTATIONS_KEY);
		            if(new PdfFileFilter().accept(file) && importAnnotations){	
		            	List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		            	try{
		            		while(warningHandler.retry()) {
		    					try {		    						
				            		PdfAnnotationImporter importer = new PdfAnnotationImporter();				            		
				            		annotations = importer.importAnnotations(file.toURI());				            		
					                warningHandler.consume();
		    					} catch (DocumentReadOnlyException e) {		    						
		    						if(warningHandler.skip()) {
		    							annotations = null;
		    							break;
		    						}					
		    						warningHandler.showDialog(file);
		    					}
		    				}
		            		//System.gc();
		            	} catch(COSRuntimeException e) {			                		
		            		LogUtils.warn("Exception during import on file: " + file.getName(), e); //$NON-NLS-1$
		            	} catch(IOException e) {
		            		LogUtils.warn("Exception during import on file: " + file.getName(), e); //$NON-NLS-1$
		            	} 
		            	
		            	if(annotations != null){
		            		final List<AnnotationModel> finalAnnotations = annotations;
		            		SwingUtilities.invokeAndWait(
							        new Runnable() {
							            public void run(){
							            	try {
								            	URI uri = file.toURI();
								            	NodeModel newNode = MonitoringUtils.insertChildNodesFromPdf(uri, finalAnnotations, isLeft, targetNode);	            
								            	for(AnnotationModel annotation : getInsertedNodes(finalAnnotations)){
													firePropertyChange(SwingWorkerDialog.DETAILS_LOG_TEXT, null, TextUtils.getText("DocearNodeDropListener.4") + annotation.getTitle() +TextUtils.getText("DocearNodeDropListener.5"));												 //$NON-NLS-1$ //$NON-NLS-2$
												}	
								            	
								            	DocearEvent event = new DocearEvent(newNode, (DocearWorkspaceProject) WorkspaceController.getMapProject(newNode.getMap()), DocearEventType.MINDMAP_ADD_PDF_TO_NODE, true);
								            	DocearController.getController().getEventQueue().dispatchEvent(event);
							            	}
							            	catch (Exception e) {
							            		LogUtils.severe(e);
							            	}
							            }
							        }
							   );
		            		try {
		            			Thread.sleep(150);
							} catch (Exception e) {
							}
		            		
		            	}
		            	
		            }
		            else {		            	
		    			ModeController modeController = Controller.getCurrentController().getModeController();
		    			final ViewerController viewerController = ((ViewerController)modeController.getExtension(ViewerController.class));
		    			SwingUtilities.invokeAndWait(
						        new Runnable() {
						            public void run(){
						            	if(!viewerController.paste(file, targetNode, isLeft)){							        				
						            		MonitoringUtils.insertChildNodeFrom(file.toURI(), isLeft, targetNode, null);
					        			}							
						            }
						        }
						   );		        			
		            }
		            count++;
					setProgress(100 * count / fileList.size());
					Thread.sleep(1L);
		        }
				return null;
			}
			
			@Override
		    protected void done() {
				firePropertyChange(SwingWorkerDialog.IS_DONE, null, null);
			}
			
			private Collection<AnnotationModel> getInsertedNodes(Collection<AnnotationModel> annotations){
				Collection<AnnotationModel> result = new ArrayList<AnnotationModel>();
				for(AnnotationModel annotation : annotations){
					result.add(annotation);
					result.addAll(this.getInsertedNodes(annotation.getChildren()));							
				}
				return result;
			}
			
		};
		
		/*if(fileList.size() > 10){
			SwingWorkerDialog monitoringDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			monitoringDialog.showDialog(thread);
		}
		else{*/
			thread.execute();
		//}
	}
	
	/*public boolean isDragAcceptable(final DropTargetDragEvent ev) {
		if(ev.isDataFlavorSupported(TransferableEntrySelection.flavorInternal)){
			return true;
		}
		return super.isDragAcceptable(ev);
		
	}*/
	

}
