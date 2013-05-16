package org.docear.plugin.core.ui.wizard;

import java.awt.Component;

public class WizardPanelDescriptor {

	private Component targetPanel;
	private Object panelIdentifier;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public final Component getPanelComponent() {
		return targetPanel;
	}

	public final void setPanelComponent(Component panel) {
		targetPanel = panel;
	}

	public final Object getPanelDescriptorIdentifier() {
		return panelIdentifier;
	}

	public final void setPanelDescriptorIdentifier(Object id) {
		panelIdentifier = id;
	}

	public Object getNextPanelDescriptor() {
		return null;
	}

	public Object getBackPanelDescriptor() {
		return null;
	}

	public void aboutToDisplayPanel() {

		// Place code here that will be executed before the
		// panel is displayed.

	}

	public void displayingPanel() {

		// Place code here that will be executed when the
		// panel is displayed.

	}

	public void aboutToHidePanel() {

		// Place code here that will be executed when the
		// panel is hidden.

	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
