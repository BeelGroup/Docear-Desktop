package org.docear.plugin.core.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.docear.plugin.core.util.NodeUtilities;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.components.menu.CheckEnableOnPopup;

@CheckEnableOnPopup
@EnabledAction(checkOnNodeChange=true)
public class DocearRemoveNodeLinkAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	public final static String KEY = "DocearRemoveNodeLinkAction";
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearRemoveNodeLinkAction() {
		super(KEY);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	@Override
	public void setEnabled() {
		try {
			IMapSelection selection = Controller.getCurrentController().getSelection();
			if(selection != null) {
				for(NodeModel node : selection.getSelection()) { 
					if(NodeUtilities.hasHyperlink(node)) {
						setEnabled(true);
						return;
					}
				}
			}
		}
		catch(Exception e) {
		}
		setEnabled(false);
	}	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			IMapSelection selection = Controller.getCurrentController().getSelection();
			if(selection != null) {
				Controller.getCurrentModeController().execute(createNodeRemoveHyperlinkActor(selection.getSelection()), selection.getSelected().getMap());
			}
		}
		catch(Exception e) {
		}
		
	}
	
	public static IActor createNodeRemoveHyperlinkActor(final Set<NodeModel> nodes) {
		final IActor actor = new IActor() {
			private Map<NodeModel, URI> oldlinkMap = new LinkedHashMap<NodeModel, URI>();
			
			public void act() {
				for(NodeModel node : nodes) {
					NodeLinks links = NodeLinks.getLinkExtension(node);
					if (links != null) {
						URI oldlink = links.getHyperLink();
						links.setHyperLink(null);
						if(oldlink != null) {
							oldlinkMap.put(node, oldlink);
							Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.HYPERLINK_CHANGED, oldlink, null);
						}
					}
				}
			}

			public String getDescription() {
				return "nodesRemoveLink";
			}

			public void undo() {
				for(Entry<NodeModel, URI> entry : oldlinkMap.entrySet()) {
					final NodeModel node = entry.getKey();
					final NodeLinks links = NodeLinks.getLinkExtension(node);
					links.setHyperLink(entry.getValue());
					Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.HYPERLINK_CHANGED, null, entry.getValue());
				}
			}
		};
		return actor;
	}
}
