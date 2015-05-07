package org.docear.metadata.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.docear.metadata.data.MetaData;
import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.GoogleScholarExtractor;
import org.docear.metadata.extractors.HtmlDataExtractor.CommonConfigKeys;
import org.docear.metadata.extractors.MalformedConfigException;

public class MetaDataTest {
	static int count = 1000;
	
	public static void main(String[] args) {
		Map<ExtractorConfigKey, Object> config = new HashMap<ExtractorConfigKey, Object>();
		config.put(CommonConfigKeys.SEARCHVALUE, "Google Scholar's ranking Algorithm: An Overview");
		
		try {
			for(int i = 0; i < count; i++){
				System.out.println(i + " von " + count);
				new GoogleScholarExtractor(config);
				Collection<MetaData> result = new GoogleScholarExtractor().search("Google Scholar's ranking Algorithm: An Overview");
				System.out.println(result.size());
			}
		} catch (MalformedConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
