package org.docear.plugin.pdfutilities.pdf;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.docear.addons.highlights.IHighlightsImporter;
import org.docear.pdf.annotation.AnnotationExtractor;
import org.docear.pdf.annotation.CommentAnnotation;
import org.docear.pdf.annotation.HighlightAnnotation;
import org.docear.pdf.bookmark.Bookmark;
import org.docear.pdf.bookmark.BookmarkExtractor;
import org.docear.pdf.feature.ADocumentCreator;
import org.docear.pdf.feature.AObjectType;
import org.docear.pdf.feature.APDMetaObject;
import org.docear.pdf.feature.PageDestination;
import org.docear.pdf.feature.UriDestination;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.util.HtmlUtils;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.addons.DocearAddonController;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.map.IAnnotationImporter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;

import com.google.common.base.CharMatcher;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.cos.COSArray;
import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.pd.PDAnnotation;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDOutlineItem;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDTextMarkupAnnotation;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;

public class PdfAnnotationImporter implements IAnnotationImporter {
	
	private URI currentFile;
	private boolean importAll = false;
	private boolean setPDObject = false;
	private int removeLinebreaksDialogResult = JOptionPane.OK_OPTION;
	private boolean modifiedDocument;
	
	public PdfAnnotationImporter(){
	}
	
	public Map<URI, List<AnnotationModel>> importAnnotations(List<URI> files) throws IOException, DocumentReadOnlyException {
		Map<URI, List<AnnotationModel>> annotationMap = new HashMap<URI, List<AnnotationModel>>();
		
		for(URI file : files){
			annotationMap.put(file, this.importAnnotations(file));
		}
		
		return annotationMap;
	}	
	
	public List<AnnotationModel> importAnnotations(URI uri) throws IOException, DocumentReadOnlyException {
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();		
		this.currentFile = uri;
		PDDocument document = getPDDocument(uri);
		if(document == null){				
			return annotations;
		}
		this.modifiedDocument = false;
		try{			
			this.importAnnotations(document, annotations);			
			this.importBookmarks(document, annotations);			
		} catch(Exception e){			
			LogUtils.warn(e);
			return annotations;
		} finally {			
			if(document != null){
				if(this.modifiedDocument && !document.isReadOnly()) {
					document.save();
				}
				document.close();
				document = null;
			}
		}		
		return annotations;
	}
	
	public AnnotationModel importPdf(URI uri) throws IOException, DocumentReadOnlyException {
		Collection<AnnotationModel> importedAnnotations = new ArrayList<AnnotationModel>();
		try{
			importedAnnotations = importAnnotations(uri);
		} catch(RuntimeException e){
			LogUtils.info("IOexception during update file: "+ uri); //$NON-NLS-1$
			LogUtils.warn(e);
		}
		URI absoluteUri = URIUtils.getAbsoluteURI(uri);
		AnnotationModel root = new AnnotationModel(0, AnnotationType.PDF_FILE);
		root.setSource(absoluteUri);
		root.setTitle(URIUtils.getFile(absoluteUri).getName());
		root.getChildren().addAll(importedAnnotations);	
		return root;
	}
	
	public boolean renameAnnotation(IAnnotation annotation, String newTitle) throws IOException, DocumentReadOnlyException {
		if(newTitle.startsWith("<HTML>") || newTitle.startsWith("<html>")){
			newTitle = HtmlUtils.extractText(newTitle);
		}
		List<AnnotationModel> annotations = new ArrayList<AnnotationModel>();
		boolean ret = false;
		this.currentFile = annotation.getSource();
		PDDocument document = getPDDocument(annotation.getSource());
		if(document != null){
			try{
				this.setImportAll(true);
				this.setPDObject(true);
				
				this.importAnnotations(document, annotations);
				this.importBookmarks(document, annotations);
				
			} catch(Exception e){
				LogUtils.warn(e);
			} finally {
				this.setImportAll(true);
				this.setPDObject(false);
				AnnotationModel result = this.searchAnnotation(annotations, annotation);
				if(result != null){
					Object annotationObject = result.getAnnotationObject();
					if(annotationObject != null && annotationObject instanceof PDOutlineItem){
						((PDOutlineItem)annotationObject).setTitle(newTitle);
						ret = true;
					}
					if(annotationObject != null && annotationObject instanceof PDAnnotation){
						((PDAnnotation)annotationObject).setContents(newTitle);
						ret = true;
					}
					document.save();
				}
				if(document != null) {
					document.close();
				}
			}
		}
		return ret;
	}

