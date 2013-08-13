package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;

public final class DocearInternallyLoadedMap implements IExtension {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static boolean isInternallyLoaded(MapModel map) {
		if(map.getExtension(DocearInternallyLoadedMap.class) != null) {
			return true;
		}
		return false;
	}
	
	public static void markInternallyLoaded(MapModel map) {
		map.putExtension(DocearInternallyLoadedMap.class, new DocearInternallyLoadedMap());
	}
	
	public static void unmarkInternallyLoaded(MapModel map) {
		map.removeExtension(DocearInternallyLoadedMap.class);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
