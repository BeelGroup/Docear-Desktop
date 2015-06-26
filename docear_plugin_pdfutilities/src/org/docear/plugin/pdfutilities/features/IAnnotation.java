package org.docear.plugin.pdfutilities.features;

import java.net.URI;

import org.freeplane.core.extension.IExtension;

public interface IAnnotation extends IExtension{
	
	public static enum AnnotationType{
		BOOKMARK, COMMENT, HIGHLIGHTED_TEXT, BOOKMARK_WITHOUT_DESTINATION, BOOKMARK_WITH_URI, PDF_FILE, FILE, TRUE_HIGHLIGHTED_TEXT
	};
	
	public AnnotationID getAnnotationID();
	
//	public void setAnnotationID(AnnotationID id);

	public AnnotationType getAnnotationType();

	public void setAnnotationType(AnnotationType annotationType);

	public Integer getPage();

	public void setPage(Integer page);
	
	public void updatePage();

	public long getObjectID();

//	public void setObjectID(long objectNumber);

	public String getTitle();	

	public void setTitle(String title);	

	public URI getDestinationUri();

	public void setDestinationUri(URI uri);	
	
	public URI getSource();

	public boolean isNew();	

	public boolean hasNewChildren();

	public void setConflicted(boolean isConflicted);

	public boolean isConflicted();	

}