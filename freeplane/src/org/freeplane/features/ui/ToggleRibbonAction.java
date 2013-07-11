package org.freeplane.features.ui;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.ribbon.RibbonBuilder;
import org.freeplane.features.mode.Controller;

@SelectableAction(checkOnPopup = true)
public class ToggleRibbonAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToggleRibbonAction() {
		super("ToggleRibbonAction");
	}
	
	public void actionPerformed(ActionEvent e) {
		RibbonBuilder ribbonBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getRibbonBuilder();
		if (ribbonBuilder != null) {
			ribbonBuilder.setMinimized(!ribbonBuilder.isMinimized());
		}
	}
	
	@Override
	public void setSelected() {
		RibbonBuilder ribbonBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getRibbonBuilder();
		if (ribbonBuilder != null) {
			setSelected(ribbonBuilder.isMinimized());
		}
	}	

}
