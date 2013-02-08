package org.freeplane.plugin.workspace.model.project;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

public class ProjectModelEvent extends TreeModelEvent {

	private static final long serialVersionUID = 1L;
	private final AWorkspaceProject project; 
	private final ProjectModelEventType type;
	
	public ProjectModelEvent(AWorkspaceProject project, Object source, Object[] path, int[] childIndices, Object[] children) {
		super(source, path, childIndices, children);
		this.project = project;
		this.type = ProjectModelEventType.DEFAULT;
	}

	public ProjectModelEvent(AWorkspaceProject project, Object source, TreePath path, int[] childIndices, Object[] children) {
		super(source, path, childIndices, children);
		this.project = project;
		this.type = ProjectModelEventType.DEFAULT;
	}

	public ProjectModelEvent(AWorkspaceProject project, Object source, Object[] path) {
		this(project, source, new TreePath(path));
	}

	public ProjectModelEvent(AWorkspaceProject project, Object source, TreePath path) {
		this(project, source, path, ProjectModelEventType.DEFAULT, null, null);
	}

	public ProjectModelEvent(AWorkspaceProject project, Object source, TreePath path, ProjectModelEventType type, Object from, Object to) {
		super(source, path);
		this.project = project;
		this.type = type;
	}

	public AWorkspaceProject getProject() {
		return this.project;
	}
	
	public ProjectModelEventType getType() {
		return type;
	}	

	public enum ProjectModelEventType {
		DEFAULT,
		RENAMED,
		MOVED,
		DELETED
	}

}
