package org.docear.plugin.core.mindmap;

import java.io.File;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.url.UrlManager;

public class MindmapLinkTypeUpdater extends AMindmapUpdater {

	public MindmapLinkTypeUpdater(String title) {
		super(title);		
	}

	public boolean updateMindmap(MapModel map) {
		return updateNodesRecursive(map.getRootNode());
	}
	
	private boolean updateMindmap(NodeModel node) {
		try {
    		NodeLinks links = NodeLinks.getLinkExtension(node);
    
    		if (links == null || links.getHyperLink() == null) {
    			return false;
    		}    
    		
    		File file = UrlManager.getController().getAbsoluteFile(node.getMap(), links.getHyperLink());
    		if (file != null) {
    			links.setHyperLink(LinkController.toLinkTypeDependantURI(node.getMap().getFile(), file));
    		}
    
    		return true;
		}
		catch(Exception e) {
			LogUtils.warn(this.getClass().getName()+".updateMindmap(): "+ e.getMessage());
		}
		return false;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private boolean updateNodesRecursive(NodeModel node) {
		boolean changes = false;
		for(NodeModel child : node.getChildren()) {
			changes = changes | updateNodesRecursive(child);
		}
		changes = changes | updateMindmap(node);
		return changes;
	}

}
