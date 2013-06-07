package org.docear.plugin.services.features.user.action;

import java.awt.event.ActionEvent;

import org.docear.plugin.core.ui.wizard.Wizard;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.actions.AWorkspaceAction;

public class DocearUserServicesAction extends AWorkspaceAction {
	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "workspace.action.user.services";
	
	public DocearUserServicesAction() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent event) {
		final Wizard wiz = new Wizard(UITools.getFrame());
		initWizard(wiz);
		
	}
	
	private void initWizard(Wizard wizard) {
	}
}
