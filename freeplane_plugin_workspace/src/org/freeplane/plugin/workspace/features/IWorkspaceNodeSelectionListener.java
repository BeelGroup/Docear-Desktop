package org.freeplane.plugin.workspace.features;

import java.util.EventListener;

import javax.swing.event.TreeSelectionEvent;

public interface IWorkspaceNodeSelectionListener extends EventListener {
	public void selectionChanged(TreeSelectionEvent event);

}
