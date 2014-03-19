package org.docear.metadata.extractors;

public class MalformedConfigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MalformedConfigException() {
		super("Could not parse config data.");		
	}	

}
