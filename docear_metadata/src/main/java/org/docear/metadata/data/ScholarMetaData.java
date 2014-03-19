package org.docear.metadata.data;

public class ScholarMetaData extends MetaData {
	
	public enum ScholarSource implements MetaDataSource{
		GOOGLESCHOLAR;
	}
	
	
	
	public ScholarMetaData(int rank,  String bibtex, String query) {
		super(rank, ScholarSource.GOOGLESCHOLAR, query);
		this.bibtex = bibtex;
	}
	private String bibtex;
	
	public String getBibtex() {
		return bibtex;
	}
	public void setBibtex(String bibtex) {
		this.bibtex = bibtex;
	}

}
