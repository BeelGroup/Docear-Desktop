package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.docear.pdf.feature.APDMetaObject;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.features.SingleMapConversionHandler.ExtractorAdaptor;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.URIUtils;

public class BatchMapConversionHandler implements IConversionProcessHandler {
	private final Map<File, ExtractorAdaptor> documentCache = new HashMap<File, ExtractorAdaptor>();
	private final ChangeListener changeListener;
	private int convertCount = 0;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public BatchMapConversionHandler(ChangeListener listener) {
		this.changeListener = listener;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void dispatchChange(String text) {
		if(changeListener != null) {
			ChangeEvent event = new ChangeEvent(text);
			changeListener.stateChanged(event);
		}
	}
	
	private void convertAnnotation(NodeModel node) {
		AnnotationModel extensionModel = AnnotationController.getModel(node, false);
//		if (extensionModel != null && extensionModel.getOldObjectNumber() > 0) {
//			convertCount++;
//			try {
//				File file = URIUtils.getFile(extensionModel.getSource());
//				synchronized (documentCache) {
//					ExtractorAdaptor adapter = getCachedExtractor(file);
//					if (adapter != null) {
//						APDMetaObject annotation = adapter.findMetaForObjectNumber(extensionModel.getOldObjectNumber());
//						if (annotation != null) {
//							AnnotationController.setModel(node, AnnotationConverter.cloneAnnotation(annotation, extensionModel));
//						}
//						else {
//							if(AnnotationType.PDF_FILE.equals(extensionModel.getAnnotationType())) {
//								AnnotationController.setModel(node, AnnotationConverter.cloneAnnotation(0, extensionModel));
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				LogUtils.warn(e);
//			}
//		}
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
			else {
				LogUtils.warn("BatchMapConversionHandler.getCachedExtractor() - File not found: " + file);
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
			System.gc();
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
	
	public void convert(MapModel map) {
		dispatchChange(TextUtils.format("docear.convert.annotations.start", map.getTitle(), map.getFile()));
		dispatchChange(TextUtils.getText("docear.convert.annotations.takeawhile"));
		long time = System.currentTimeMillis();
		try {
			convertCount  = 0;
			convertAnnotation(map.getRootNode());
			DocearMapModelController.getModel(map).setVersion(DocearMapModelController.CURRENT_MAP_VERSION);
			map.setSaved(false);
		} 
		finally {
			dispatchChange(TextUtils.format("docear.convert.annotations.final", convertCount, ((System.currentTimeMillis() - time)/1000)+"sec"));
		}
	}
}
