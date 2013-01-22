package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.Date;

import org.docear.plugin.core.features.DocearNodePrivacyExtensionController;
import org.docear.plugin.core.features.DocearNodePrivacyExtensionController.DocearPrivacyLevel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class DocearSetNodePrivacyAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String KEY = "DocearNodePrivacyAction";

	public DocearSetNodePrivacyAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			boolean dirtyMap = false;
			DocearPrivacyLevel level = DocearPrivacyLevel.DEMO;
			
			//DOCEAR - show Privacy Dialog here
			
			if(level == null) {
				return;
			}
			Date now = new Date();
			Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
			for (NodeModel nodeModel : nodes) {
				// set the privacy level
				DocearNodePrivacyExtensionController.getController().setPrivacyLevel(nodeModel, level);
				// update modification time
				nodeModel.getHistoryInformation().setLastModifiedAt(now);
				dirtyMap = true;
			}
			if(dirtyMap) {
				MapModel map = Controller.getCurrentModeController().getMapController().getRootNode().getMap();
				map.setSaved(false);
				map.fireMapChangeEvent(new MapChangeEvent(nodes, map, "DCR_PRIVACY_CHANGED", null, level));
			}
		}
		catch (final Exception ex) {
			UITools.errorMessage(ex);
			LogUtils.warn(ex);
		}

	}

}
