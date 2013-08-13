package org.docear.plugin.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.NullArgumentException;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearInternallyLoadedMap;
import org.docear.plugin.core.features.DocearLifeCycleObserver.MapEventType;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.plugin.workspace.mindmapmode.MModeWorkspaceUrlManager;

public class MapUtils {

	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public static MapModel getMapFromUri(URI uri) throws IOException {
		ModeController current = Controller.getCurrentModeController();
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		try {
			for (Entry<String, MapModel> entry : maps.entrySet()) {
				if (entry.getValue().getFile() != null && entry.getValue().getFile().toURI().equals(uri)) {
					return entry.getValue();
				}
			}
			try {
				MapModel map = new MMapModel();
				AttributeRegistry.getRegistry(map);
				URL url = MModeWorkspaceUrlManager.getController().getAbsoluteUrl(map, uri);
				final MapIO mapIO = (MapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				mapIO.load(url, map);
				DocearInternallyLoadedMap.markInternallyLoaded(map);
				DocearController.getController().getLifeCycleObserver().fireMapEvent(MapEventType.CREATED, map);
				return map;
			}
			catch (Exception cause) {
				throw new IOException(cause);
			}
		}
		finally {
			Controller.getCurrentController().selectMode(current);
		}
	}
	
	public static boolean saveMap(MapModel map, File file) {
		try {
			Controller.getCurrentController().selectMode(MModeController.MODENAME);
			MMapIO mapIO = (MMapIO) MModeController.getMModeController().getExtension(MapIO.class);
			mapIO.writeToFile(map, file);
			map.setSaved(true);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static List<MapModel> getMapsFromUris(Collection<URI> mindmaps) {
		List<MapModel> maps = new ArrayList<MapModel>();
		for (URI uri : mindmaps) {
			try {
				MapModel map = getMapFromUri(uri);
				if (map != null) {
					maps.add(map);
				}
			}
			catch (Exception e) {
				LogUtils.warn(e.getMessage());
			}
		}
		return maps;
	}
	public static boolean isMapCurrentlyOpened(MapModel map) {
		if (map == null) {
			throw new NullArgumentException("map");
		}
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		for (Entry<String, MapModel> entry : maps.entrySet()) {
			if (entry.getValue().getFile() == null) {
				if (entry.getValue().equals(map)) {
					return true;
				}
			}
			else if (entry.getValue().getFile().equals(map.getFile())) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static MapModel openMapNoShow(URL url) throws IOException {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url)) {
			return null;
		}
		try {
			if (AddOnsController.getController().installIfAppropriate(url)) {
				return null;
			}
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			final MapModel map = new MMapModel();
			UrlManager.getController().loadCatchExceptions(url, map);
			//map.setReadOnly(true);
			map.setSaved(true);
			return map;
		} 
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	public static void showMap(MapModel map) {
		if(map == null) {
			throw new IllegalArgumentException("NULL");
		}
		Controller.getCurrentModeController().getMapController().newMapView(map);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

}
