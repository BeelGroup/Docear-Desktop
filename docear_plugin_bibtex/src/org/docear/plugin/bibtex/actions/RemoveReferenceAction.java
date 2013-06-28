package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

@EnabledAction(checkOnNodeChange=true)
public class RemoveReferenceAction extends AFreeplaneAction {

	public static final String KEY = "RemoveReferenceAction";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RemoveReferenceAction() {
		super(KEY);
	}

    public void actionPerformed(ActionEvent e) {
	for (NodeModel node : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
	    ReferencesController.getController().getJabRefAttributes().removeReferenceFromNode(node);

	    ((MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent()).getNodeView(node).updateAll();
	}

    }

    public void setEnabled() {
	Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();

		for (NodeModel node : nodes) {
			if (node == null) {
				setEnabled(false);
				return;
			}

			final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);

			if (bibtexKey != null && bibtexKey.length() > 0) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

    }

}
