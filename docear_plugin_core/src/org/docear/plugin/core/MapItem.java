package org.docear.plugin.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.util.MapUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.view.swing.map.MapView;

public class MapItem {
	private URI uri;
	private MapModel map;
	private Boolean mapIsOpen;

	public MapItem(URI mapUri) {
		this.uri = mapUri;
	}

	public MapItem(MapModel mapModel) {
		this.map = mapModel;
	}

	public MapModel getModel() {
		Controller.getCurrentController().selectMode(MModeController.MODENAME);
		if (this.map == null) {
			URL url;
			String mapExtensionKey;
			try {
				url = URIUtils.getAbsoluteURI(uri).toURL();
				mapExtensionKey = Controller.getCurrentController().getMapViewManager().checkIfFileIsAlreadyOpened(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}

			if (mapExtensionKey != null) {
				map = Controller.getCurrentController().getMapViewManager().getMaps().get(mapExtensionKey);
				if (map != null) {
					return map;
				}
			}

			map = new MMapModel();
			try {
				File f = URIUtils.getAbsoluteFile(uri);
				if (f.exists()) {
					map = MapUtils.getMapFromUri(f.toURI());
					// do not work on non-docear-mindmaps
					if (DocearMapModelController.getModel(map) == null) {
						throw new Exception("no DocearMapModel");
					}
					map.setURL(url);
					map.setSaved(true);
				} else {
					map = null;
				}
			} catch (Exception e) {
				LogUtils.warn("org.docear.plugin.core.MapItem.getModel(): "+ e.getMessage());
				map = null;
			}
		}

		return map;
	}

	public String getIdentifierForDialog() {
		if (this.uri != null) {
			return this.uri.getPath();
		}

		if (this.map != null && this.map.getTitle() != null) {
			return TextUtils.getText("unsaved_map") + " \""
					+ this.map.getTitle() + "\"";
		}

		return TextUtils.getText("unknown_unsaved_map");
	}

	@SuppressWarnings("unchecked")
	public boolean isMapOpen() {
		if (this.mapIsOpen == null) {
			if (this.uri == null) {
				this.mapIsOpen = true;
			} 
			else {
				for (MapView view : (List<MapView>) Controller
						.getCurrentController().getMapViewManager().getMapViewVector()) {
					File mapFile = view.getModel().getFile();
					if (mapFile != null) {
						URI mapUri = mapFile.toURI();

						if (uri.equals(mapUri)) {
							this.mapIsOpen = true;
						}

					}
				}
				if(this.mapIsOpen == null) {
					this.mapIsOpen = false;
				}
			}
		}

		return this.mapIsOpen;
	}
}
