package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.docear.pdf.annotation.AnnotationExtractor;
import org.docear.pdf.bookmark.Bookmark;
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
		DocearFileBackupController.createBackup("convert_annotations", map);
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
						ExtractorAdaptor adapter = documentCache.get(file);
						if(adapter == null) {
							adapter = new ExtractorAdaptor(getPDDocument(file));
							documentCache.put(file, adapter);
						}
						
						switch (extensionModel.getAnnotationType()) {
						case BOOKMARK:
						case BOOKMARK_WITH_URI:
						case BOOKMARK_WITHOUT_DESTINATION:
							for (APDMetaObject bookmark : adapter.getBookmarkExtractor().getMetaObjects()) {
								if(bookmark instanceof Bookmark) {
									if(bookmark.getObjectNumber() == extensionModel.getOldObjectNumber()) {
										AnnotationController.setModel(node, cloneAnnotation(bookmark, extensionModel));
										return;
									}
								}
							}
							break;
						case HIGHLIGHTED_TEXT:
						case COMMENT:
							for (APDMetaObject annotation : adapter.getAnnotationExtractor().getMetaObjects()) {
								if(annotation.getObjectNumber() == extensionModel.getOldObjectNumber()) {
									AnnotationController.setModel(node, cloneAnnotation(annotation, extensionModel));
									return;
								}
							}
							break;
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
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/
		public ExtractorAdaptor(PDDocument document) {
			this.document = document;
			
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public AnnotationExtractor getAnnotationExtractor() {
			if(annotationExt == null) {
				annotationExt = new AnnotationExtractor(document);
			}
			return annotationExt;
		}
		
		public BookmarkExtractor getBookmarkExtractor() {
			if(bookmarkExt == null) {
				bookmarkExt = new BookmarkExtractor(document);
			}
			return bookmarkExt;
		}
		
		public void save() throws IOException {
			if(!document.isReadOnly()) {
				if(bookmarkExt.isDocumentModified() || annotationExt.isDocumentModified()) {
					document.save();
				}
			}
		}
		
		public void close() throws IOException {
			annotationExt.resetAll();
			annotationExt = null;
			bookmarkExt.resetAll();
			bookmarkExt = null;
			document.close();
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
}
