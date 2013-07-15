package org.docear.plugin.core.features;

import java.io.File;
import java.io.IOException;

import org.freeplane.features.map.MapModel;

public interface IFileBackupHandler {
	public void createMapBackup(String label, MapModel map) throws IOException;
	public void createFileBackup(String label, File file) throws IOException;
}
