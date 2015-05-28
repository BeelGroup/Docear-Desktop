package org.docear.plugin.services.features.upload;

import org.docear.plugin.services.ServiceController;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;

public class MapLifeCycleListener implements IMapLifeCycleListener {

	public void onCreate(MapModel map) {
	}

	public void onRemove(MapModel map) {
	}

	public void onSavedAs(MapModel map) {
	}

	public void onSaved(MapModel map) {
		if(ServiceController.getFeature(UploadController.class) != null){
			ServiceController.getFeature(UploadController.class).addMapToUpload(map);
		}
	}
	
	

}
