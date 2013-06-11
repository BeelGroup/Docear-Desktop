package org.docear.plugin.services;


public class AlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = -1030909086001665733L;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public AlreadyRegisteredException() {
		super();
	}
	
	public AlreadyRegisteredException(String message) {
		super(message);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
