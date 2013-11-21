package org.docear.plugin.services.features.io;

import org.docear.plugin.core.event.DocearEvent;

public class DocearUnauthorizedExceptionEvent extends DocearEvent {

	private static final long serialVersionUID = 1L;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearUnauthorizedExceptionEvent(Object source) {
		super(source, DocearServiceResponse.Status.UNAUTHORIZED);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
