package org.docear.metadata.events;

import java.util.ArrayList;

import org.docear.metadata.data.MetaData;

public class FetchedResultsEvent extends MetaDataEvent {
	
	private ArrayList<MetaData> result = new ArrayList<MetaData>();	

	public FetchedResultsEvent(ArrayList<MetaData> result) {
		super();
		this.result = result;
	}

	public ArrayList<MetaData> getResult() {
		return result;
	}

	public void setResult(ArrayList<MetaData> result) {
		this.result = result;
	}
	
}
