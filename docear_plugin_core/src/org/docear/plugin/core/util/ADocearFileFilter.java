package org.docear.plugin.core.util;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class ADocearFileFilter implements FileFilter {
	
	
	public List<String> getStringList(String property) {
		List<String> result = new ArrayList<String>();
		
		if(property == null || property.length() <= 0) return result;
		property = property.trim();		
		String[] list = property.split("\\|");
		for(String s : list){
			if(s != null && s.length() > 0){
				result.add(s);
			}
		}
		return result;
	}
}
