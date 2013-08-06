package org.docear.plugin.pdfutilities.features;

import java.io.IOException;

import org.docear.pdf.feature.APDMetaObject;
import org.docear.plugin.core.features.DocearFileBackupController;
import org.docear.plugin.core.features.DocearRequiredConversionController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;

public class AnnotationConverter implements IMapLifeCycleListener {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	private static IConversionProcessHandler currentHandler = new SingleMapConversionHandler();
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public static void convertAnnotations(MapModel map) throws IOException {
		if(map == null) {
			throw new IllegalArgumentException("NULL");
		}
		DocearFileBackupController.createBackupForConversion(map);
		synchronized (currentHandler) {
			currentHandler.convert(map);
			LogUtils.info("converted annotations for "+map.getTitle()+" - "+map.getFile());
		}
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
	
	public static IAnnotation cloneAnnotation(int uid, AnnotationModel model) {
		AnnotationModel newModel = new AnnotationModel(uid, model.getAnnotationType());
		newModel.setSource(model.getSource());
		newModel.setDestinationUri(model.getDestinationUri());
		newModel.setPage(model.getPage());
		newModel.setOldObjectNumber(model.getOldObjectNumber());
		newModel.getChildren().addAll(model.getChildren());
		return newModel;
	}
	
	public static void SetConversionProcessHandler(IConversionProcessHandler handler) {
		synchronized (currentHandler) {
			currentHandler = handler;
			if(handler == null) {
				currentHandler = new SingleMapConversionHandler();
			}
		}
	}
	
	public static IConversionProcessHandler getConversionProcessHandler() {
		return currentHandler;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void onCreate(MapModel map) {
		if(DocearRequiredConversionController.hasRequiredConversion(ConvertAnnotationsExtension.class, map)) {
			try {
				if(map instanceof MMapModel) {
					MMapModel mmap = (MMapModel) map;
					if (mmap.getTimerForAutomaticSaving() != null) {
						mmap.getTimerForAutomaticSaving().cancel();
					}
				}
				convertAnnotations(map);
				MMapIO mio = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				mio.writeToFile(map, map.getFile());
				if(map instanceof MMapModel) {
					MMapModel mmap = (MMapModel) map;
					mmap.scheduleTimerForAutomaticSaving();
				}
			} catch (IOException e) {
				LogUtils.warn(e);
			}
		}
	}

	public void onRemove(MapModel map) {}

	public void onSavedAs(MapModel map) {}

	public void onSaved(MapModel map) {}
}
