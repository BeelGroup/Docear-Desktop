package org.docear.plugin.pdfutilities.listener;

import java.util.Map.Entry;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.pdfutilities.features.DocearNodeMonitoringExtension;
import org.docear.plugin.pdfutilities.features.DocearNodeMonitoringExtensionController;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class DocearNodeSelectionListener implements INodeSelectionListener {

	public void onDeselect(NodeModel node) {
		Controller.getCurrentController().getViewController().removeStatus("Annotation Info"); //$NON-NLS-1$
		Controller.getCurrentController().getViewController().removeStatus("Map Version"); //$NON-NLS-1$
		Controller.getCurrentController().getViewController().removeStatus("Docear Extension Info"); //$NON-NLS-1$
	}

	public void onSelect(NodeModel node) {	
		DocearMapModelExtension mapExtension = DocearMapModelController.getModel(node.getMap());
		String mapVersion = ""; //$NON-NLS-1$
		Controller.getCurrentController().getViewController().removeStatus("DocearMapInfo");
		if(mapExtension != null){
			mapVersion = TextUtils.getText("DocearNodeSelectionListener.4") + mapExtension.getVersion();  //$NON-NLS-1$
		}
		else{
			mapVersion = TextUtils.getText("DocearNodeSelectionListener.5");  //$NON-NLS-1$
		}
		Controller.getCurrentController().getViewController().addStatusInfo("DocearMapInfo", mapVersion); //$NON-NLS-1$
		
		
		IAnnotation model = AnnotationController.getModel(node, false);
		
		if(model != null){
			StringBuilder builder = new StringBuilder();
			
			if(model.getAnnotationType() != null){
				builder.append(TextUtils.getText("DocearNodeSelectionListener.7") + model.getAnnotationType()); //$NON-NLS-1$
			}
			
			if(model.getPage() != null){
				builder.append(TextUtils.getText("DocearNodeSelectionListener.8") + model.getPage()); //$NON-NLS-1$
			}
			
			if(model.getObjectID() >= 0){
				builder.append(TextUtils.getText("DocearNodeSelectionListener.9") + model.getObjectID()); //$NON-NLS-1$
			}
			
//			if(model.getAnnotationID() != null){
//				builder.append(TextUtils.getText("DocearNodeSelectionListener.11") + model.getAnnotationID().getObjectID()); //$NON-NLS-1$
//			}
			
			Controller.getCurrentController().getViewController().addStatusInfo("DocearAnnotationModelInfo", builder.toString()); //$NON-NLS-1$
		}
		else {
			Controller.getCurrentController().getViewController().removeStatus("DocearAnnotationModelInfo");
		}
		
		DocearNodeMonitoringExtension extension = DocearNodeMonitoringExtensionController.getModel(node);
		if(extension != null){
			StringBuilder builder = new StringBuilder();
			
			for(Entry<String, Object> entry : extension.getAllEntries()){
				if(builder.length() < 1){
					builder.append(entry.getKey());
				}
				else{
					builder.append(TextUtils.getText("DocearNodeSelectionListener.13") + entry.getKey()); //$NON-NLS-1$
				}
				if(entry.getValue() != null){
					if(entry.getValue() instanceof String){
						if(((String)entry.getValue()).length() > 0){
							builder.append(": " + entry.getValue()); //$NON-NLS-1$
						}
					}
					else{
						builder.append(": " + entry.getValue()); //$NON-NLS-1$
					}
				}
			}
			
			Controller.getCurrentController().getViewController().addStatusInfo("DocearExtensionInfo", builder.toString()); //$NON-NLS-1$
		}
		else {
			Controller.getCurrentController().getViewController().removeStatus("DocearExtensionInfo");
		}
	}

}
