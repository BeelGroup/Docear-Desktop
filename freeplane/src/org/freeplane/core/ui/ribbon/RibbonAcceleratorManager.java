package org.freeplane.core.ui.ribbon;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.IKeyStrokeProcessor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class RibbonAcceleratorManager implements IKeyStrokeProcessor, IAcceleratorChangeListener {
	
	private static final String SHORTCUT_PROPERTY_PREFIX = "acceleratorFor";
	
	private final Map<KeyStroke, AFreeplaneAction> accelerators = new HashMap<KeyStroke, AFreeplaneAction>();
	private final Map<String, KeyStroke> actionMap = new HashMap<String, KeyStroke>();
	private final List<IAcceleratorChangeListener> changeListeners = new ArrayList<IAcceleratorChangeListener>();
	
	private final RibbonBuilder builder;
	private IAcceleratorChangeListener acceleratorChangeListener;

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
 	public RibbonAcceleratorManager(RibbonBuilder ribbonBuilder) {
 		this.builder = ribbonBuilder;
 	}
 	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

 	public void setAccelerator(final AFreeplaneAction action, final KeyStroke keyStroke) {
		final AFreeplaneAction oldAction = accelerators.put(keyStroke, action);
		if (keyStroke != null && oldAction != null) {
			UITools.errorMessage(TextUtils.format("action_keystroke_in_use_error", keyStroke, getActionTitle(action.getKey()), getActionTitle(oldAction.getKey())));
			accelerators.put(keyStroke, oldAction);
			final String shortcutKey = getPropertyKey(action.getKey());
			
			ResourceController.getResourceController().setProperty(shortcutKey, "");
			return;
		}
		final KeyStroke removedAccelerator = removeAccelerator(action);
		actionMap.put(action.getKey(), keyStroke);
		if (acceleratorChangeListener != null && (removedAccelerator != null || keyStroke != null)) {
			//acceleratorChangeListener.acceleratorChanged(item, removedAccelerator, keyStroke);
			acceleratorChangeListener.acceleratorChanged(action, removedAccelerator, keyStroke);
		}
	}
 	
 	private String getActionTitle(String key) {
 		String title = TextUtils.getText(key+".text");
		if(title == null || title.isEmpty()) {
			title = key;
		}
		return title;
 	}
 	
 	public void setDefaultAccelerator(final String itemKey, final String accelerator) {
		final String shortcutKey = getPropertyKey(itemKey);
		if (null == ResourceController.getResourceController().getProperty(shortcutKey, null)) {
			ResourceController.getResourceController().setDefaultProperty(shortcutKey, accelerator);
		}
	}
 	
 	public KeyStroke removeAccelerator(final AFreeplaneAction action) throws AssertionError {
		final KeyStroke oldAccelerator = actionMap.get(action.getKey());
		if (oldAccelerator != null) {
			final AFreeplaneAction oldAction = accelerators.remove(oldAccelerator);
			if (!action.equals(oldAction)) {
				throw new AssertionError("unexpected action " + "for accelerator " + oldAccelerator);
			}
		}
		return oldAccelerator;
	}
 	
 	public void setAcceleratorChangeListener(final IAcceleratorChangeListener acceleratorChangeListener) {
		this.acceleratorChangeListener = acceleratorChangeListener;
	}
 	
 	public String getPropertyKey(final String key) {
		return SHORTCUT_PROPERTY_PREFIX +"."+ builder.getMode().getModeName().toLowerCase() + "." + key;
	}
 	
 	public KeyStroke getAccelerator(String key) {
 		return actionMap.get(key);
 	}
 	
 	public void addAcceleratorChangeListener(IAcceleratorChangeListener changeListener) {
		synchronized (changeListeners) {
			if(!changeListeners.contains(changeListener)) {
				changeListeners.add(changeListener);
			}
		}
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public boolean processKeyBinding(KeyStroke ks, KeyEvent event, int condition, boolean pressed, boolean consumed) {
		if (!consumed && condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
			AFreeplaneAction action = accelerators.get(ks);
			if(action != null) {
				if(action != null && SwingUtilities.notifyAction(action, ks, event, event.getComponent(), event.getModifiers())) {
					return true;
				}
			}
		}
		return false;
	}

	public void acceleratorChanged(JMenuItem action, KeyStroke oldStroke, KeyStroke newStroke) {
		// TODO Auto-generated method stub
		
	}

	public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
		KeyStroke ks = actionMap.put(action.getKey(), newStroke);
		if(ks != null) {
			accelerators.remove(ks);
		}		
		accelerators.put(newStroke, action);
	}
}
