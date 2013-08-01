package org.docear.plugin.pdfutilities.features;

import java.net.URI;

import org.freeplane.features.map.NodeModel;

public class AnnotationNodeModel implements IAnnotation {
	
	private final NodeModel node;
	private final IAnnotation wrappedModel;

	public AnnotationNodeModel(NodeModel node, IAnnotation model) {
		if (node == null || model == null) {
			throw new IllegalArgumentException("NULL");
		}
		this.node = node;
		this.wrappedModel = model;
	}
	
	public NodeModel getNode() {
		return node;
	}

	public String getTitle() {
		return node.getText();
	}

	public void setTitle(String title) {
		this.node.setText(title);
	}

	public AnnotationID getAnnotationID() {
		return wrappedModel.getAnnotationID();
	}

	public AnnotationType getAnnotationType() {
		return wrappedModel.getAnnotationType();
	}

	public void setAnnotationType(AnnotationType annotationType) {
		wrappedModel.setAnnotationType(annotationType);
	}

	public Integer getPage() {
		return wrappedModel.getPage();
	}

	public void setPage(Integer page) {
		wrappedModel.setPage(page);
	}

	public void updatePage() {
		wrappedModel.updatePage();
	}

	public long getObjectID() {
		return wrappedModel.getObjectID();
	}

	public URI getDestinationUri() {
		return wrappedModel.getDestinationUri();
	}

	public void setDestinationUri(URI uri) {
		wrappedModel.setDestinationUri(uri);
	}

	public URI getSource() {
		return wrappedModel.getSource();
	}

	public boolean isNew() {
		return wrappedModel.isNew();
	}

	public boolean hasNewChildren() {
		return wrappedModel.hasNewChildren();
	}

	public void setConflicted(boolean isConflicted) {
		wrappedModel.setConflicted(isConflicted);
	}

	public boolean isConflicted() {
		return wrappedModel.isConflicted();
	}
	
	public String toString() {
		return this.getTitle();
	}
}
