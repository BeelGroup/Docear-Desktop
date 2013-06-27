package org.freeplane.core.ui.ribbon.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeViewTypeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.mindmapmode.SetBooleanMapPropertyAction;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class ViewSettingsContributorFactory implements IRibbonContributorFactory {	

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}				
				JRibbonBand band = new JRibbonBand(TextUtils.getText("ribbon.band.viewsettings"), null, null);
								
				JCommandButton button = new JCommandButton(TextUtils.getText("menu_displayAttributes"));
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						
						final AttributeViewTypeAction showSelectedAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("ShowSelectedAttributesAction");
						final JCommandToggleMenuButton showSelectedAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showSelectedAttributesAction);							
						popupmenu.addMenuButton(showSelectedAttributesButton);
						showSelectedAttributesAction.setSelected();
						showSelectedAttributesButton.getActionModel().setSelected(showSelectedAttributesAction.isSelected());
						
						final AttributeViewTypeAction showAllAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("ShowAllAttributesAction");
						final JCommandToggleMenuButton showAllAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showAllAttributesAction);						
						popupmenu.addMenuButton(showAllAttributesButton);
						showAllAttributesAction.setSelected();
						showAllAttributesButton.getActionModel().setSelected(showAllAttributesAction.isSelected());
						
						final AttributeViewTypeAction hideAllAttributesAction = (AttributeViewTypeAction) context.getBuilder().getMode().getAction("HideAllAttributesAction");						
						final JCommandToggleMenuButton hideAllAttributesButton = RibbonActionContributorFactory.createCommandToggleMenuButton(hideAllAttributesAction);						
						popupmenu.addMenuButton(hideAllAttributesButton);
						hideAllAttributesAction.setSelected();
						hideAllAttributesButton.getActionModel().setSelected(hideAllAttributesAction.isSelected());
												
    					final SetBooleanMapPropertyAction showIconAction = (SetBooleanMapPropertyAction) context.getBuilder().getMode().getAction("SetBooleanMapPropertyAction.show_icon_for_attributes");
    					final JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(showIconAction);
    					showIconAction.setSelected();
    					toggleButton.getActionModel().setSelected(showIconAction.isSelected());
    					popupmenu.addMenuButton(toggleButton);
						
						context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {							
							public void updateState(NodeModel node) {
								showSelectedAttributesAction.setSelected();
								showSelectedAttributesButton.getActionModel().setSelected(showSelectedAttributesAction.isSelected());
								showAllAttributesAction.setSelected();
								showAllAttributesButton.getActionModel().setSelected(showAllAttributesAction.isSelected());
								hideAllAttributesAction.setSelected();
								hideAllAttributesButton.getActionModel().setSelected(hideAllAttributesAction.isSelected());
								showIconAction.setSelected();
								toggleButton.getActionModel().setSelected(showIconAction.isSelected());
							}
						});
						return popupmenu;
					}
				});
				band.addCommandButton(button, RibbonElementPriority.TOP);
								
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.None(band.getControlPanel()));				
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
