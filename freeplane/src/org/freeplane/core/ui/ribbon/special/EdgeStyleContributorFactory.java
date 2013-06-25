package org.freeplane.core.ui.ribbon.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ButtonGroup;

import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
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

public class EdgeStyleContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				try {
					JRibbonBand band = new JRibbonBand(TextUtils.getText("ribbon.band.edgeStyles"), null, null);
					band.setExpandButtonKeyTip("ES");
					band.setCollapsedStateKeyTip("ZE");
					MUIFactory uiFactory = Controller.getCurrentModeController().getExtension(MUIFactory.class);
					
					JCommandButton styleGroupButton = new JCommandButton(TextUtils.getText("edgeStyleGroupAction.text"));
					styleGroupButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);

					final JCommandToggleMenuButton styleAsParent = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAsParentAction");
					final JCommandToggleMenuButton styleLinear = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.linear");
					final JCommandToggleMenuButton styleBezier = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.bezier");
					final JCommandToggleMenuButton styleSharpLinear = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.sharp_linear");
					final JCommandToggleMenuButton styleSharpBezier = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.sharp_bezier");
					final JCommandToggleMenuButton styleHorizontal = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.horizontal");
					final JCommandToggleMenuButton styleHideEdge = RibbonActionContributorFactory.createCommandToggleMenuButton("EdgeStyleAction.hide_edge");
					
					ButtonGroup group = new ButtonGroup();
					styleAsParent.getActionModel().setGroup(group);
					styleLinear.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);
					styleBezier.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);
					styleSharpLinear.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);
					styleSharpBezier.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);
					styleHorizontal.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);
					styleHideEdge.getActionModel().setGroup(group);styleAsParent.getActionModel().setGroup(group);					

					styleGroupButton.setPopupCallback(new PopupPanelCallback() {						
						public JPopupPanel getPopupPanel(JCommandButton commandButton) {
							JCommandPopupMenu popupmenu = new JCommandPopupMenu();
							popupmenu.addMenuButton(styleAsParent);
							popupmenu.addMenuButton(styleLinear);
							popupmenu.addMenuButton(styleBezier);
							popupmenu.addMenuButton(styleSharpLinear);
							popupmenu.addMenuButton(styleSharpBezier);
							popupmenu.addMenuButton(styleHorizontal);
							popupmenu.addMenuButton(styleHideEdge);
							return popupmenu;
						}
					});
										
					band.addCommandButton(styleGroupButton, RibbonElementPriority.TOP);

					// final Container sizeBox = uiFactory.createSizeBox();
					// JRibbonComponent sizeComboWrapper = new
					// JRibbonComponent((JComponent) sizeBox);
					// sizeComboWrapper.setKeyTip("SS");
					// band.addFlowComponent(sizeComboWrapper);
					//
					// final Container styleBox = uiFactory.createStyleBox();
					// final Dimension preferredSize =
					// styleBox.getPreferredSize();
					// preferredSize.width = 90;
					// styleBox.setPreferredSize(preferredSize);
					// JRibbonComponent styleComboWrapper = new
					// JRibbonComponent((JComponent) styleBox);
					// styleComboWrapper.setKeyTip("SD");
					// band.addFlowComponent(styleComboWrapper);
					//
					// JCommandButtonStrip styleStrip = new
					// JCommandButtonStrip();
					//
					// //
					// styleStrip.add(RibbonActionContributorFactory.createCommandButton("BoldAction"));
					// styleStrip.add(RibbonActionContributorFactory.createCommandButton("ItalicAction"));
					// styleStrip.add(RibbonActionContributorFactory.createCommandButton("NodeColorAction"));
					// styleStrip.add(RibbonActionContributorFactory.createCommandButton("NodeBackgroundColorAction"));
					//
					// band.addFlowComponent(styleStrip);

					List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();
					// policies.add(new
					// CoreRibbonResizePolicies.None(mindMapBand.getControlPanel()));
					policies.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));
//					policies.add(new CoreRibbonResizePolicies.High2Mid(band.getControlPanel()));
					band.setResizePolicies(policies);
					parent.addChild(band, null);
				}
				finally {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}

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
