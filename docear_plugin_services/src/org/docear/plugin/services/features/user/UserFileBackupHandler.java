package org.docear.plugin.services.features.user;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.docear.plugin.core.features.DocearFileBackupController;
import org.docear.plugin.core.features.IFileBackupHandler;
import org.docear.plugin.services.ServiceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.URIUtils;

public class UserFileBackupHandler implements IFileBackupHandler {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public UserFileBackupHandler() {
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public void createMapBackup(String label, MapModel map) throws IOException {
		if(map == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(label == null) {
			label = "";
		}
		File mapFile = map.getFile();
		if(mapFile != null) {
			File backupDir = new File(getFolder(), label);
			if(!backupDir.exists()) {
				backupDir.mkdirs();
			}
			File backupFile = new File(backupDir , DocearFileBackupController.dateFormat.format(new Date())+"__"+mapFile.getName());
			FileUtils.copyFile(mapFile, backupFile);
			LogUtils.info("created backup "+ backupFile +" of "+ mapFile);
		}
	}
	
	public File getFolder() {
		return new File(URIUtils.getAbsoluteFile(ServiceController.getController().getUserSettingsHome()), "backup");
	}

	public void createFileBackup(String label, File file) throws IOException {
		if(file == null) {
			throw new IllegalArgumentException("NULL");
		}
		if(!file.exists() || file.isDirectory()) {
			throw new IllegalArgumentException("file does not exist or is a directory");
		}
		if(label == null) {
			label = "";
		}
		
		File backupDir = new File(getFolder(), label);
		if(!backupDir.exists()) {
			backupDir.mkdirs();
		}
		File backupFile = new File(backupDir ,DocearFileBackupController.dateFormat.format(new Date())+"__"+file.getName());
		FileUtils.copyFile(file, backupFile);
		LogUtils.info("created backup "+ backupFile +" of "+ file);
	}
}
