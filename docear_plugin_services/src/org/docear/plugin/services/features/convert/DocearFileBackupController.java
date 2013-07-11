package org.docear.plugin.services.features.convert;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.docear.plugin.services.ServiceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.URIUtils;

public final class DocearFileBackupController {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-M-d-HH-mm-ss");
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	private DocearFileBackupController() {
	}
	
	/***********************************************************************************
	 * METHODS
	 * @throws IOException 
	 **********************************************************************************/

	public static void createConversionBackup(String label, MapModel map) throws IOException {
		if(map == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(label == null) {
			label = "";
		}
		File mapFile = map.getFile();
		if(mapFile != null) {
			File backupDir = new File(URIUtils.getAbsoluteFile(ServiceController.getController().getUserSettingsHome()), "backup"+File.separator+label);
			if(!backupDir.exists()) {
				backupDir.mkdirs();
			}
			File backupFile = new File(backupDir ,dateFormat.format(new Date())+"__"+mapFile.getName());
			FileUtils.copyFile(mapFile, backupFile);
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
