package org.docear.plugin.core.features;

import org.freeplane.features.map.MapModel;

public final class DocearRequiredConversionController {
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	private DocearRequiredConversionController() {
		
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static void setRequiredConversion(IRequiredConversion conversion, MapModel map) {
		if(map == null || conversion == null) {
			return;
		}
		if(!map.containsExtension(conversion.getClass())) {
			map.addExtension(conversion);
			if(conversion.requiresBackup()) {
				DocearFileBackupController.addMapBackup(conversion.getBackupLabel(), map);
			}
		}
	}
	
	public static <T extends IRequiredConversion> T getRequiredConversion(Class<T> clazz, MapModel map) {
		return (T) map.getExtension(clazz);
	}
	
	public static boolean hasRequiredConversion(Class<? extends IRequiredConversion> clazz, MapModel map) {
		return getRequiredConversion(clazz, map) != null;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