	private void setPDObject(boolean b) {
		setPDObject = b;		
	}
	
	private boolean setPDObject() {
		return setPDObject;		
	}

	public PDDocument getPDDocument(URI uri) throws IOException, DocumentReadOnlyException {
		MapModel map = Controller.getCurrentController().getMap();
		URI absoluteUri = URIUtils.resolveURI(URIUtils.getAbsoluteURI(map), uri);
		File file = URIUtils.getFile(absoluteUri);
		if(uri == null || file == null || !file.exists() || !PdfFileFilter.accept(uri)){
			return null;
		}		
		
		PDDocument document = ADocumentCreator.getPDDocument(file);
		
		if(document != null && document.isReadOnly()) {
			document.close();
			document = null;
			throw new DocumentReadOnlyException();
		}
			
		return document;
		
	}
	
	private void importAnnotations(PDDocument document, List<AnnotationModel> annotations) throws IOException {
		boolean importComments = false;
		boolean importHighlightedTexts = false;		
		boolean importPopUpHighlighted = false;
		boolean importPopUpOnly = false;
		
		if(this.importAll){
			importComments = true;
			importHighlightedTexts = true;
		}
		else{
			importComments = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.IMPORT_COMMENTS_KEY);
			importHighlightedTexts = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.IMPORT_HIGHLIGHTED_TEXTS_KEY);
		}		
		importPopUpHighlighted = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.IMPORT_POP_UP_HIGHLIGHTED_KEY);
		importPopUpOnly = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.IMPORT_ONLY_POP_UP_KEY); 
		if(DocearAddonController.getController().hasPlugin(IHighlightsImporter.class) && !importPopUpOnly){
			IHighlightsImporter highlightsImporter = DocearAddonController.getController().getAddon(IHighlightsImporter.class);
			try{				
				List<APDMetaObject> metaObjects = highlightsImporter.getMetaObjects(document, importComments, importHighlightedTexts, importPopUpHighlighted);
				this.modifiedDocument = highlightsImporter.isDocumentModified() || this.modifiedDocument;
				for (APDMetaObject meta : metaObjects) {
					AnnotationModel annotation = new AnnotationModel(meta.getUID());
					transferMetaObject(document, meta, annotation);
					annotations.add(annotation);
				}
			}
			finally{
				highlightsImporter.resetAll();
			}
			
		}
		else{
			AnnotationExtractor extractor = new AnnotationExtractor(document);
			try {
				extractor.setIgnoreComments(!importComments);
				extractor.setIgnoreHighlights(!importHighlightedTexts);
				
				List<APDMetaObject> metaObjects = extractor.getMetaObjects();
				this.modifiedDocument = extractor.isDocumentModified() || this.modifiedDocument;
				for (APDMetaObject meta : metaObjects) {
					AnnotationModel annotation = new AnnotationModel(meta.getUID());
					transferMetaObject(document, meta, annotation);
					annotations.add(annotation);
				}
			}
			finally {
				extractor.resetAll();
			}
		}		
	}
	
	private void importBookmarks(PDDocument document, List<AnnotationModel> annotations) throws IOException {
		if(document == null || (!this.importAll && !DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.IMPORT_BOOKMARKS_KEY))){
			return;
		}
		
		BookmarkExtractor extractor = new BookmarkExtractor(document);
		try {
			List<APDMetaObject> metaObjects = extractor.getMetaObjects();
			this.modifiedDocument = extractor.isDocumentModified() || this.modifiedDocument;
			importBookmarksRecursive(document, annotations, metaObjects);
		}
		finally {
			extractor.resetAll();
		}
		
	}

	private void importBookmarksRecursive(PDDocument document, List<AnnotationModel> annotations, List<APDMetaObject> metaObjects) {
		for (APDMetaObject meta : metaObjects) {
			AnnotationModel annotation = new AnnotationModel(meta.getUID());
			transferMetaObject(document, meta, annotation);
			annotations.add(annotation);
		}
	}
	
	
	
	private void transferMetaObject(PDDocument document, APDMetaObject meta, AnnotationModel annotation) {
		
		annotation.setOldObjectNumber(meta.getObjectNumber());
		annotation.setIsNewID(meta.getContext().isNewID());
		annotation.setSource(this.currentFile);
		annotation.setTitle(meta.getText());
		annotation.setAnnotationType(getAnnotationTypeFromMeta(meta.getType()));
		
		if(meta.getDestination() != null) {
			if(meta.getDestination() instanceof PageDestination) {
				annotation.setPage(((PageDestination)meta.getDestination()).getPage());
			}
			else if(meta.getDestination() instanceof UriDestination) {
				annotation.setDestinationUri(((UriDestination)meta.getDestination()).getUri());
			}
		}
		removeLinebreaks(annotation, meta.getObjectReference(), document);
		if(this.setPDObject()){
			annotation.setAnnotationObject(meta.getObjectReference());
		}
		
		if(meta.hasChildren()) {
			for (APDMetaObject metaChild : meta.getChildren()) {
				AnnotationModel childAnnotation = new AnnotationModel(metaChild.getUID());
				transferMetaObject(document, metaChild, childAnnotation);
				annotation.getChildren().add(childAnnotation);
			}
		}
		
	}


	private AnnotationType getAnnotationTypeFromMeta(AObjectType metaType) {
		if(Bookmark.BOOKMARK.equals(metaType)) {
			return AnnotationType.BOOKMARK;
		}
		if(Bookmark.BOOKMARK_WITH_URI.equals(metaType)) {
			return AnnotationType.BOOKMARK_WITH_URI;
		}
		if(Bookmark.BOOKMARK_WITHOUT_DESTINATION.equals(metaType)) {
			return AnnotationType.BOOKMARK_WITHOUT_DESTINATION;
		}
		if(CommentAnnotation.COMMENT.equals(metaType)) {
			return AnnotationType.COMMENT;
		}
		if(HighlightAnnotation.HIGHTLIGHTED_TEXT.equals(metaType)) {
			return AnnotationType.HIGHLIGHTED_TEXT;
		}
		if(metaType.toString().equalsIgnoreCase("TRUE_HIGHTLIGHTED_TEXT")){
			return AnnotationType.TRUE_HIGHLIGHTED_TEXT;
		}
		return null;
	}
	
	public void removeLinebreaks(IAnnotation annotation, Object annotationObject, PDDocument document) {
		if(this.removeLinebreaksDialogResult == JOptionPane.CANCEL_OPTION) return;
		
		String oldText = annotation.getTitle();
		String text = removeLinebreaks(annotation.getTitle(), false);
		
		if(text.equals(annotation.getTitle())) {
			return;
		}

		// Existing properties are not used yet --> help for Marcel  :)
//		boolean removeFromBookmarks = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.REMOVE_LINEBREAKS_BOOKMARKS_KEY);
//		boolean removeFromComments = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.REMOVE_LINEBREAKS_COMMENTS_KEY);
//		boolean removeFromHighlights = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.REMOVE_LINEBREAKS_HIGHLIGHTED_KEY);
//		if((annotation.getAnnotationType() == AnnotationType.BOOKMARK && !removeFromBookmarks)
//				|| (annotation.getAnnotationType() == AnnotationType.COMMENT && !removeFromComments)
//				|| (annotation.getAnnotationType() == AnnotationType.HIGHLIGHTED_TEXT && !removeFromHighlights)
//				) {
//			return;
//		}

		if(annotationObject != null && annotationObject instanceof PDOutlineItem){
			((PDOutlineItem)annotationObject).setTitle(text);
			annotation.setTitle(text);
		}
		if(annotationObject != null && annotationObject instanceof PDAnnotation){
			((PDAnnotation)annotationObject).setContents(text);
			annotation.setTitle(text);
		}
		try{
			document.save();
		}catch (IOException e) {
			if(e.getMessage().equals("destination is read only")){ //$NON-NLS-1$
				Object[] options = { TextUtils.getText("DocearRenameAnnotationListener.1"), TextUtils.getText("DocearRenameAnnotationListener.8"),TextUtils.getText("DocearRenameAnnotationListener.3") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				int result = this.removeLinebreaksDialogResult;
				if(result == JOptionPane.OK_OPTION){
					result = JOptionPane.showOptionDialog(Controller.getCurrentController().getMapViewManager().getSelectedComponent(), TextUtils.getText("DocearRenameAnnotationListener.6")+document.getName()+TextUtils.getText("DocearRenameAnnotationListener.7"), TextUtils.getText("DocearRenameAnnotationListener.5"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); //$NON-NLS-1$ //$NON-NLS-2$
				}				
				if( result == JOptionPane.OK_OPTION){
					if(annotationObject != null && annotationObject instanceof PDOutlineItem){
						((PDOutlineItem)annotationObject).setTitle(oldText);
						annotation.setTitle(oldText);
					}
					if(annotationObject != null && annotationObject instanceof PDAnnotation){
						((PDAnnotation)annotationObject).setContents(oldText);
						annotation.setTitle(oldText);
					}
					removeLinebreaks(annotation, annotationObject, document);			
				}
				else if( result == JOptionPane.CANCEL_OPTION ){	
					this.removeLinebreaksDialogResult = JOptionPane.CANCEL_OPTION;
					if(annotationObject != null && annotationObject instanceof PDOutlineItem){
						((PDOutlineItem)annotationObject).setTitle(oldText);
						annotation.setTitle(oldText);
					}
					if(annotationObject != null && annotationObject instanceof PDAnnotation){
						((PDAnnotation)annotationObject).setContents(oldText);
						annotation.setTitle(oldText);
					}
				}
				else if( result == JOptionPane.NO_OPTION ){
					this.removeLinebreaksDialogResult = JOptionPane.NO_OPTION;
					if(annotationObject != null && annotationObject instanceof PDOutlineItem){
						((PDOutlineItem)annotationObject).setTitle(oldText);
						annotation.setTitle(text);
					}
					if(annotationObject != null && annotationObject instanceof PDAnnotation){
						((PDAnnotation)annotationObject).setContents(oldText);
						annotation.setTitle(text);
					}
				}
			}
			else{
				LogUtils.severe("RemoveLinebreaksImport IOException at Target("+oldText+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (COSRuntimeException e) {
			LogUtils.severe("RemoveLinebreaksImport COSRuntimeException at Target("+oldText+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public static String removeLinebreaks(String text, boolean forced) {
		boolean removeLinebreaks = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.REMOVE_LINEBREAKS_KEY);
		if(removeLinebreaks || forced) {
			boolean keepDoubleLinebreaks = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.KEEP_DOUBLE_LINEBREAKS_KEY);
			boolean addSpaces = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.ADD_SPACES_KEY);
			boolean removeDashes = DocearController.getPropertiesController().getBooleanProperty(PdfUtilitiesController.REMOVE_DASHES_KEY);
			
			String lines[] = text.split("\\r?\\n");
			if(lines.length < 2) return text;
			StringBuilder sb = new StringBuilder();			
			for(int i = 0; i < lines.length; i++){
				if(keepDoubleLinebreaks && (i + 1 < lines.length) && lines[i + 1].isEmpty()){
					lines[i] = lines[i] + "\n\n";
					sb.append(lines[i]);
					i = i + 1;
					continue;
				}
				if(removeDashes && lines[i].endsWith("-")){
					lines[i] = lines[i].substring(0, lines[i].length() - 1);
					if(i + 1 < lines.length && lines[i + 1].startsWith(" ")){
						lines[i] = CharMatcher.WHITESPACE.trimFrom(lines[i]);
					}
					sb.append(lines[i]);
					continue;
				}
				if(addSpaces){				
					if((i + 1 < lines.length) && (!lines[i].endsWith(" ") && !lines[i + 1].startsWith(" "))){				
						lines[i] = CharMatcher.WHITESPACE.trimFrom(lines[i]) + " ";
						sb.append(lines[i]);					
						continue;
					}
				}
				sb.append(lines[i]);				
			}
			return sb.toString();
		}
		return text;
	}
	
	
	protected String extractAnnotationText(PDPage pdPage, PDTextMarkupAnnotation annotation) {
		
		StringBuilder sb = new StringBuilder();		
		COSArray rect = (COSArray)annotation.cosGetField(PDTextMarkupAnnotation.DK_QuadPoints);	
		for(int i = 0; i < (rect.size() / 8); i++){
			TextExtractor extractor = new TextExtractor();			
			Float lowerLeft_X = Math.min(Math.min(rect.get(0 + (8 * i)).getValueFloat(0), rect.get(2 + (8 * i)).getValueFloat(0)), Math.min(rect.get(4 + (8 * i)).getValueFloat(0), rect.get(6 + (8 * i)).getValueFloat(0)));
			Float upperRight_X = Math.max(Math.max(rect.get(0 + (8 * i)).getValueFloat(0), rect.get(2 + (8 * i)).getValueFloat(0)), Math.max(rect.get(4 + (8 * i)).getValueFloat(0), rect.get(6 + (8 * i)).getValueFloat(0)));
			Float lowerLeft_Y = Math.min(Math.min(rect.get(1 + (8 * i)).getValueFloat(0), rect.get(3 + (8 * i)).getValueFloat(0)), Math.min(rect.get(5 + (8 * i)).getValueFloat(0), rect.get(7 + (8 * i)).getValueFloat(0)));
			Float upperRight_y = Math.max(Math.max(rect.get(1 + (8 * i)).getValueFloat(0), rect.get(3 + (8 * i)).getValueFloat(0)), Math.max(rect.get(5 + (8 * i)).getValueFloat(0), rect.get(7 + (8 * i)).getValueFloat(0)));
			Shape shape = new Rectangle2D.Float(lowerLeft_X, lowerLeft_Y, upperRight_X - lowerLeft_X,  upperRight_y - lowerLeft_Y);
			extractor.setBounds(shape);
			AffineTransform pageTx = new AffineTransform();
			PDFGeometryTools.adjustTransform(pageTx, pdPage);
			extractor.setDeviceTransform(pageTx);
			CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(null, extractor);
			interpreter.process(pdPage.getContentStream(), pdPage.getResources());					
			sb.append(extractor.getContent().trim());
			if(i < ((rect.size() / 8) - 1)){
				sb.append("\n");
			}
		}		
		return sb.toString();
	}	
	
	public Integer getAnnotationDestination(PDAnnotation pdAnnotation) {				
		
		if(pdAnnotation != null){
			PDPage page = pdAnnotation.getPage();			
			if(page != null)
				return page.getNodeIndex()+1;
		}
		
		return null;		
	}

	public AnnotationModel searchAnnotation(URI uri, NodeModel node) throws Exception {
		this.currentFile = uri;
		if(!this.isImportAll()) this.setImportAll(true);
		List<AnnotationModel> annotations = this.importAnnotations(uri);
		this.setImportAll(false);
		return searchAnnotation(annotations, node);        
	}
	
	public AnnotationModel searchAnnotation(IAnnotation annotation) throws Exception {
		if(annotation.getAnnotationID() != null && annotation.getAnnotationID().getUri() != null){
			this.currentFile = annotation.getAnnotationID().getUri();
			if(!this.isImportAll()) this.setImportAll(true);
			List<AnnotationModel> annotations = this.importAnnotations(annotation.getAnnotationID().getUri());
			this.setImportAll(false);
			return searchAnnotation(annotations, annotation); 
		}
		else{
			return null;
		}
	}
	
	public AnnotationModel searchAnnotation(List<AnnotationModel> annotations, NodeModel node) {
		for(AnnotationModel annotation : annotations){           
			AnnotationModel extensionModel = AnnotationController.getModel(node, false);
			if(extensionModel == null){
				if(annotation.getTitle().equals(node.getText())){
					//TODO: DOCEAR is Update nodeModel good here??
					//TODO: DOCEAR How to deal with nodes without extension(and object number) and changed annotation title ??
					AnnotationController.setModel(node, annotation);
					return annotation;
				}
				else{
					AnnotationModel searchResult = searchAnnotation(annotation.getChildren(), node);
					if(searchResult != null) return searchResult;
				}
			}
			else{
				return searchAnnotation(annotations, extensionModel);
			}
           
       }
		return null;
	}
	
	private AnnotationModel searchAnnotation(List<AnnotationModel> annotations, IAnnotation target) {
		for(AnnotationModel annotation : annotations){ 
			if(annotation.getObjectID() == target.getObjectID()){
				return annotation;
			}
			else{
				AnnotationModel searchResult = searchAnnotation(annotation.getChildren(), target);
				if(searchResult != null) return searchResult;
			}
		}
		return null;
	}

	public boolean isImportAll() {
		return importAll;
	}

	public void setImportAll(boolean importAll) {
		this.importAll = importAll;
	}

}
