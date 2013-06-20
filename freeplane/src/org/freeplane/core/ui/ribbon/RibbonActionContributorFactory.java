package org.freeplane.core.ui.ribbon;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class RibbonActionContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static RibbonElementPriority getPriority(String attr) {
		RibbonElementPriority prio = RibbonElementPriority.MEDIUM;
		if("top".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.TOP;
		}
		else if("low".equals(attr.trim().toLowerCase())) {
			prio = RibbonElementPriority.LOW;
		}
		return prio;
	}

	public static JCommandButton createCommandButton(final String key) {
		String title = TextUtils.getText(key+".text");
		if(title == null || title.isEmpty()) {
			title = key;
		}
		String resource = ResourceController.getResourceController().getProperty(key+".icon", null);
		ResizableIcon icon = null;
		if (resource != null) {
			URL location = ResourceController.getResourceController().getResource(resource);
			icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(16, 16));
		}
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		final String tooltip = TextUtils.getRawText(key+ ".tooltip", null);
		if (tooltip != null && !"".equals(tooltip)) {
			button.setToolTipText(tooltip);//setActionRichTooltip(new RichTooltip(tooltip, ""));
		}
		button.addActionListener(new RibbonActionListener(key));
		return button;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			
			private List<AbstractCommandButton> childButtons = new ArrayList<AbstractCommandButton>();

			public String getKey() {
				return attributes.getProperty("action");
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				final String key = attributes.getProperty("action");
				if(key != null) {
					final JCommandButton button = createCommandButton(key);
					
					String pathKey = (String) structure.getKeyByUserObject(this);
					IndexedTree.Node n = (Node) structure.get(pathKey);
					if(n.getChildCount() > 0) {
						button.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
						button.setPopupCallback(getPopupPanelCallBack(n, structure));
					}
					
					parent.addChild(button, getPriority(attributes.getProperty("priority", "medium")));
				}
			}
			
			private PopupPanelCallback getPopupPanelCallBack(Node n, IndexedTree structure) {
				childButtons.clear();
				Enumeration<?> children = n.children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(structure, this);
				}
				return new PopupPanelCallback() {
					
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						for (AbstractCommandButton button : childButtons) {
							JCommandMenuButton menuButton = new JCommandMenuButton(button.getText(), button.getIcon());
							for (ActionListener listener : button.getListeners(ActionListener.class)) {
								if(listener instanceof RibbonActionListener) {
									menuButton.addActionListener(listener);
								}
							}
							if(button.getToolTipText() != null) {
								menuButton.setToolTipText(button.getToolTipText());
							}
							popupmenu.addMenuButton(menuButton);
						}
						return popupmenu;
					}
				};
			}

			public void addChild(Object child, Object properties) {
				if(child instanceof AbstractCommandButton) {
					childButtons.add((AbstractCommandButton) child);
				}
				
			}
		};
	}
	
	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
	
	protected static class RibbonActionListener implements ActionListener {
		private final String key;

		protected RibbonActionListener(String key) {
			this.key = key;
		}

		public void actionPerformed(ActionEvent e) {
			AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
			if(action == null) {
				return;
			}
			action.actionPerformed(e);
		}
	}
}
