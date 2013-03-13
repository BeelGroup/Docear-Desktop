/**
 * author: Marcel Genzmehr
 * 22.08.2011
 */
package org.docear.plugin.core.event;

import java.util.EventObject;

import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;


/**
 * 
 */
public class DocearEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final DocearEventType type;	
	private final Object eventObject;
	private final DocearWorkspaceProject project;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public DocearEvent(Object source, DocearWorkspaceProject project) {
		this(source, project, DocearEventType.NULL, null);
	}
	
	public DocearEvent(Object source, DocearWorkspaceProject project, DocearEventType type) {
		this(source, project, type, null);
	}
	
	public DocearEvent(Object source, DocearWorkspaceProject project, DocearEventType type, Object eventObj) {
		super(source);
		this.type = type;
		this.eventObject = eventObj;
		this.project = project;
	}
	
	public DocearEvent(Object source, DocearWorkspaceProject project, Object eventObj) {
		this(source, project, DocearEventType.NULL , eventObj);
	}
	
	public DocearEvent(Object source, Object eventObj) {
		this(source, null, DocearEventType.NULL , eventObj);
	}
	
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public DocearEventType getType() {
		return type;
	}
	
	public Object getEventObject() {
		return eventObject;
	}
	
	public DocearWorkspaceProject getProject() {
		return this.project;
	}
	
	public String toString() {
		return this.getClass().getSimpleName()+"[type="+getType()+";eventObject="+getEventObject()+";source="+getSource()+"]";
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
