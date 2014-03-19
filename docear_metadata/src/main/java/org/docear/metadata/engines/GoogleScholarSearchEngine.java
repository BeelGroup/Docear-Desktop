package org.docear.metadata.engines;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.docear.metadata.data.MetaData;
import org.docear.metadata.events.MetaDataListener;
import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.GoogleScholarExtractor;
import org.docear.metadata.extractors.MalformedConfigException;
import org.docear.metadata.extractors.HtmlDataExtractor.CommonConfigKeys;

public class GoogleScholarSearchEngine extends SearchEngine {

	public GoogleScholarSearchEngine(Map<ExtractorConfigKey, Object> config) {
		super(config);
		
	}	

	@Override
	public Callable<Collection<MetaData>> getExtractor(String query, Map<ExtractorConfigKey, Object> options, MetaDataListener listener) throws MalformedConfigException {
		Map<ExtractorConfigKey, Object> queryConfig = this.config;
		queryConfig.putAll(options);
		queryConfig.put(CommonConfigKeys.SEARCHVALUE, query);
		return new GoogleScholarExtractor(queryConfig, listener);		
	}

}
