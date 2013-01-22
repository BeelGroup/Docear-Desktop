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

public class DocearNodeModifiedExtensionController implements IExtension {
	
	public static void install(ModeController modeController) {
		modeController.addExtension(DocearNodeModifiedExtensionController.class, new DocearNodeModifiedExtensionController(modeController));
	}
	
	private static DocearNodeModifiedExtensionController getController(ModeController modeController) {
		return (DocearNodeModifiedExtensionController) modeController.getExtension(DocearNodeModifiedExtensionController.class);
	}
	
	public static DocearNodeModifiedExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}
	
	public DocearNodeModifiedExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		//read the last moved time attribute
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
		//read the last folded time attribute
		readManager.addAttributeHandler("node", "LAST_FOLDED", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				if(node == null || value == null) {
					return;
				}
				try {
					getController(modeController).updateFoldedTime((NodeModel) node, Long.decode(value).longValue());
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		//read the last link opened time attribute
		readManager.addAttributeHandler("node", "LINK_OPENED", new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				if(node == null || value == null) {
					return;
				}
				try {
					getController(modeController).updateLinkOpenedTime((NodeModel) node, Long.decode(value).longValue());
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		});
		
		
		writeManager.addExtensionAttributeWriter(DocearNodeModifiedExtension.class, new IExtensionAttributeWriter() {
			public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
				try {
					final DocearNodeModifiedExtension modelExtension = extension != null ? (DocearNodeModifiedExtension) extension : DocearNodeModifiedExtensionController.getExtension((NodeModel) userObject);
					if (modelExtension == null) {
						return;
					}
					if(modelExtension.getLastMovedTime() > -1) {
						writer.addAttribute("MOVED", Long.toString(modelExtension.getLastMovedTime()));
					}
					if(modelExtension.getLastFoldedTime() > -1) {
						writer.addAttribute("LAST_FOLDED", Long.toString(modelExtension.getLastFoldedTime()));
					}
					if(modelExtension.getLastLinkOpenedTime() > -1) {
						writer.addAttribute("LINK_OPENED", Long.toString(modelExtension.getLastLinkOpenedTime()));
					}
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
		DocearNodeModifiedExtension model = getExtension(node);
		if(model == null) {
			model = new DocearNodeModifiedExtension();
			node.addExtension(model);
		}
		model.setLastMovedTime(timeMillis);
	}
	
	public void updateMovedTime(NodeModel node) {
		updateMovedTime(node, System.currentTimeMillis());
	}
		
	public void updateLinkOpenedTime(NodeModel node, long timeMillis) {
		if(node == null) {
			return;
		}
		DocearNodeModifiedExtension model = getExtension(node);
		if(model == null) {
			model = new DocearNodeModifiedExtension();
			node.addExtension(model);
		}
		model.setLastLinkOpenedTime(timeMillis);
	}
	
	public void updateLinkOpenedTime(NodeModel node) {
		updateLinkOpenedTime(node, System.currentTimeMillis());
	}
	
	public void updateFoldedTime(NodeModel node, long timeMillis) {
		if(node == null) {
			return;
		}
		DocearNodeModifiedExtension model = getExtension(node);
		if(model == null) {
			model = new DocearNodeModifiedExtension();
			node.addExtension(model);
		}
		model.setLastFoldedTime(timeMillis);
	}
	
	public void updateFoldedTime(NodeModel node) {
		updateFoldedTime(node, System.currentTimeMillis());
	}
	
	public static DocearNodeModifiedExtension getExtension(final NodeModel node) {
		DocearNodeModifiedExtension docearNodeModel = (DocearNodeModifiedExtension) node.getExtension(DocearNodeModifiedExtension.class);		
		return docearNodeModel;
	}
	
	
	/**************************
	 * 
	 * @author mg
	 *
	 */
	public class DocearNodeModifiedExtension implements IExtension {
		private long lastMoved = -1;
		private long lastLinkOpened = -1;
		private long lastFolded = -1;
		
		private long movedCount = 0;
		private long editedCount = 0;
		private long foldedCount = 0;
		private long openedCount = 0;
		
		public void setLastMovedTime(long timeMillis) {
			this.lastMoved = timeMillis;
		}
				
		public void setLastLinkOpenedTime(long timeMillis) {
			this.lastLinkOpened = timeMillis;
		}
		
		public void setLastFoldedTime(long timeMillis) {
			this.lastFolded = timeMillis;
		}
		
		public long getLastMovedTime() {
			return this.lastMoved;
		}
		
		public long getLastLinkOpenedTime() {
			return this.lastLinkOpened;
		}
		
		public long getLastFoldedTime() {
			return this.lastFolded;
		}
		
		public void incMovedCount() {
			this.movedCount++;
		}
		
		public long getMovedCount() {
			return this.movedCount;
		}
		
		public void incEditedCount() {
			this.editedCount++;
		}
		
		public long getEditedCount() {
			return this.editedCount;
		}
		
		public void incOpenedCount() {
			this.openedCount++;
		}
		
		public long getOpenedCount() {
			return this.openedCount;
		}
		
		public void incFoldedCount() {
			this.foldedCount++;
		}
		
		public long getFoldedCount() {
			return this.foldedCount;
		}
		
	}
}
