package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class DocearNodeMovedExtensionController implements IExtension {
	
	public static void install(ModeController modeController) {
		modeController.addExtension(DocearNodeMovedExtensionController.class, new DocearNodeMovedExtensionController(modeController));
	}
	
	private static DocearNodeMovedExtensionController getController(ModeController modeController) {
		return (DocearNodeMovedExtensionController) modeController.getExtension(DocearNodeMovedExtensionController.class);
	}
	
	public static DocearNodeMovedExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}
	
	public DocearNodeMovedExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		//read the last moved time
		readManager.addAttributeHandler("node", "MOVED", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				if(node == null || value == null) {
					return;
				}
				try {
					getController(modeController).updateMovedTime((NodeModel) node, Long.decode(value).longValue());
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		writeManager.addExtensionAttributeWriter(DocearNodeMovedExtension.class, new IExtensionAttributeWriter() {
			public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
				try {
					final DocearNodeMovedExtension modelExtension = extension != null ? (DocearNodeMovedExtension) extension : DocearNodeMovedExtensionController.getModel((NodeModel) userObject);
					if (modelExtension == null || modelExtension.getTime() == -1) {
						return;
					}
					writer.addAttribute("MOVED", Long.toString(modelExtension.getTime()));
					}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		
	}
	
	public void updateMovedTime(NodeModel node, long timeMillis) {
		if(node == null) {
			return;
		}
		DocearNodeMovedExtension model = getModel(node);
		if(model == null) {
			model = new DocearNodeMovedExtension();
			node.addExtension(model);
		}
		model.setTime(timeMillis);
	}
	
	public void updateMovedTime(NodeModel node) {
		updateMovedTime(node, System.currentTimeMillis());
	}

	public static DocearNodeMovedExtension getModel(final NodeModel node) {
		DocearNodeMovedExtension docearNodeModel = (DocearNodeMovedExtension) node.getExtension(DocearNodeMovedExtension.class);		
		return docearNodeModel;
	}
	
	
	/**************************
	 * 
	 * @author mag
	 *
	 */
	public class DocearNodeMovedExtension implements IExtension {
		private long timeMillis = -1;		
		
		public void setTime(long timeMillis) {
			this.timeMillis = timeMillis;
		}
		
		public void setTimeNow() {
			this.timeMillis = System.currentTimeMillis();
		}
		
		public long getTime() {
			return this.timeMillis;
		}
		
	}
}
