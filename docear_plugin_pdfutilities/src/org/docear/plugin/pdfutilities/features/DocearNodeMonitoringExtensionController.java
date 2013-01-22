package org.docear.plugin.pdfutilities.features;

import org.docear.plugin.pdfutilities.features.DocearNodeMonitoringExtension.DocearExtensionKey;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class DocearNodeMonitoringExtensionController implements IExtension{
	
	public static DocearNodeMonitoringExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static DocearNodeMonitoringExtensionController getController(ModeController modeController) {
		return (DocearNodeMonitoringExtensionController) modeController.getExtension(DocearNodeMonitoringExtensionController.class);
	}
	public static void install( final DocearNodeMonitoringExtensionController docearNodeModelController) {
		Controller.getCurrentModeController().addExtension(DocearNodeMonitoringExtensionController.class, docearNodeModelController);
	}
	
	public DocearNodeMonitoringExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		DocearNodeMonitoringExtensionXmlBuilder builder = new DocearNodeMonitoringExtensionXmlBuilder();
		builder.registerBy(readManager, writeManager);
	}
	
	public static DocearNodeMonitoringExtension getModel(final NodeModel node) {
		DocearNodeMonitoringExtension docearNodeModel = (DocearNodeMonitoringExtension) node.getExtension(DocearNodeMonitoringExtension.class);		
		return docearNodeModel;
	}
	
	public static void setModel(final NodeModel node, final DocearNodeMonitoringExtension docearNodeModel) {
		final DocearNodeMonitoringExtension olddocearNodeModel = (DocearNodeMonitoringExtension) node.getExtension(DocearNodeMonitoringExtension.class);
		if (docearNodeModel != null && olddocearNodeModel == null) {
			node.addExtension(docearNodeModel);
		}
		else if (docearNodeModel == null && olddocearNodeModel != null) {
			node.removeExtension(DocearNodeMonitoringExtension.class);
		}
	}
	
	public static boolean containsKey(NodeModel node, DocearExtensionKey key){
		DocearNodeMonitoringExtension extension = getModel(node);
		if(extension != null){
			return extension.containsKey(key.toString());
		}		
		return false;
	}
	
	public static DocearNodeMonitoringExtension setEntry(NodeModel node, DocearExtensionKey key, Object value){
		return setEntry(node, key.toString(), value);
	}
	
	public static DocearNodeMonitoringExtension setEntry(NodeModel node, String key, Object value){
		DocearNodeMonitoringExtension extension = getModel(node);
		if(extension == null){
			extension = new DocearNodeMonitoringExtension();
			extension.putEntry(key, value);
			setModel(node, extension);
		}
		else{
			extension.putEntry(key, value);
		}
		return extension;
	}

}
