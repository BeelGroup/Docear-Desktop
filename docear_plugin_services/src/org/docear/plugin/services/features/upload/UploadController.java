package org.docear.plugin.services.features.upload;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.io.DirectoryObserver;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.user.IUserAccountChangeListener;
import org.freeplane.core.user.UserAccountChangeEvent;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.URIUtils;

public class UploadController extends ADocearServiceFeature {
	
	private static FileFilter zipFilter = new FileFilter() {
		public boolean accept(File f) {
			return (f != null && f.getName().toLowerCase().endsWith(".zip"));
		}
	};
	
	private final Map<String, MapModel> mapUploadQueue = new HashMap<String, MapModel>();
	
	private final Set<DirectoryObserver> observers = new HashSet<DirectoryObserver>(); 
	
	private final Runnable packerRunner = new Runnable() {
		public void run() {
			createPackages();
		}
	};
	
	private final CyclicUploadPacker packerThread = new CyclicUploadPacker(packerRunner, (180)); //every 3 minutes 
	
	private final UploadThread uploadThread = new UploadThread(this);
	
	private final Set<File> uploadFiles = new HashSet<File>();
	
	private final DirectoryObserver defaultObserver = new DirectoryObserver() {
		
		public void fileRemoved(File file) {
			uploadFiles.remove(file);
		}
		
		public void fileCreated(File file) {
			uploadFiles.add(file);
		}
	};
	
	public UploadController() {
		DocearController.getController().getEventQueue().addEventListener(new IDocearEventListener() {		
			public void handleEvent(DocearEvent event) {
				if (event.getType() == DocearEventType.APPLICATION_CLOSING) {
					shutdown();
				}
				else if (event.getType() == DocearEventType.FINISH_THREADS) {
					finishThreads();
				}
			}
		});
		
		this.addUploadDirectoryObserver(defaultObserver);
	}
	
	/**
	 * @return
	 */
	public boolean isBackupEnabled() {
		DocearUser userSettings = ServiceController.getCurrentUser();
		return userSettings.isBackupEnabled() && userSettings.isTransmissionEnabled() && userSettings.isValid();
	}

	public boolean isUploadEnabled() {
		DocearUser user = ServiceController.getCurrentUser();
		boolean needUser = (user.isBackupEnabled() || user.isRecommendationsEnabled()) && user.isTransmissionEnabled() && user.isOnline();

		return needUser && user.isValid();
	}
	
	/**
	 * Provides the time in minutes until the next upload cycle should be started
	 * 
	 * @return time to wait until the next upload cycle
	 */
	public int getUploadInterval() {
		final ResourceController resourceCtrl = Controller.getCurrentController().getResourceController();
		int backupMinutes = resourceCtrl.getIntProperty("save_backup_automcatically", 0);
		if (backupMinutes <= 0) {
			backupMinutes = 30;
		}
		return backupMinutes;
	}
	/**
	 * @return
	 */
	public File getUploadDirectory(String forName)  {
		File uploadBufferDirectory = new File(getUploadBufferPath(), forName);
		if (!uploadBufferDirectory.exists()) {
			uploadBufferDirectory.mkdirs();
		}
		return uploadBufferDirectory;
	}
	
	public File getUploadBufferPath() {
		return new File(URIUtils.getFile(ServiceController.getController().getUserSettingsHome()), "queue");
	}
	
	/**
	 * @return
	 */
	public File[] getUploadPackages(String forName) {
		return getUploadDirectory(forName).listFiles(zipFilter);
	}
	
	
	public Iterator<File> getUploadJobs() {
		return new Iterator<File>() {
			int i = 0;
			File[] files = uploadFiles.toArray(new File[0]); 
			public boolean hasNext() {
				return i < files.length;
			}

			public File next() {
				return files[i++];
			}

			public void remove() {
				synchronized (uploadFiles) {
					uploadFiles.remove(files[i-1]);
				}
				
			}
		};
	}
	
	public void refreshUploadBuffer() {
		File[] files = getUploadPackages("mindmaps");
		if(files == null) {
			return;
		}
		synchronized (uploadFiles) {
			for(File file : files) {
				try {
					new ZipFile(file);
					uploadFiles.add(file);
				}
				catch(Exception e) {
					LogUtils.warn("org.docear.plugin.services.features.upload.UploadController.refreshUploadBuffer() -> corrupted ZipFile: "+file.getAbsolutePath());
					file.delete();				
				}
			}
		}
	}
	
	/**
	 * @param map
	 */
	public void addMapToUpload(MapModel map) {
		boolean backup = isBackupEnabled();
		File file = map.getFile();
		if(file == null || (!backup && !isUploadEnabled())) {
			return;
		}
		DocearMapModelExtension mapExt = map.getExtension(DocearMapModelExtension.class);
		if(mapExt != null && mapExt.getMapId() != null) {
			synchronized (mapUploadQueue) {
				mapUploadQueue.put(mapExt.getMapId(), map);
			}
		}
		
	}
	
	/**
	 * @param observer
	 */
	public void addUploadDirectoryObserver(DirectoryObserver observer) {
		synchronized (observers) {
			observers.add(observer);
		}		
	}
	
	/**
	 * @param observer
	 */
	public void removeUploadDirectoryObserver(DirectoryObserver observer) {
		synchronized (observers) {
			observers.remove(observer);
		}		
	}
	
