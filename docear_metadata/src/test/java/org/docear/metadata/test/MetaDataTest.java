package org.docear.metadata.test;

import java.util.HashMap;
import java.util.Map;

import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.GoogleScholarExtractor;
import org.docear.metadata.extractors.HtmlDataExtractor.CommonConfigKeys;
import org.docear.metadata.extractors.MalformedConfigException;

public class MetaDataTest {

	public static void main(String[] args) {
		Map<ExtractorConfigKey, Object> config = new HashMap<ExtractorConfigKey, Object>();
		config.put(CommonConfigKeys.SEARCHVALUE, "Google Scholar's ranking Algorithm: An Overview");
		
		try {
			for(int i = 0; i < 1000; i++){
				System.out.println(i + " von 150");
				new GoogleScholarExtractor().search("Google Scholar's ranking Algorithm: An Overview");
				new GoogleScholarExtractor(config);
			}
		} catch (MalformedConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
