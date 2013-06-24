package org.freeplane.core.ui.ribbon.special;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Properties;

import javax.swing.JComponent;

import org.freeplane.core.ui.ribbon.IRibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;

public class FontStyleContributorFactory implements IRibbonContributorFactory {

	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(RibbonBuildContext context, IRibbonContributor parent) {
				if (parent == null) {
					return;
				}
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				try {
					// RIBBONS expandlistener and icon
					JFlowRibbonBand fontBand = new JFlowRibbonBand("Font", null, null);
					fontBand.setExpandButtonKeyTip("FN");
					fontBand.setCollapsedStateKeyTip("ZF");

					
					MUIFactory uiFactory = Controller.getCurrentModeController().getExtension(MUIFactory.class);
					
					final Container fontBox = uiFactory.createFontBox();
//					final Dimension preferredSize = fontBox.getPreferredSize();
//					preferredSize.width = 150;
//					fontBox.setPreferredSize(preferredSize);					
					JRibbonComponent fontComboWrapper = new JRibbonComponent((JComponent) fontBox);
					fontComboWrapper.setKeyTip("SF");
					fontBand.addFlowComponent(fontComboWrapper);
					
					final Container sizeBox = uiFactory.createSizeBox();
					JRibbonComponent sizeComboWrapper = new JRibbonComponent((JComponent) sizeBox);
					sizeComboWrapper.setKeyTip("SS");
					fontBand.addFlowComponent(sizeComboWrapper);
					
					final Container styleBox = uiFactory.createStyleBox();
					final Dimension preferredSize = styleBox.getPreferredSize();
					preferredSize.width = 90;
					styleBox.setPreferredSize(preferredSize);
					JRibbonComponent styleComboWrapper = new JRibbonComponent((JComponent) styleBox);
					styleComboWrapper.setKeyTip("SD");
					fontBand.addFlowComponent(styleComboWrapper);
					
					JCommandButtonStrip styleStrip = new JCommandButtonStrip();
					
//					styleBoldButton.setActionRichTooltip(new RichTooltip(TextUtils.getRawText(action.getTooltipKey()), "makes the node text bold"));
//					styleBoldButton.setActionKeyTip("1");
					styleStrip.add(RibbonActionContributorFactory.createCommandButton("BoldAction"));
					styleStrip.add(RibbonActionContributorFactory.createCommandButton("ItalicAction"));
					styleStrip.add(RibbonActionContributorFactory.createCommandButton("NodeColorAction"));
					styleStrip.add(RibbonActionContributorFactory.createCommandButton("NodeBackgroundColorAction"));
					
					// JCommandToggleButton styleItalicButton = new
					// JCommandToggleButton("", new format_text_italic());
					// styleItalicButton.setActionRichTooltip(new
					// RichTooltip("Italic", "makes the node text italic"));
					// styleItalicButton.setActionKeyTip("2");
					// styleStrip.add(styleItalicButton);
					//
					// JCommandToggleButton styleUnderlineButton = new
					// JCommandToggleButton("", new format_text_underline());
					// // styleUnderlineButton.setActionRichTooltip(new
					// RichTooltip(resourceBundle.getString("FontUnderline.tooltip.textActionTitle"),
					// resourceBundle
					// //
					// .getString("FontUnderline.tooltip.textActionParagraph1")));
					// // styleUnderlineButton.setActionKeyTip("3");
					// styleStrip.add(styleUnderlineButton);
					//
					// JCommandToggleButton styleStrikeThroughButton = new
					// JCommandToggleButton("", new
					// format_text_strikethrough());
					// // styleStrikeThroughButton.setActionRichTooltip(new
					// RichTooltip(resourceBundle.getString("FontStrikethrough.tooltip.textActionTitle"),
					// resourceBundle
					// //
					// .getString("FontStrikethrough.tooltip.textActionParagraph1")));
					// // styleStrikeThroughButton.setActionKeyTip("4");
					// // styleStrip.add(styleStrikeThroughButton);

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
