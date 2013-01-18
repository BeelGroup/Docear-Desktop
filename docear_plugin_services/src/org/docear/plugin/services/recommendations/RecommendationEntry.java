package org.docear.plugin.services.recommendations;

import java.net.MalformedURLException;
import java.net.URL;

public class RecommendationEntry {

	private final String title;
	private final URL link;
	private final URL clickUrl;
	
	public RecommendationEntry(String title, String url, String clickUrl) throws MalformedURLException {
		this.title = normalize(title);
		this.link = (url==null ? null:new URL(url));
		this.clickUrl = (clickUrl==null ? null:new URL(clickUrl));
	}

	private String normalize(String str) {
		String[] tokens = str.split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			String tmp = token.trim();
			sb.append(tmp.charAt(0));
			sb.append(tmp.substring(1).toLowerCase());
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	public String getTitle() {
		return title;
	}

	public URL getLink() {
		return link;
	}
	
	public URL getClickUrl() {
		return clickUrl;
	}
	
	public String toString() {
		return title;
	}

}
