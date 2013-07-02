package org.freeplane.core.ui.ribbon;

import java.util.HashMap;
import java.util.Map;

public class CurrentState {
	private Map<Class<? extends Object>, Object> map = new HashMap<Class<? extends Object>, Object>();
 
	public void set(Class<? extends Object> key, Object value) {
		map.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(Class<T> key) {
		return (T) map.get(key);
	}
	
	public boolean contains(Class<? extends Object> key) {
		return (get(key) != null);
	}

}
