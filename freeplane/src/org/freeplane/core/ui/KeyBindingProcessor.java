package org.freeplane.core.ui;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.KeyStroke;

import org.freeplane.core.extension.IExtension;

public class KeyBindingProcessor implements IExtension {
	
	private final HashSet<IKeyStrokeProcessor> processors = new HashSet<IKeyStrokeProcessor>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		synchronized (processors) {
			Iterator<IKeyStrokeProcessor> iter = processors.iterator();
			boolean intercept = false;
			while(iter.hasNext()) { //maybe break after the first interception?
				intercept = iter.next().processKeyBinding(ks, e, condition, pressed) || intercept;
			}
			return intercept;
		}
	}
	
	public void addKeyStrokeProcessor(IKeyStrokeProcessor processor) {
		synchronized (processors) {
			if(!processors.contains(processor)) {
				processors.add(processor);
			}
		}
	}
	
	public void removeKeyStrokeProcessor(IKeyStrokeProcessor processor) {
		synchronized (processors) {
			if(processors.contains(processor)) {
				processors.remove(processor);
			}
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
