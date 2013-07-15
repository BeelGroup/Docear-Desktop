package org.docear.plugin.pdfutilities.features;

import org.docear.plugin.core.features.IRequiredConversion;

public class ConvertAnnotationsExtension implements IRequiredConversion {
	private boolean needsBackup = true;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public boolean requiresBackup() {
		return needsBackup;
	}

	public void backupDone() {
		needsBackup = false;
	}

	public String getBackupLabel() {
		return "convert_annotations";
	}
	
}