	/**
	 * 
	 */
	private void createPackages() {
		synchronized (mapUploadQueue) {
			Iterator<Entry<String, MapModel>> iter = mapUploadQueue.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, MapModel> entry = iter.next();
				createMapPackage(entry.getValue());
				iter.remove();				
			}
		}		
	}
	
	/**
	 * @param file
	 */
	protected final void fireFileCreated(File file) {
		synchronized (observers) {
			for(DirectoryObserver observer : observers) {
				observer.fileCreated(file);
			}
		}
	}
	
	/**
	 * @param file
	 */
	protected final void fireFileRemoved(File file) {
		synchronized (observers) {
			for(DirectoryObserver observer : observers) {
				observer.fileRemoved(file);
			}
		}
	}

	/**
	 * @param map
	 */
	private void createMapPackage(final MapModel map) {		
		if (map == null) {
			return;
		}
		
		final Properties meta = getMapProperties(map);
		if (meta == null) {
			return;
		}
		
		Thread thread = new Thread() {
			public void run() {
				try {					
					File backupFile = new File(getUploadDirectory("mindmaps").getAbsolutePath(), System.currentTimeMillis() + "_" + map.getFile().getName() + ".zip");
					
					
					FileOutputStream fout = null;
					ZipOutputStream out = null;					
					InputStream in = null;
					try {			
						fout = new FileOutputStream(backupFile);
						out = new ZipOutputStream(fout);
						in = new FileInputStream(map.getFile());
						
						ZipEntry entry = new ZipEntry("metadata.inf");
						out.putNextEntry(entry);
						meta.store(out, "");
						
						entry = new ZipEntry(map.getFile().getName());			
						out.putNextEntry(entry);
						
						while (true) {
							int data = in.read();
							if (data == -1) {
								break;
							}
							out.write(data);
						}
						out.flush();
					} 
					finally {	
						in.close();
						out.close();
						fout.close();
						fireFileCreated(backupFile);
						
						DocearController.getController().removeWorkingThreadHandle(this.getName());
					}					
				}
				catch (Exception e) {
					DocearLogger.warn("org.docear.plugin.services.upload.UploadController.createMapPackage(): "+e.getMessage());
				}				
			}
			
		};
		DocearController.getController().addWorkingThreadHandle(thread.getName());
		thread.start();
	}
	
	/**
	 * @param map
	 * @return
	 */
	private Properties getMapProperties(MapModel map) {
		DocearUser userSettings = ServiceController.getCurrentUser();
 		DocearController docearController = DocearController.getController();
				
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme == null) {
			return null;
		}
		
		boolean isLibraryMap = DocearController.getController().isLibraryMap(map);
				
		String typeName = (dmme.getType() == null ? "" : dmme.getType().name());
		
		Properties properties = new Properties();
		properties.put("mindmap_id", dmme.getMapId());
		properties.put("timestamp", ""+System.currentTimeMillis());
		properties.put("is_library_map", new Boolean(isLibraryMap).toString());
		properties.put("backup", new Boolean(userSettings.isBackupEnabled()).toString());
		properties.put("allow_content_research", new Boolean(false).toString());
		properties.put("allow_information_retrieval", new Boolean(false).toString());		
		properties.put("allow_usage_research", new Boolean(false).toString());
		properties.put("allow_recommendations", new Boolean(userSettings.isRecommendationsEnabled()).toString());
		properties.put("enable_synchronization", new Boolean(userSettings.isSynchronizationEnabled()).toString());
		properties.put("enable_collaboration", new Boolean(userSettings.isCollaborationEnabled()).toString());
		
		if (typeName != null && typeName.trim().length()>0) {
			properties.put("map_type", typeName);
		}		
		properties.put("map_version", dmme.getVersion());
		properties.put("application_name", docearController.getApplicationName());
		properties.put("application_version", docearController.getApplicationVersion());
		properties.put("application_status", docearController.getApplicationStatus());
		properties.put("application_status_version", docearController.getApplicationStatusVersion());
		properties.put("application_build", ""+docearController.getApplicationBuildNumber());
		properties.put("application_date", docearController.getApplicationBuildDate());
		properties.put("filesize", ""+map.getFile().length());
		properties.put("filename", map.getFile().getName());
		properties.put("filepath", map.getFile().getAbsolutePath());
		
		return properties;
	}
	
	/**
	 * 
	 */
	public void finishThreads() {		
		String runnerID = Integer.toHexString(this.hashCode());
		DocearController.getController().addWorkingThreadHandle(runnerID);
		packerRunner.run();
		DocearController.getController().removeWorkingThreadHandle(runnerID);
	}
	
	public void shutdown() {		
		this.packerThread.terminate();
		this.uploadThread.terminate();
	}

	@Override
	protected void installDefaults(ModeController modeController) {
		UserAccountController.getController().addUserAccountChangeListener(new IUserAccountChangeListener() {
			
			public void activated(UserAccountChangeEvent event) {
				refreshUploadBuffer();
				uploadThread.startUpload();
			}
			
			public void aboutToDeactivate(UserAccountChangeEvent event) {
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refreshUploadBuffer();
				uploadThread.start();
				packerThread.start();
			}
		});
	}
}
