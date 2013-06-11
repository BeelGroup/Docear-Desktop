package org.docear.plugin.core.ui.components;

import java.awt.Component;
import java.util.LinkedHashSet;
import java.util.Set;

public class ComponentGroup {
	private Set<Component> components = new LinkedHashSet<Component>();
	private boolean enabled = true; 
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void add(Component c) {
		this.components.add(c);
	}
	
	public void remove(Component c) {
		this.components.remove(c);
	}
	
	public Component[] getComponents() {
		return this.components.toArray(new Component[0]);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		updateComponents();
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}


	private void updateComponents() {
		for(Component comp : components) {
			comp.setEnabled(isEnabled());
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
