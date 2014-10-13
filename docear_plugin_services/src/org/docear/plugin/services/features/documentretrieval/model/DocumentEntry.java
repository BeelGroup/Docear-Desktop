package org.docear.plugin.services.features.documentretrieval.model;

import java.net.MalformedURLException;
import java.net.URL;

public class DocumentEntry {

	private final String title;
	private final URL link;
	private final URL clickUrl;
	private final String prefix;
	private final boolean highlighted;
	private String evaluationLabel = "";
	private int id;
	
	
	public DocumentEntry(int setId, String prefix, String title, String evaluationLabel, String url, String clickUrl, boolean highlighted) throws MalformedURLException {
		this.prefix = prefix;
		this.title = title;
		this.link = (url==null ? null:new URL(url));
		this.clickUrl = (clickUrl==null ? null:new URL(clickUrl));
		this.highlighted = highlighted;
		this.evaluationLabel = evaluationLabel;
		this.id = setId;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getTitle() {
		return title;
	}
	
	public String getEvaluationLabel() {
		return evaluationLabel;
	}

	public int getSetId() {
		return id;
	}

	public URL getLink() {
		return link;
	}
	
	public URL getClickUrl() {
		return clickUrl;
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	
	public String toString() {		
		return title;
	}
	
}
