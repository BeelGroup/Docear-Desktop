package org.freeplane.core.ui.ribbon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.ribbon.RibbonSeparatorContributorFactory.RibbonSeparator;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
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
		String title = getActionTitle(key);
		ResizableIcon icon = getActionIcon(key);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		updateRichTooltip(button, key, null);
		button.addActionListener(new RibbonActionListener(key));
		return button;
	}
	
	public static void updateRichTooltip(final JCommandButton button, String key, KeyStroke ks) {
		RichTooltip tip = null;
		final String tooltip = TextUtils.getRawText(key+ ".tooltip", null);
		if (tooltip != null && !"".equals(tooltip)) {
			tip = new RichTooltip(getActionTitle(key), tooltip);
			if(ks != null) {
				tip.addFooterSection(ks.toString());
			}
			button.setActionRichTooltip(tip);
		}
	}

	public static ResizableIcon getActionIcon(final String key) {
		String resource = ResourceController.getResourceController().getProperty(key+".icon", null);
		ResizableIcon icon = null;
		if (resource != null) {
			URL location = ResourceController.getResourceController().getResource(resource);
			icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(16, 16));
		}
		return icon;
	}

	public static String getActionTitle(final String key) {
		String title = TextUtils.getText(key+".text");
		if(title == null || title.isEmpty()) {
			title = key;
		}
		return title;
	}

	private ActionAcceleratorChangeListener changeListener;
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			
			private List<Component> childButtons = new ArrayList<Component>();

			public String getKey() {
				return attributes.getProperty("action");
			}
			
			public void contribute(RibbonBuildContext context, IRibbonContributor parent) {
				final String actionKey = attributes.getProperty("action");
				if(actionKey != null) {
					final JCommandButton button = createCommandButton(actionKey);
					
					String accel = attributes.getProperty("accelerator", null);
					if (accel != null) {
						if (Compat.isMacOsX()) {
							accel = accel.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
						}
						KeyStroke ks = KeyStroke.getKeyStroke(accel);
						context.getBuilder().getAcceleratorManager().setDefaultAccelerator(actionKey, accel);
//						KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(actionKey);
						if(ks != null) {
							updateRichTooltip(button, actionKey, ks);
						}
					}
					
					getAccelChangeListener().addAction(actionKey, button);
					context.getBuilder().getAcceleratorManager().addAcceleratorChangeListener(getAccelChangeListener());
					IndexedTree.Node n = context.getStructureNode(this);
					if(n.getChildCount() > 0) {
						button.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
						button.setPopupCallback(getPopupPanelCallBack(n, context));
					}
					
					parent.addChild(button, getPriority(attributes.getProperty("priority", "medium")));
				}
				else {
					final String name = attributes.getProperty("name");
					if(name != null) {
						final JCommandButton button = new JCommandButton(getActionTitle(name), getActionIcon(name));
						updateRichTooltip(button, name, null);
						IndexedTree.Node n = context.getStructureNode(this);
						if(n.getChildCount() > 0) {
							button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
							button.setPopupCallback(getPopupPanelCallBack(n, context));
						}
						parent.addChild(button, getPriority(attributes.getProperty("priority", "medium")));
					}
				}
			}
			
			private PopupPanelCallback getPopupPanelCallBack(Node n, RibbonBuildContext context) {
				childButtons.clear();
				Enumeration<?> children = n.children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(context, this);
				}
				return new PopupPanelCallback() {
					
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						for (Component comp : childButtons) {
							if(comp instanceof JSeparator) {
								popupmenu.addMenuSeparator();
							}
							else if(comp instanceof AbstractCommandButton) {
								AbstractCommandButton button = (AbstractCommandButton) comp;
								JCommandMenuButton menuButton = new JCommandMenuButton(button.getText(), button.getIcon());
								for (ActionListener listener : button.getListeners(ActionListener.class)) {
									if(listener instanceof RibbonActionListener) {
										menuButton.addActionListener(listener);
									}
								}
								if(button.getToolTipText() != null) {
									menuButton.setToolTipText(button.getToolTipText());
								}
								if(button instanceof JCommandButton) {
									if(((JCommandButton) button).getPopupCallback() != null) {
										menuButton.setCommandButtonKind(((JCommandButton) button).getCommandButtonKind());
										menuButton.setPopupCallback(((JCommandButton) button).getPopupCallback());
									}
								}
								popupmenu.addMenuButton(menuButton);
							}
						}
						return popupmenu;
					}
				};
			}

			public void addChild(Object child, Object properties) {
				if(child instanceof AbstractCommandButton) {
					childButtons.add((AbstractCommandButton) child);
				}
				if(child instanceof RibbonSeparator) {
					childButtons.add(new JSeparator(JSeparator.HORIZONTAL));
				}
				
			}
		};
	}
	
	protected ActionAcceleratorChangeListener getAccelChangeListener() {
		if(changeListener == null) {
			changeListener = new ActionAcceleratorChangeListener();
		}
		return changeListener;
	}

	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
	
	public static class RibbonActionListener implements ActionListener {
		private final String key;

		protected RibbonActionListener(String key) {
			this.key = key;
		}

		public void actionPerformed(ActionEvent e) {
			AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
			if(action == null) {
				return;
			}
//			final Object source = e.getSource();
//			if ((0 != (e.getModifiers() & ActionEvent.CTRL_MASK))
//			        && source instanceof IKeyBindingManager && !((IKeyBindingManager) source).isKeyBindingProcessed()
//			        && source instanceof JMenuItem) {
//				final JMenuItem item = (JMenuItem) source;
//				newAccelerator(item, null);
//				return;
//			}
			action.actionPerformed(e);
		}
		
		
	}
	
	public static class ActionAcceleratorChangeListener implements IAcceleratorChangeListener {
		private final Map<String, JCommandButton> actionMap = new HashMap<String, JCommandButton>();
		
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public void addAction(String actionKey, JCommandButton button) {
			actionMap.put(actionKey, button);
		}
		
		public void clear() {
			actionMap.clear();
		}
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
		public void acceleratorChanged(JMenuItem action, KeyStroke oldStroke, KeyStroke newStroke) {
			
		}
		
		public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
			JCommandButton button = actionMap.get(action.getKey()); 
			if(button != null) {
				updateRichTooltip(button, action.getKey(), newStroke);
			}

		}
	}
}
