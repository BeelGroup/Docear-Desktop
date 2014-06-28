package org.docear.plugin.services.features.documentretrieval.model;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;

public class DocumentEntries {
	
	private final int documentsAvailable;
	private Collection<DocumentEntry> documentEntries = new ArrayList<DocumentEntry>();
	
	public DocumentEntries(int documentsAvailable) throws MalformedURLException {
		this.documentsAvailable = documentsAvailable;
	}
	
	public void addDocumentEntry(int setId, String prefix, String title, String evaluationLabel, String url, String clickUrl, boolean highlighted) throws MalformedURLException {
		documentEntries.add(new DocumentEntry(setId, prefix, title, evaluationLabel, url, clickUrl, highlighted));
	}
	
	public Collection<DocumentEntry> getDocumentEntries() {
		return documentEntries;
	}
	
	public int getDocumentsAvailable() {
		return this.documentsAvailable;
	}
}
