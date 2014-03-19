package org.docear.plugin.pdfutilities.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import name.pachler.nio.file.WatchKey;

import org.docear.plugin.core.features.DocearInternallyLoadedMap;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.map.MapConverter;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class DocearAutoMonitoringListener implements IMapLifeCycleListener,  WindowFocusListener{
	
	private List<NodeModel> autoMonitorNodes = new ArrayList<NodeModel>();
	private boolean startup = true;
		
	Comparator<WatchKey> watchKeyComparator = new Comparator<WatchKey>() {

		public int compare(WatchKey key1, WatchKey key2) {
			int hash1 = key1.hashCode(); 
			int hash2 = key2.hashCode();
			if(hash1 > hash2) {
				return 1;
			}
			else if(hash1 < hash2) {
				return -1;
			}
			return 0;
		}
	};
	//TODO: enable automatic file monitoring
	//private WatchService watcher = FileSystems.getDefault().newWatchService();
	private Map<MapModel, List<WatchKey>> mapKeysMap = new HashMap<MapModel, List<WatchKey>>();
	private Map<WatchKey, MapModel> keyMapMap = new TreeMap<WatchKey, MapModel>(watchKeyComparator);
	private Map<WatchKey, String> watchables = new TreeMap<WatchKey, String>(watchKeyComparator);
	
	
	public DocearAutoMonitoringListener() {
		//TODO: enable automatic file monitoring
//		new Thread() {
//			public void run() {
//				try {
//					WatchKey key = null;
//					while((key = watcher.take()) != null) {
//						Path watchPath = null;
//						if(key instanceof PathWatchKey) {
//							watchPath = (Path) ((PathWatchKey) key).watchable();
//						}
//						for(WatchEvent<?> event : key.pollEvents()) {
//							Object obj = event.context();
//							File file = new File(watchPath.toString(), obj.toString());
//							if(StandardWatchEventKind.ENTRY_CREATE.equals(event.kind())) {
//								onCreateFile(file, key);
//							}
//							else if(StandardWatchEventKind.ENTRY_DELETE.equals(event.kind())) {
//								onDeleteFile(file, key);								
//							}
//							else if(StandardWatchEventKind.ENTRY_MODIFY.equals(event.kind())) {
//								onModifyFile(file, key);
//							}
//							else {
//								LogUtils.info("DirectoryWatcher: unknown event kind"+ event.kind());
//							}
//						}
//						key.reset();
//					}
//				} catch (InterruptedException e) {
//				}
//				finally {
//					LogUtils.info("closing directory watcher...");
//					try {
//						watcher.close();
//					} catch (IOException e) {
//					}
//				}				
//			}
//		}.start();
	}
	
	
	public void onCreate(final MapModel map) {
		if(map == null || map.getFile() == null || DocearInternallyLoadedMap.isInternallyLoaded(map)) { 
			return;
		}
		List<? extends NodeModel> monitoringNodes = (List<? extends NodeModel>) getAutoMonitorNodes(map.getRootNode());
		if(monitoringNodes == null || monitoringNodes.size() <= 0) {
			return;
		}
		autoMonitorNodes.addAll(monitoringNodes);
		
		//TODO: enable automatic file monitoring
		//registerMonitoredDirectories(map, monitoringNodes);
		
		if(!startup){
			SwingUtilities.invokeLater(new Thread() {
				public void run() {
					
					LogUtils.info("Monitoring started"); //$NON-NLS-1$
					startMonitoring();
			
				} //run()
			}); // Thread
		}
	}

	public void onRemove(MapModel map) {
		List<WatchKey> keys = mapKeysMap.get(map);
		if(keys != null) {
			for (WatchKey watchKey : keys) {
				cleanUpWatchKey(watchKey);
			}
		}
		
	}

	public void windowGainedFocus(WindowEvent e) {
		if(startup && !MapConverter.currentlyConverting){
			startup = false;
			startMonitoring();			
		}
	}

	public void windowLostFocus(WindowEvent e) {
	
	}
	
	private List<? extends NodeModel> getAutoMonitorNodes(NodeModel node) {
		List<NodeModel> result = new ArrayList<NodeModel>();
		if(MonitoringUtils.isAutoMonitorNode(node)){
			result.add(node);
		}
		for(NodeModel child : node.getChildren()){
			result.addAll(getAutoMonitorNodes(child));
		}
		return result;
	}

	public void onSavedAs(MapModel map) {
		
	}

	public void onSaved(MapModel map) {
		
	}

	private synchronized void startMonitoring() {
		if(autoMonitorNodes.size() > 0){
			UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(autoMonitorNodes, !startup);
			autoMonitorNodes.clear();
		}		
	}
	
	private void cleanUpWatchKey(WatchKey watchKey) {
		watchKey.cancel();
		watchables.remove(watchKey);
		keyMapMap.remove(watchKey);
	}
	

}
