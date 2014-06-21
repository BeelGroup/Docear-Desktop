package org.docear.plugin.services.features.documentretrieval.documentsearch;

public class SearchModel {
	private Long id;
	private String model;
	
	public SearchModel(Long id, String model) {
		this.id = id;
		this.model = model;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	
	
}
