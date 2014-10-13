package org.docear.plugin.services.features.documentretrieval.workspace;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.services.ServiceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;

public class DownloadFolderNode extends FolderLinkNode implements TreeExpansionListener {
	private static final long serialVersionUID = 2295413841014945798L;
	private final Icon FOLDER_DOWNLOADS_ICON = new ImageIcon(ServiceController.class.getResource("/icons/folder-download.png"));
	private boolean firstExpand = true;

	@Override
	public String getTagName() {
		//don't write this node into the ws config
		return null;
	}

	@Override
	public String getName() {
		//always show the localized node name
		return TextUtils.getText("docear.node.downloads");
	}

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(FOLDER_DOWNLOADS_ICON);
		renderer.setClosedIcon(FOLDER_DOWNLOADS_ICON);
		renderer.setLeafIcon(FOLDER_DOWNLOADS_ICON);
		return true;
	}

	public boolean isSystem() {
		return true;
	}

	public void treeExpanded(TreeExpansionEvent event) {
		if(firstExpand) {
			firstExpand  = false;
			this.refresh();
		}
	}

	public void treeCollapsed(TreeExpansionEvent event) {
	}
}