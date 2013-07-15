package org.docear.plugin.core.features;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.freeplane.core.extension.IExtension;

public class DocearMapBackupStack implements IExtension{

	private final Set<String> label = new HashSet<String>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void addLabel(String label) {
		this.label.add(label);
	}
	
	public Iterator<String> iterator() {
		return label.iterator();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
