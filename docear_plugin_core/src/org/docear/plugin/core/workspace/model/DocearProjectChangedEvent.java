package org.docear.plugin.core.workspace.model;

import java.util.EventObject;

public class DocearProjectChangedEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private final Object object;
	private final Object descriptor;

	public DocearProjectChangedEvent(DocearWorkspaceProject project, Object object) {
		this(project, object, null);
	}
	
	public DocearProjectChangedEvent(DocearWorkspaceProject project, Object object, Object descriptor) {
		super(project);
		this.object = object;
		this.descriptor = descriptor;
	}

	public DocearWorkspaceProject getSource() {
		return (DocearWorkspaceProject) super.getSource();
	}

	public Object getObject() {
		return object;
	}

	public Object getDescriptor() {
		return descriptor;
	}

}
