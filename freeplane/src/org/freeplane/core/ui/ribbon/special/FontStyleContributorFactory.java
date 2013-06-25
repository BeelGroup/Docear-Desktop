package org.freeplane.core.ui.ribbon.special;

import java.awt.Container;
import java.util.Properties;

import javax.swing.JComponent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;

public class FontStyleContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				try {
					// RIBBONS expandlistener and icon
					JFlowRibbonBand fontBand = new JFlowRibbonBand(TextUtils.getText("ribbon.band.font"), null, null);
					fontBand.setExpandButtonKeyTip("FN");
					fontBand.setCollapsedStateKeyTip("ZF");

					
					MUIFactory uiFactory = Controller.getCurrentModeController().getExtension(MUIFactory.class);
					
					final Container fontBox = uiFactory.createFontBox();					
					JRibbonComponent fontComboWrapper = new JRibbonComponent((JComponent) fontBox);
					fontComboWrapper.setKeyTip("SF");
					fontBand.addFlowComponent(fontComboWrapper);
					
					final Container sizeBox = uiFactory.createSizeBox();
					JRibbonComponent sizeComboWrapper = new JRibbonComponent((JComponent) sizeBox);
					sizeComboWrapper.setKeyTip("SS");
					fontBand.addFlowComponent(sizeComboWrapper);
					
					final Container styleBox = uiFactory.createStyleBox();
					JRibbonComponent styleComboWrapper = new JRibbonComponent((JComponent) styleBox);
					styleComboWrapper.setKeyTip("SD");
					fontBand.addFlowComponent(styleComboWrapper);
					
					JCommandButtonStrip styleStrip = new JCommandButtonStrip();
					
					AFreeplaneAction action = context.getBuilder().getMode().getAction("BoldAction");
					final JCommandToggleButton boldButton = RibbonActionContributorFactory.createCommandToggleButton(action);
					addDefaultToggleHandler(context, action, boldButton);					
					styleStrip.add(boldButton);
					
					action = context.getBuilder().getMode().getAction("ItalicAction");
					final JCommandToggleButton italicButton = RibbonActionContributorFactory.createCommandToggleButton(action);
					addDefaultToggleHandler(context, action, italicButton);					
					styleStrip.add(italicButton);
					
					action = context.getBuilder().getMode().getAction("NodeColorAction");
					styleStrip.add(RibbonActionContributorFactory.createCommandButton(action));
					action = context.getBuilder().getMode().getAction("NodeBackgroundColorAction");
					styleStrip.add(RibbonActionContributorFactory.createCommandButton(action));
					
					fontBand.addFlowComponent(styleStrip);

					parent.addChild(fontBand, null);
				}
				finally {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}

			}

			public void addChild(Object child, Object properties) {
			}
		};
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final AFreeplaneAction action, final JCommandToggleButton button) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(NodeModel node) {
				if(AFreeplaneAction.checkSelectionOnChange(action)) {
					action.setSelected();
					button.getActionModel().setSelected(action.isSelected());
				}
			}
		});
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
