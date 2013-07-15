package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;

public interface IRequiredConversion extends IExtension {
	public boolean requiresBackup();
	public String getBackupLabel();

}
