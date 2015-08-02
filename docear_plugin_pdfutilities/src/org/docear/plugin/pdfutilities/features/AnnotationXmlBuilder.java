package org.docear.plugin.pdfutilities.features;

import java.io.IOException;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearRequiredConversionController;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.URIUtils;

public class AnnotationXmlBuilder implements IElementDOMHandler, IExtensionElementWriter {
	
	private static final String ANNOTATION_PAGE_XML_TAG = "page"; //$NON-NLS-1$
	private static final String ANNOTATION_TYPE_XML_TAG = "type"; //$NON-NLS-1$
	private static final String ANNOTATION_HIGHLIGHT_TYPE_XML_TAG = "highlight_type"; //$NON-NLS-1$
	private static final String ANNOTATION_OBJECT_NUMBER_XML_TAG = "object_number"; //$NON-NLS-1$
	private static final String ANNOTATION_OBJECT_ID_XML_TAG = "object_id"; //$NON-NLS-1$
	private static final String DOCUMENT_HASH_XML_TAG = "document_hash"; //$NON-NLS-1$
	private static final String PDF_ANNOTATION_XML_TAG = "pdf_annotation"; //$NON-NLS-1$
	private static final String PDF_TITLE_XML_TAG = "pdf_title";
	
	
	public AnnotationXmlBuilder(){		
	}
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(PDF_ANNOTATION_XML_TAG, this);
		//DOCEAR - just read the title elements to prevent freeplane from handling it as unknown
		reader.addElementHandler(PDF_TITLE_XML_TAG, new IElementDOMHandler() {			
			public Object createElement(Object parent, String tag, XMLElement attributes) {
				return null;
			}
			
			public void endElement(Object parent, String tag, Object element, XMLElement dom) {				
			}
		});
		registerAttributeHandlers(reader);
		writer.addExtensionElementWriter(AnnotationModel.class, this);		
	}

	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				if(annotation.getAnnotationType() == null || !annotation.getAnnotationType().equals(AnnotationType.TRUE_HIGHLIGHTED_TEXT)){
					annotation.setAnnotationType(AnnotationModel.AnnotationType.valueOf(value));
				}
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_PAGE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try{
					annotation.setPage(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Page Number."); //$NON-NLS-1$
				}
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_OBJECT_NUMBER_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try{
					annotation.setOldObjectNumber(Integer.parseInt(value));
				} catch(NumberFormatException e){
					LogUtils.warn("Could not Parse Pdf Annotation Object Number."); //$NON-NLS-1$
				}
			}
			
		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_HIGHLIGHT_TYPE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;				
				annotation.setAnnotationType(AnnotationModel.AnnotationType.valueOf(value));				
			}
			
		});
		
//		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, ANNOTATION_GENERATION_NUMBER_XML_TAG, new IAttributeHandler() {
//			
//			public void setAttribute(Object node, String value) {
//				final AnnotationModel annotation = (AnnotationModel) node;
//				try{
//					annotation.setGenerationNumber(Integer.parseInt(value));
//				} catch(NumberFormatException e){
//					LogUtils.warn("Could not Parse Pdf Annotation Generation Number."); //$NON-NLS-1$
//				}
//			}
//			
//		});
		
		reader.addAttributeHandler(PDF_ANNOTATION_XML_TAG, DOCUMENT_HASH_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final AnnotationModel annotation = (AnnotationModel) node;
				try {
					AnnotationController.registerDocumentHash(annotation.getSource(), value);
				}
				catch (Throwable e) {
					LogUtils.info("Error ("+e.getMessage()+") for: "+annotation.getSource());
				}
			}
			
		});
		
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if(attributes == null) {
			return null;
		}
		if (tag.equals(PDF_ANNOTATION_XML_TAG)) {
			final AnnotationModel oldAnnotationModel = AnnotationController.getModel((NodeModel) parent, false);
			if(oldAnnotationModel != null){
				return oldAnnotationModel;
			}
			else{
				AnnotationModel model = new AnnotationModel(-1);
				try{
					MapModel map = ((NodeModel)parent).getMap();
					DocearMapModelExtension mapExt = DocearMapModelController.getModel(map);
					if(mapExt != null && mapExt.getVersion().compareTo(DocearMapModelController.CURRENT_MAP_VERSION) < 0) {
						DocearRequiredConversionController.setRequiredConversion(new ConvertAnnotationsExtension(), map);
					}
					String obj_id = attributes.getAttribute(ANNOTATION_OBJECT_ID_XML_TAG, null);
					model = new AnnotationModel(Long.parseLong(obj_id));
				}
				catch(NumberFormatException e){
					//LogUtils.warn("Could not Parse Pdf Annotation Object ID.");
				}
				try {
					model.setSource(URIUtils.getAbsoluteURI((NodeModel) parent));
					return model;
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		}
		return null;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof AnnotationModel) {
				final AnnotationModel annotation = (AnnotationModel) userObject;
				if(AnnotationType.PDF_FILE.equals(annotation.getAnnotationType())) {
					AnnotationController.setModel(node, AnnotationConverter.cloneAnnotation(0, annotation));
				}
				else {
					AnnotationController.setModel(node, annotation);
				}
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}

	public void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension) throws IOException {
		
		final AnnotationModel model = extension != null ? (AnnotationModel) extension : AnnotationController.getModel(node, false);
		if (model == null) {
			return;
		}
		final XMLElement pdfAnnotation = new XMLElement();
		pdfAnnotation.setName(PDF_ANNOTATION_XML_TAG);
		
		final AnnotationType annotationType = model.getAnnotationType();
		if (annotationType != null) {
			if(annotationType.equals(AnnotationType.TRUE_HIGHLIGHTED_TEXT)){
				pdfAnnotation.setAttribute(ANNOTATION_HIGHLIGHT_TYPE_XML_TAG, annotationType.toString());
				pdfAnnotation.setAttribute(ANNOTATION_TYPE_XML_TAG, AnnotationType.HIGHLIGHTED_TEXT.toString());
			}
			else{
				pdfAnnotation.setAttribute(ANNOTATION_TYPE_XML_TAG, annotationType.toString());
			}
		}
		
		final Integer page = model.getPage();
		if (page != null) {
			pdfAnnotation.setAttribute(ANNOTATION_PAGE_XML_TAG, "" + page); //$NON-NLS-1$
		}
		
		final long objectID = model.getObjectID();
		if(objectID >= 0) {
			pdfAnnotation.setAttribute(ANNOTATION_OBJECT_ID_XML_TAG, "" + objectID); //$NON-NLS-1$
		}
		
		final int objectNumber = model.getOldObjectNumber();
		if(objectNumber >= 0) {
			pdfAnnotation.setAttribute(ANNOTATION_OBJECT_NUMBER_XML_TAG, "" + objectNumber); //$NON-NLS-1$
		}
		
		final String documentHash = model.getDocumentHash();
		if(documentHash != null && documentHash.length() > 0){
			pdfAnnotation.setAttribute(DOCUMENT_HASH_XML_TAG, "" + documentHash);
			final String documentTitle = AnnotationController.getDocumentTitle(model.getSource());
			if(documentTitle != null) {
				final XMLElement pdftitle = new XMLElement();
				pdftitle.setName(PDF_TITLE_XML_TAG);
				pdftitle.setContent(documentTitle);
				pdfAnnotation.addChild(pdftitle);
			}
		}
		
		writer.addElement(model, pdfAnnotation);
		
	}

}
