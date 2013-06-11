package org.docear.plugin.services.features.upload;

import java.io.File;
import java.util.Iterator;

import org.docear.plugin.core.features.DocearThread;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.features.io.FiletransferClient;

public class UploadThread extends DocearThread {
	
	private final UploadController uploadCtrl;
	
	public UploadThread(UploadController controller) {
		super("Docear Upload-Thread");
		uploadCtrl = controller;
	}

	public void execute() {
		while (!isTerminated()) {
			DocearLogger.info(this+" running.");
			int backupMinutes = uploadCtrl.getUploadInterval();
			try {
				if (uploadCtrl.isBackupEnabled() || uploadCtrl.isUploadEnabled()) {
					DocearLogger.info(this.toString()+": uploading packages to the server ...");
					Iterator<File> jobBuffer = uploadCtrl.getUploadJobs();					
					FiletransferClient client = new FiletransferClient("mindmaps");
					while(jobBuffer.hasNext()) {
						File file = jobBuffer.next();
						if(file == null || !file.exists()) {
							jobBuffer.remove();
							continue;
						}
						boolean success = false;
						try {
							success = client.sendFile(file, true);
						}
						catch(Exception e) {
							DocearLogger.warn("org.docear.plugin.services.upload.UploadThread.execute() -> sendFile: "+e.getMessage());
						}
						if (success) {
							DocearLogger.info(this.toString()+": synchronizing '"+file+"' successfull");
							jobBuffer.remove();
						}
						else {
							DocearLogger.info(this.toString()+": synchronizing '"+file+"' failed");
						}
					}
				}
			} catch (Exception e) {
				DocearLogger.warn("org.docear.plugin.services.upload.UploadThread.execute(): "+e.getMessage());
			}
			try {
				if(!isInterrupted()) {
					sleep(60000 * backupMinutes);
				}
			} catch (InterruptedException e) {
			}
		}

	}
}
