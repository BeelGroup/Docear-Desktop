package org.freeplane.core.ui.ribbon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.ribbon.RibbonSeparatorContributorFactory.RibbonSeparator;
import org.freeplane.core.ui.ribbon.StructureTree.StructurePath;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class RibbonActionContributorFactory implements IRibbonContributorFactory {

	public static final String ACTION_KEY_PROPERTY = "ACTION_KEY";
	public static final String ACTION_NAME_PROPERTY = "ACTION_NAME";
	private final RibbonBuilder builder;

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RibbonActionContributorFactory(RibbonBuilder builder) {
		this.builder = builder;
	}

	
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
	
	public static JButton createButton(final AFreeplaneAction action) {
		String title = getActionTitle(action);
		ResizableIcon icon = getActionIcon(action);
		
		final JButton button = new JButton(title, icon);
		
//		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		return button;
	}

	public static JCommandButton createCommandButton(final AFreeplaneAction action) {
		String title = getActionTitle(action);
		ResizableIcon icon = getActionIcon(action);
		
		final JCommandButton button = new JCommandButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		return button;
	}
	
	public static JCommandToggleButton createCommandToggleButton(final AFreeplaneAction action) {
		String title = getActionTitle(action);
		ResizableIcon icon = getActionIcon(action);
		
		final JCommandToggleButton button = new JCommandToggleButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		return button;
	}
	
	public static JCommandMenuButton createCommandMenuButton(final AFreeplaneAction action) {
		String title = getActionTitle(action);
		ResizableIcon icon = getActionIcon(action);
		
		final JCommandMenuButton button = new JCommandMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		return button;
	}
	
	public static JCommandToggleMenuButton createCommandToggleMenuButton(final AFreeplaneAction action) {
		String title = getActionTitle(action);
		ResizableIcon icon = getActionIcon(action);
		
		final JCommandToggleMenuButton button = new JCommandToggleMenuButton(title, icon);
		
		updateRichTooltip(button, action, null);
		button.addActionListener(new RibbonActionListener(action));
		return button;
	}
	
	public static void updateRichTooltip(final AbstractCommandButton button, AFreeplaneAction action, KeyStroke ks) {
		RichTooltip tip = null;
		final String tooltip = TextUtils.getRawText(action.getTooltipKey(), null);
		if (tooltip != null && !"".equals(tooltip)) {
			tip = new RichTooltip(getActionTitle(action), tooltip);
		}
		if(ks != null) {
			if(tip == null) {
				tip = new RichTooltip(getActionTitle(action), "  ");
			}
			tip.addFooterSection(formatShortcut(ks));
		}
		if(tip != null) {
			button.setActionRichTooltip(tip);
		}
	}

	public static String formatShortcut(KeyStroke ks) {
		StringBuilder sb = new StringBuilder();
		if(ks != null) {
			String[] st = ks.toString().split("[\\s]+");
			for (String s : st) {
				if("pressed".equals(s.trim())) {
					continue;
				}
				if(sb.length() > 0) {
					sb.append(" + ");
				}
				sb.append(s.substring(0, 1).toUpperCase());
				sb.append(s.substring(1));
			}
		}
		return sb.toString();
	}

	public static ResizableIcon getActionIcon(final AFreeplaneAction action) {
		ResizableIcon icon = null;
		ImageIcon ico = (ImageIcon) action.getValue(Action.SMALL_ICON);
		if(ico != null) {
			icon = ImageWrapperResizableIcon.getIcon(ico.getImage(), new Dimension(ico.getIconWidth(), ico.getIconHeight()));
		}
		else {
			String resource = ResourceController.getResourceController().getProperty(action.getIconKey(), null);
			if (resource != null) {
				URL location = ResourceController.getResourceController().getResource(resource);
				icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(16, 16));
			}
		}
		return icon;
	}

	public static String getActionTitle(final AFreeplaneAction action) {
		String title = TextUtils.getText(action.getTextKey());
		if(title == null || title.isEmpty()) {
			title = action.getTextKey();
		}
		return title;
	}
	
	public static AFreeplaneAction getDummyAction(final String key) {
		return new AFreeplaneAction("ribbon.action."+key) {
			private static final long serialVersionUID = -5405032373977903024L;

			public void actionPerformed(ActionEvent e) {
				//RIBBONS - do nothing
			}
		};
	}

	private ActionAcceleratorChangeListener changeListener;
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		final String actionKey = attributes.getProperty("action");
			if(actionKey != null) {
			String accel = attributes.getProperty("accelerator", null);
			if (accel != null) {
				if (Compat.isMacOsX()) {
					accel = accel.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
				}
				builder.getAcceleratorManager().setDefaultAccelerator(actionKey, accel);
			}
		}
		return new ARibbonContributor() {
			
			private List<Component> childButtons = new ArrayList<Component>();

			public String getKey() {
				String key = attributes.getProperty("action");
				if(key == null) {
					key = attributes.getProperty("name");
				}
				return key;
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				final String actionKey = attributes.getProperty("action");
				if(actionKey != null) {
					AFreeplaneAction action = context.getBuilder().getMode().getAction(actionKey);
					if(action != null) {
						final JCommandButton button = createCommandButton(action);
						button.putClientProperty(ACTION_KEY_PROPERTY, action);
						
						KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(actionKey);
						if(ks != null) {
							updateRichTooltip(button, action, ks);
						}
						getAccelChangeListener().addAction(actionKey, button);
						context.getBuilder().getAcceleratorManager().addAcceleratorChangeListener(getAccelChangeListener());
						if(context.hasChildren(context.getCurrentPath())) {
							StructurePath path = context.getCurrentPath();
							button.setCommandButtonKind(CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
							button.setPopupCallback(getPopupPanelCallBack(path, context));
						}
						
						parent.addChild(button, getPriority(attributes.getProperty("priority", "medium")));
					}
				}
				else {
					final String name = attributes.getProperty("name");
					if(name != null) {
						AFreeplaneAction action = getDummyAction(name);
						final JCommandButton button = new JCommandButton(getActionTitle(action), getActionIcon(action));
						button.putClientProperty(ACTION_NAME_PROPERTY, action);
						updateRichTooltip(button, action, null);
						if(context.hasChildren(context.getCurrentPath())) {
							StructurePath path = context.getCurrentPath();
							button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
							button.setPopupCallback(getPopupPanelCallBack(path, context));
						}
						parent.addChild(button, getPriority(attributes.getProperty("priority", "medium")));
					}
				}
			}
			
			private PopupPanelCallback getPopupPanelCallBack(StructurePath path, final RibbonBuildContext context) {
				childButtons.clear();
				context.processChildren(path, this);
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
								menuButton.setEnabled(button.isEnabled());
								
								for (ActionListener listener : button.getListeners(ActionListener.class)) {
									if(listener instanceof RibbonActionListener) {
										menuButton.addActionListener(listener);
									}
								}
								AFreeplaneAction action = (AFreeplaneAction)button.getClientProperty(ACTION_KEY_PROPERTY);
								if(action != null) {
									menuButton.putClientProperty(ACTION_KEY_PROPERTY, action);
									updateRichTooltip(menuButton, action, context.getBuilder().getAcceleratorManager().getAccelerator(actionKey));
									if(AFreeplaneAction.checkEnabledOnChange(action)) {
										action.setEnabled();
										menuButton.setEnabled(action.isEnabled());
									}
									if(AFreeplaneAction.checkSelectionOnChange(action)||AFreeplaneAction.checkSelectionOnPopup(action)|| AFreeplaneAction.checkSelectionOnPropertyChange(action)) {
										action.setSelected();
										menuButton.getActionModel().setSelected(action.isSelected());
									}
								}
								else {
									action = (AFreeplaneAction)button.getClientProperty(ACTION_NAME_PROPERTY);
									if(action != null) {
										menuButton.putClientProperty(ACTION_NAME_PROPERTY, action);
										updateRichTooltip(menuButton, action, null);
									}
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
		private final RibbonBuilder builder;

		protected RibbonActionListener(AFreeplaneAction action) {
			this.key = action.getKey();
			this.builder = Controller.getCurrentModeController().getUserInputListenerFactory().getRibbonBuilder();
		}

		public void actionPerformed(ActionEvent e) {
			AFreeplaneAction action = Controller.getCurrentModeController().getAction(key);
			if(action == null) {
				return;
			}
			if ((0 != (e.getModifiers() & ActionEvent.CTRL_MASK))/*
			        && source instanceof IKeyBindingManager && !((IKeyBindingManager) source).isKeyBindingProcessed()/**/) {
				builder.getAcceleratorManager().newAccelerator(action, null);				
				return;
			}
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
				updateRichTooltip(button, action, newStroke);
			}

		}
	}
}
