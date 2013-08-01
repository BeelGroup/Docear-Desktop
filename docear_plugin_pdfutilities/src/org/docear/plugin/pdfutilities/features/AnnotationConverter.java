package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.pdf.annotation.AnnotationExtractor;
import org.docear.pdf.bookmark.BookmarkExtractor;
import org.docear.pdf.feature.APDMetaObject;
import org.docear.plugin.core.features.DocearFileBackupController;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearRequiredConversionController;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.FileLocator;

public class AnnotationConverter implements IMapLifeCycleListener {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public static void convertAnnotations(MapModel map) throws IOException {
		if(map == null) {
			throw new IllegalArgumentException("NULL");
		}
		DocearFileBackupController.createBackupForConversion(map);
		new AnnotationConversionProcess(map).convert();
		LogUtils.info("converted annotations for "+map.getTitle()+" - "+map.getFile());
	}
	
	public static IAnnotation cloneAnnotation(APDMetaObject metaObject, AnnotationModel model) {
		AnnotationModel newModel = new AnnotationModel(metaObject.getUID(), model.getAnnotationType());
		newModel.setSource(model.getSource());
		newModel.setDestinationUri(model.getDestinationUri());
		newModel.setPage(model.getPage());
		newModel.setOldObjectNumber(model.getOldObjectNumber());
		newModel.getChildren().addAll(model.getChildren());
		return newModel;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void onCreate(MapModel map) {
		if(DocearRequiredConversionController.hasRequiredConversion(ConvertAnnotationsExtension.class, map)) {
			try {
				convertAnnotations(map);
				MMapIO mio = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				mio.save(map);
			} catch (IOException e) {
				LogUtils.warn(e);
			}
		}

	}

	public void onRemove(MapModel map) {}

	public void onSavedAs(MapModel map) {}

	public void onSaved(MapModel map) {}
	
	public static class AnnotationConversionProcess extends Thread {

		private static final int MAX_CACHED_ITEMS = 200;
		private final MapModel map;
		private final Map<File, ExtractorAdaptor> documentCache = new HashMap<File, ExtractorAdaptor>();

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public AnnotationConversionProcess(MapModel map) {
			this.map = map;
		}

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public void convert() {
			try {
				convertAnnotation(map.getRootNode());
				DocearMapModelController.getModel(map).setVersion(DocearMapModelController.CURRENT_MAP_VERSION);
				map.setSaved(false);
			}
			finally {
				close();
			}
		}
		
		private void convertAnnotation(NodeModel node) {
			AnnotationModel extensionModel = AnnotationController.getModel(node, false);
			if(extensionModel != null && extensionModel.getOldObjectNumber() > 0){
				try {
//					URI absoluteUri = URIUtils.resolveURI(URIUtils.getAbsoluteURI(map), uri);
//					File file = URIUtils.getFile(absoluteUri);
					File file = URIUtils.getFile(extensionModel.getSource());
					synchronized (documentCache) {
						ExtractorAdaptor adapter = getCachedExtractor(file);
						if(adapter != null) {
							switch (extensionModel.getAnnotationType()) {
							case BOOKMARK:
							case BOOKMARK_WITH_URI:
							case BOOKMARK_WITHOUT_DESTINATION:
								APDMetaObject bookmark = adapter.findMetaForObjectNumber(extensionModel.getOldObjectNumber());
								if(bookmark != null) {
									AnnotationController.setModel(node, cloneAnnotation(bookmark, extensionModel));
								}
								break;
							case HIGHLIGHTED_TEXT:
							case COMMENT:
								APDMetaObject annotation = adapter.findMetaForObjectNumber(extensionModel.getOldObjectNumber());
								if(annotation != null) {
									AnnotationController.setModel(node, cloneAnnotation(annotation, extensionModel));
									break;
								}
								
								break;
							}
						}
					}
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
			for(NodeModel child : node.getChildren()) {
				convertAnnotation(child);
			}
		}

		private ExtractorAdaptor getCachedExtractor(File file) throws IOException, COSLoadException {
			ExtractorAdaptor adapter = documentCache.get(file);
			if(adapter == null) {
				PDDocument document = getPDDocument(file);
				if(document != null) {
					trimCache();
					adapter = new ExtractorAdaptor(document);
					documentCache.put(file, adapter);
					LogUtils.info("EXTRACTOR-ADDED---("+documentCache.size()+")---"+file);
				}
			}
			return adapter;
		}

		private void trimCache() { 
			if(documentCache.size() % 200 == 0 ) {
				System.gc();
			}
//			if(documentCache.size() > MAX_CACHED_ITEMS) {
//				LogUtils.info("---TRIM-CONVERTER-CACHE---");
//				int threshold = 50;
//				Iterator<Entry<File, ExtractorAdaptor>> iter = documentCache.entrySet().iterator();
//				while(iter.hasNext() && threshold > 0) {
//					Entry<File, ExtractorAdaptor> entry = iter.next();
//					try {
//						long lastModified = entry.getKey().lastModified();
//						entry.getValue().save();
//						entry.getValue().close();
//						entry.getKey().setLastModified(lastModified);
//					} catch (IOException e) {
//						LogUtils.warn(e);
//					}
//					iter.remove();
//					threshold--;
//				}
//			}
		}
		
		private PDDocument getPDDocument(File file) throws IOException, COSLoadException, COSRuntimeException {
			if(file == null || !file.exists() || !new PdfFileFilter().accept(file)){
				return null;
			}
			
			FileLocator locator = new FileLocator(file);		
			PDDocument document = PDDocument.createFromLocator(locator);
			locator = null;
			return document;
		}
		
		public void run() {
			convert();
		}
		
		public void close() {
			synchronized (documentCache) {
				for (Entry<File, ExtractorAdaptor> entry : documentCache.entrySet()) {
					try {
						long lastModified = entry.getKey().lastModified();
						entry.getValue().save();
						entry.getValue().close();
						entry.getKey().setLastModified(lastModified);
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
		}
		
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
	
	public static class ExtractorAdaptor {
		private PDDocument document;
		private AnnotationExtractor annotationExt;
		private BookmarkExtractor bookmarkExt;
		private Map<Integer, APDMetaObject> objNumberIndex;
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/
		public ExtractorAdaptor(PDDocument document) {
			if(document == null) {
				throw new IllegalArgumentException("NULL");
			}
			this.document = document;
			
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		private AnnotationExtractor getAnnotationExtractor() {
			if(annotationExt == null) {
				annotationExt = new AnnotationExtractor(document);
			}
			return annotationExt;
		}
		
		private BookmarkExtractor getBookmarkExtractor() {
			if(bookmarkExt == null) {
				bookmarkExt = new BookmarkExtractor(document);
			}
			return bookmarkExt;
		}
		
		private void indexMetaList(List<APDMetaObject> metas) {
			for (APDMetaObject meta : metas) {
				if(meta.getObjectNumber() >= 0) {
					objNumberIndex.put(meta.getObjectNumber(), meta);
				}
				if(meta.hasChildren()) {
					indexMetaList(meta.getChildren());
				}
			}
		}
		
		public APDMetaObject findMetaForObjectNumber(Integer objectNumber) {
			if(objNumberIndex == null) {
				objNumberIndex = new LinkedHashMap<Integer, APDMetaObject>();
				try {
					indexMetaList(getBookmarkExtractor().getMetaObjects());
				} catch (IOException e) {
					LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.AnnotationConverter.ExtractorAdaptor.findMetaForObjectNumber(objectNumber)...bookmarks: "+e.getMessage());
				}
				try {
					indexMetaList(getAnnotationExtractor().getMetaObjects());
				} catch (IOException e) {
					LogUtils.warn("Exception in org.docear.plugin.pdfutilities.features.AnnotationConverter.ExtractorAdaptor.findMetaForObjectNumber(objectNumber)...annotations: "+e.getMessage());
				}
				try {
					save();
				} catch (IOException e) {
				}
				try {
					close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return objNumberIndex.get(objectNumber);
		}
		public void save() throws IOException {
			if(!document.isReadOnly()) {
				if((bookmarkExt != null && bookmarkExt.isDocumentModified()) 
						|| (annotationExt != null && annotationExt.isDocumentModified())) {
					document.save();
				}
			}
		}
		
		public void close() throws IOException {
			if(annotationExt != null) {
				annotationExt.resetAll();
				annotationExt = null;
			}
			if(bookmarkExt != null) {
				bookmarkExt.resetAll();
				bookmarkExt = null;
			}
			if(document != null) {
				document.close();
			}
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
}
