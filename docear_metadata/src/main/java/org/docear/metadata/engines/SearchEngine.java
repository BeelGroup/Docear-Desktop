package org.docear.metadata.engines;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.docear.metadata.data.MetaData;
import org.docear.metadata.events.MetaDataListener;
import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.MalformedConfigException;

public abstract class SearchEngine {
	
	protected Map<ExtractorConfigKey, Object> config = new HashMap<ExtractorConfigKey, Object>();
	
	public SearchEngine(Map<ExtractorConfigKey, Object> config){
		if(config != null){
			this.config = config;
		}
	}
	
	public abstract Callable<Collection<MetaData>> getExtractor(String query, Map<ExtractorConfigKey, Object> options, MetaDataListener listener) throws MalformedConfigException;

	public Map<ExtractorConfigKey, Object> getConfig() {
		return config;
	}

	public void setConfig(Map<ExtractorConfigKey, Object> config) {
		if(config != null){
			this.config = config;
		}
	}

}
