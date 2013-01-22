package org.docear.plugin.core.features;

import java.util.Date;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class DocearNodePrivacyExtensionController implements IExtension {

	public enum DocearPrivacyLevel {
		PUBLIC, DEMO, PRIVATE 
	}

	public static void install(ModeController modeController) {
		modeController.addExtension(DocearNodePrivacyExtensionController.class, new DocearNodePrivacyExtensionController(modeController));
	}
	
	private static DocearNodePrivacyExtensionController getController(ModeController modeController) {
		return (DocearNodePrivacyExtensionController) modeController.getExtension(DocearNodePrivacyExtensionController.class);
	}
	
	public static DocearNodePrivacyExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}
	
	public static DocearNodePrivacyExtension getExtension(final NodeModel node) {
		DocearNodePrivacyExtension extension = (DocearNodePrivacyExtension) node.getExtension(DocearNodePrivacyExtension.class);		
		return extension;
	}
	
	public DocearNodePrivacyExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		//read the last moved time attribute
		readManager.addAttributeHandler("node", "DCR_PRIVACY_LEVEL", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				if(node == null || value == null) {
					return;
				}
				try {
					getController(modeController).setPrivacyLevel((NodeModel) node, DocearPrivacyLevel.valueOf(value));
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		
		
		writeManager.addExtensionAttributeWriter(DocearNodePrivacyExtension.class, new IExtensionAttributeWriter() {
			public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
				try {
					final DocearNodePrivacyExtension modelExtension = extension != null ? (DocearNodePrivacyExtension) extension : DocearNodePrivacyExtensionController.getExtension((NodeModel) userObject);
					if (modelExtension == null) {
						return;
					}
					if(!modelExtension.getPrivacyLevel().equals(DocearPrivacyLevel.PUBLIC)) {
						writer.addAttribute("DCR_PRIVACY_LEVEL", modelExtension.getPrivacyLevel().toString());
					}
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		
	}

	public void setPrivacyLevel(NodeModel node, DocearPrivacyLevel level) {
		if(node == null) {
			return;
		}
		DocearNodePrivacyExtension extension = getExtension(node);
		DocearPrivacyLevel oldValue = null;
		if(extension == null) {
			extension = new DocearNodePrivacyExtension();
			node.addExtension(extension);
		}
		else {
			oldValue = extension.getPrivacyLevel();
		}
		extension.setPrivacyLevel(level);
		// update modification time
		node.getHistoryInformation().setLastModifiedAt(new Date());
		node.fireNodeChanged(new NodeChangeEvent(node, "DOCEAR_PRIVACY_LEVEL", oldValue, level));
		
	}
	
	/***************************************************
	 * @author mg
	 *
	 */
	public class DocearNodePrivacyExtension implements IExtension {
		
		private DocearPrivacyLevel privacyLevel = DocearPrivacyLevel.PUBLIC;

		public void setPrivacyLevel(DocearPrivacyLevel level) {
			if(level == null) {
				level = DocearPrivacyLevel.PUBLIC;
			}
			this.privacyLevel = level;
		}
		
		public DocearPrivacyLevel getPrivacyLevel() {
			return this.privacyLevel;
		}

	}
	
}
