package org.freeplane.core.ui.ribbon.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapViewController;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class ZoomContributorFactory implements IRibbonContributorFactory {	

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}				
				JFlowRibbonBand band = new JFlowRibbonBand(TextUtils.getText("ribbon.band.font"), null, null);
				
				JComboBox zoomBox = ((MapViewController) Controller.getCurrentController().getMapViewManager()).createZoomBox();				
				band.addFlowComponent(zoomBox);
				
				AFreeplaneAction action = context.getBuilder().getMode().getAction("FitToPage");				
				JCommandButton button = RibbonActionContributorFactory.createCommandButton(action);
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				band.addFlowComponent(button);
				
				action = context.getBuilder().getMode().getAction("CenterSelectedNodeAction");
				button= RibbonActionContributorFactory.createCommandButton(action);
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				button.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						SetBooleanPropertyAction booleanAction = (SetBooleanPropertyAction) context.getBuilder().getMode().getAction("SetBooleanPropertyAction.center_selected_node");
						final JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(booleanAction);
						toggleButton.getActionModel().setSelected(booleanAction.isPropertySet());
						popupmenu.addMenuButton(toggleButton);
						return popupmenu;
					}
				});
				band.addFlowComponent(button);
				
				button = new JCommandButton(TextUtils.getText("menu_viewmode"));
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);				
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
    					JCommandPopupMenu popupmenu = new JCommandPopupMenu();
    					
    					AFreeplaneAction action = context.getBuilder().getMode().getAction("ViewLayoutTypeAction.OUTLINE");
    					JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
    					popupmenu.addMenuButton(toggleButton);
    					
    					action = context.getBuilder().getMode().getAction("ToggleFullScreenAction");    					
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);    					
    					popupmenu.addMenuButton(toggleButton);
    					
    					SetBooleanPropertyAction presentationModeAction = (SetBooleanPropertyAction) context.getBuilder().getMode().getAction("SetBooleanPropertyAction.presentation_mode");    					
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(presentationModeAction);
    					toggleButton.getActionModel().setSelected(presentationModeAction.isPropertySet());
    					popupmenu.addMenuButton(toggleButton);
    					
    					action = context.getBuilder().getMode().getAction("ShowSelectionAsRectangleAction");
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);    					
    					popupmenu.addMenuButton(toggleButton);
    					
    					SetBooleanPropertyAction highlightFormulasAction = (SetBooleanPropertyAction) context.getBuilder().getMode().getAction("SetBooleanPropertyAction.highlight_formulas");
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(highlightFormulasAction);
    					toggleButton.getActionModel().setSelected(highlightFormulasAction.isPropertySet());
    					popupmenu.addMenuButton(toggleButton);
    					
    					return popupmenu;
    				}
				});
				band.addFlowComponent(button);
				
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.FlowThreeRows(band.getControlPanel()));
				policies.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
				band.setResizePolicies(policies);			
				
				parent.addChild(band, null);		    	
			}

			public void addChild(Object child, Object properties) {
			}
		};
	}
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
