package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.pdf.annotation.AnnotationExtractor;
import org.docear.pdf.bookmark.BookmarkExtractor;
import org.docear.pdf.feature.ADocumentCreator;
import org.docear.pdf.feature.APDMetaObject;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.URIUtils;

import de.intarsys.pdf.pd.PDDocument;

public class SingleMapConversionHandler implements IConversionProcessHandler {
	private final Map<File, ExtractorAdaptor> documentCache = new HashMap<File, ExtractorAdaptor>();

	private int convertCount = 0;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void convert(MapModel map) {
		long time = System.currentTimeMillis();
		try {
			convertCount  = 0;
			convertAnnotation(map.getRootNode());
			DocearMapModelController.getModel(map).setVersion(DocearMapModelController.CURRENT_MAP_VERSION);
			map.setSaved(false);
		} finally {
			LogUtils.info("\n\n" + map.getFile() + "\n\n" + (System.currentTimeMillis() - time) + "\n\n(nodeCount: "+convertCount+")\n\n");
			close();
			System.gc();
		}
	}

	private void convertAnnotation(NodeModel node) {
		AnnotationModel extensionModel = AnnotationController.getModel(node, false);
		if (extensionModel != null) {
			convertCount++;
			if(extensionModel.getOldObjectNumber() > 0) {
				try {
					File file = URIUtils.getFile(extensionModel.getSource());
					synchronized (documentCache) {
						ExtractorAdaptor adapter = getCachedExtractor(file);
						if (adapter != null) {
							APDMetaObject annotation = adapter.findMetaForObjectNumber(extensionModel.getOldObjectNumber());
							if (annotation != null) {
								AnnotationController.setModel(node, AnnotationConverter.cloneAnnotation(annotation, extensionModel));
							}
						}
					}
				} catch (Exception e) {
					LogUtils.warn(e);
				}
			}
			else {
				if(AnnotationType.PDF_FILE.equals(extensionModel.getAnnotationType())) {
					AnnotationController.setModel(node, AnnotationConverter.cloneAnnotation(0, extensionModel));
				}
			}
		}
		for (NodeModel child : node.getChildren()) {
			convertAnnotation(child);
		}
	}

	private ExtractorAdaptor getCachedExtractor(File file) throws IOException {
		ExtractorAdaptor adapter = documentCache.get(file);
		if (adapter == null) {
			if(file.exists()) {
				trimCache();
				adapter = new ExtractorAdaptor(file);
				documentCache.put(file, adapter);
			}
		}
		return adapter;
	}

	private void trimCache() {
		if (documentCache.size() % 200 == 0) {
			System.gc();
		}
	}

	public void close() {
		synchronized (documentCache) {
			for (Entry<File, ExtractorAdaptor> entry : documentCache.entrySet()) {
				try {
					entry.getValue().close();
				} catch (IOException e) {
					LogUtils.warn(e);
				}
			}
			documentCache.clear();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
		System.gc();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public static class ExtractorAdaptor {
		private Map<Integer, APDMetaObject> objNumberIndex;
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 * @throws IOException 
		 **********************************************************************************/
		public ExtractorAdaptor(File file) throws IOException {
			if(file == null) {
				throw new IllegalArgumentException("NULL");
			}
			initializeIndex(file);
			
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		private void indexMetaList(List<APDMetaObject> metas) {
			for (APDMetaObject meta : metas) {
				if(meta.getObjectNumber() > 0) {
					objNumberIndex.put(meta.getObjectNumber(), meta);
				}
				if(meta.hasChildren()) {
					indexMetaList(meta.getChildren());
				}
			}
		}
		
		private void initializeIndex(File file) throws IOException {
			objNumberIndex = new LinkedHashMap<Integer, APDMetaObject>();
			PDDocument document = ADocumentCreator.getPDDocument(file);
			BookmarkExtractor bookmarkExtractor = null;
			try {
				bookmarkExtractor = new BookmarkExtractor(document);
				indexMetaList(bookmarkExtractor.getMetaObjects());
			} catch (IOException e) {
				LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.SingleMapConversionHandler.ExtractorAdaptor.initializeIndex("+file+")...bookmarks: "+e.getMessage());
			}
			AnnotationExtractor annotationExtractor = null;
			try {
				annotationExtractor = new AnnotationExtractor(document);
				indexMetaList(annotationExtractor.getMetaObjects());
			} catch (IOException e) {
				LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.SingleMapConversionHandler.ExtractorAdaptor.initializeIndex("+file+")...annotations: "+e.getMessage());
			}
			try {
				if(!document.isReadOnly()) {
					if((bookmarkExtractor != null && bookmarkExtractor.isDocumentModified()) || (annotationExtractor != null && annotationExtractor.isDocumentModified())) {
						document.save();
					}
				}
			} catch (IOException e) {
				LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.SingleMapConversionHandler.ExtractorAdaptor.initializeIndex("+file+")...save: "+e.getMessage());
			}
			//close and clear all temporarily objects
			try {
				if(annotationExtractor != null) {
					annotationExtractor.resetAll();
				}
				if(bookmarkExtractor != null) {
					bookmarkExtractor.resetAll();
				}
				if(document != null) {
					document.close();
				}
			} catch (IOException e) {
				LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.SingleMapConversionHandler.ExtractorAdaptor.initializeIndex("+file+")...close: "+e.getMessage());
			}
		}
		
		public APDMetaObject findMetaForObjectNumber(Integer objectNumber) {
			return objNumberIndex.get(objectNumber);
		}
		
		public void close() throws IOException {
			objNumberIndex.clear();
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
}