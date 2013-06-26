/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.headlessmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;

import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.ribbon.RibbonBuilder;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 24.12.2012
 */
public class HeadlessMapViewController implements IMapViewManager {
	final private Map<String, MapModel> maps = new HashMap<String, MapModel>();
	Collection<IMapSelectionListener> mapSelectionListeners = new ArrayList<IMapSelectionListener>(); 
	private MapModel currentMap = null;
	private String currentKey = null;

	public void addMapSelectionListener(IMapSelectionListener pListener) {
		
	}

	public void addMapViewChangeListener(IMapViewChangeListener pListener) {
		
	}

	public boolean changeToMapView(Component newMapView) {
		return true;
	}

	public boolean changeToMapView(String mapViewDisplayName) {
		if(mapViewDisplayName != null && maps.containsKey(mapViewDisplayName)) {
			final MapModel nextMap = maps.get(mapViewDisplayName);
			MapModel oldMap = currentMap;
			for(IMapSelectionListener mapSelectionListener : mapSelectionListeners)
				mapSelectionListener.beforeMapChange(oldMap, nextMap);
			currentKey = mapViewDisplayName;
			currentMap = nextMap;
			for(IMapSelectionListener mapSelectionListener : mapSelectionListeners)
				mapSelectionListener.afterMapChange(oldMap, nextMap);
	        return true;
        }
        else
			return false;
	}

	public boolean changeToMode(String modeName) {
		return true;
	}

	public String checkIfFileIsAlreadyOpened(URL urlToCheck) throws MalformedURLException {
		final String key = urlToCheck.toString();
		if(maps.containsKey(key))
			return key;
		else
			return null;
	}

	public boolean close(boolean withoutSave) {
		if(currentMap == null)
			return false;
		maps.remove(currentKey);
		currentKey = null;
		currentMap = null;
		return true;
	}

	public String createHtmlMap() {
		return "";
	}

	public RenderedImage createImage() {
		return null;
	}

	public Color getBackgroundColor(NodeModel node) {
		return Color.white;
	}

	public Component getComponent(NodeModel node) {
		return null;
	}

	public Font getFont(NodeModel node) {
		return null;
	}

	public List<String> getMapKeys() {
		throw new RuntimeException("Method not implemented");
	}

	public Map<String, MapModel> getMaps() {
		return maps;
	}

	public IMapSelection getMapSelection() {
		return new MapSelection();
	}

	public Component getMapViewComponent() {
		return null;
	}

	public List<? extends Component> getMapViewVector() {
		return Collections.emptyList();
	}

	public ModeController getModeController(Component newMap) {
		return null;
	}

	public MapModel getModel() {
		return currentMap;
	}

	public MapModel getModel(Component mapView) {
		return null;
	}

	public Component getSelectedComponent() {
		throw new RuntimeException("Method not implemented");
	}

	public Color getTextColor(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public float getZoom() {
		throw new RuntimeException("Method not implemented");
	}

	public void newMapView(MapModel map, ModeController modeController) {
		final String key = map.getURL().toString();
		if(key.equals(currentKey))
			close(true);
		maps.put(key, map);
		changeToMapView(key);
	}

	public void nextMapView() {
		throw new RuntimeException("Method not implemented");
	}

	public void previousMapView() {
		throw new RuntimeException("Method not implemented");
	}

	public void removeMapSelectionListener(IMapSelectionListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	public void removeMapViewChangeListener(IMapViewChangeListener pListener) {
		throw new RuntimeException("Method not implemented");
	}

	public void scrollNodeToVisible(NodeModel node) {
		throw new RuntimeException("Method not implemented");
	}

	public void setZoom(float zoom) {
		throw new RuntimeException("Method not implemented");
	}

	public boolean tryToChangeToMapView(String mapView) {
		return changeToMapView(mapView);
	}

	public boolean tryToChangeToMapView(URL url) throws MalformedURLException {
		if(url == null)
			return false;
		return tryToChangeToMapView(url.toString());
	}

	public void updateMapViewName() {
		throw new RuntimeException("Method not implemented");
	}

	public boolean isLeftTreeSupported(Component mapViewComponent) {
		throw new RuntimeException("Method not implemented");
	}

	public Map<String, MapModel> getMaps(String modename) {
		return maps;
	}

	public List<Component> getViews(MapModel map) {
		return Collections.emptyList();
	}

	public JScrollPane getScrollPane() {
		return null;
	}

	public Container getViewport() {
		return null;
	}

	public void updateMenus(MenuBuilder menuBuilder) {
		
	}
	
	public void updateRibbon(RibbonBuilder ribbonBuilder) {
		
	}

	public void obtainFocusForSelected() {
		
	}

	public void setTitle() {
		
	}

	public Object setEdgesRenderingHint(Graphics2D g) {
		return "";
	}

	public void setTextRenderingHint(Graphics2D g) {
		
	}

	public boolean closeAllMaps() {
		maps.clear();
		currentKey = null;
		currentMap = null;
		return true;
	}
	
	private class MapSelection implements IMapSelection {

		public void centerNode(NodeModel node) {
			// TODO Auto-generated method stub
			
		}

		public NodeModel getSelected() {
			// TODO just a hack because of undo
			return Controller.getCurrentController().getModeController().getMapController().getRootNode();
		}

		public Set<NodeModel> getSelection() {			 
			return new HashSet<NodeModel>();
		}

		public List<NodeModel> getOrderedSelection() {
			// TODO Auto-generated method stub
			return null;
		}

		public List<NodeModel> getSortedSelection(boolean differentSubtrees) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isSelected(NodeModel node) {
			// TODO Auto-generated method stub
			return false;
		}

		public void keepNodePosition(NodeModel node, float horizontalPoint,
				float verticalPoint) {
			// TODO Auto-generated method stub
			
		}

		public void makeTheSelected(NodeModel node) {
			// TODO Auto-generated method stub
			
		}

		public void scrollNodeToVisible(NodeModel selected) {
			// TODO Auto-generated method stub
			
		}

		public void selectAsTheOnlyOneSelected(NodeModel node) {
			// TODO Auto-generated method stub
			
		}

		public void selectBranch(NodeModel node, boolean extend) {
			// TODO Auto-generated method stub
			
		}

		public void selectContinuous(NodeModel node) {
			// TODO Auto-generated method stub
			
		}

		public void selectRoot() {
			// TODO Auto-generated method stub
			
		}

		public void setSiblingMaxLevel(int nodeLevel) {
			// TODO Auto-generated method stub
			
		}

		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void toggleSelected(NodeModel node) {
			// TODO Auto-generated method stub
			
		}

		public void replaceSelection(NodeModel[] nodes) {
			// TODO Auto-generated method stub
			
		}
	}

	public void setScrollbarsVisible(boolean areScrollbarsVisible) {
		
	}
}