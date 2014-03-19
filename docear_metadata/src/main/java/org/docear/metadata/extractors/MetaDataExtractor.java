package org.docear.metadata.extractors;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.docear.metadata.data.MetaData;

public interface MetaDataExtractor extends Callable<Collection<MetaData>>{
			
	public Collection<MetaData> search(String query);

}
