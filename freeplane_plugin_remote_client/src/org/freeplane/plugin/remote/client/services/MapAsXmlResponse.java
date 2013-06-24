package org.freeplane.plugin.remote.client.services;

public class MapAsXmlResponse {
	private final String xmlString;
	private final long revision;

	public MapAsXmlResponse(String xmlString, long revision) {
		super();
		this.xmlString = xmlString;
		this.revision = revision;
	}

	public String getXmlString() {
		return xmlString;
	}

	public long getRevision() {
		return revision;
	}

}
