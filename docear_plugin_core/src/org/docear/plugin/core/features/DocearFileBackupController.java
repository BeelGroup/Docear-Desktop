package org.docear.plugin.core.features;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.URIUtils;

public final class DocearFileBackupController implements IFileBackupHandler {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-M-d-HH-mm-ss");
	private static IFileBackupHandler backupHandler;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	private DocearFileBackupController() {
	}
	
	/***********************************************************************************
	 * METHODS
	 * @throws IOException 
	 **********************************************************************************/

	public static void setFileBackupHandler(IFileBackupHandler handler) {
		backupHandler = handler;
	}
	
	public static IFileBackupHandler getFileBackupHandler() {
		if(backupHandler == null) {
			backupHandler = new DocearFileBackupController();
		}
		return backupHandler;
	}
	
	public static void createBackup(String label, MapModel map) throws IOException {
		getFileBackupHandler().createMapBackup(label, map);
	}
	
	public static void createBackup(String label, File file) throws IOException {
		getFileBackupHandler().createFileBackup(label, file);
	}
	
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
			
			File backupDir = new File(URIUtils.getAbsoluteFile(URIUtils.createURI(Compat.getDefaultApplicationUserDirectory())), "backup"+File.separator+label);
			if(!backupDir.exists()) {
				backupDir.mkdirs();
			}
			File backupFile = new File(backupDir ,dateFormat.format(new Date())+"__"+mapFile.getName());
			FileUtils.copyFile(mapFile, backupFile);
			LogUtils.info("created backup "+ backupFile +" of "+ mapFile);
		}
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
		
		File backupDir = new File(URIUtils.getAbsoluteFile(URIUtils.createURI(Compat.getDefaultApplicationUserDirectory())), "backup"+File.separator+label);
		if(!backupDir.exists()) {
			backupDir.mkdirs();
		}
		File backupFile = new File(backupDir ,dateFormat.format(new Date())+"__"+file.getName());
		FileUtils.copyFile(file, backupFile);
		LogUtils.info("created backup "+ backupFile +" of "+ file);
	}
}
