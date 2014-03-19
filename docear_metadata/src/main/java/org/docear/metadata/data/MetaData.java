package org.docear.metadata.data;



public abstract class MetaData {	
	
	public enum AbstractSource implements MetaDataSource{
		ABSTRACT;
	}
	
	protected int rank;	
	protected MetaDataSource source;
	protected String query;
	
	public MetaData(){}
			
	public MetaData(int rank, MetaDataSource source, String query) {
		this.rank = rank;		
		this.source = source;
		this.query = query;
	}
	
	public int getRank() {
		return rank;
	}
	public void setRank(short rank) {
		this.rank = rank;
	}
	public MetaDataSource getSource() {
		return source;
	}
	public void setSource(MetaDataSource source) {
		this.source = source;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
