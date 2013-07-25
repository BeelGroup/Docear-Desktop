package org.docear.plugin.pdfutilities.features;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.freeplane.core.util.LogUtils;


public class AnnotationModel implements IAnnotation{
	
	private AnnotationID id;
	private AnnotationType annotationType;
	private AnnotationModel parent;
	private Integer page;
	private URI destinationUri;	
	private String title;
	private final long objectID;
	private int oldObjectNumber = -1;
	private URI uri;
	private Object annotationObject;	
	
	private List<AnnotationModel> children = new ArrayList<AnnotationModel>();
	
	private boolean isConflicted;
	private boolean isNew;
	private boolean isInserted;
	private boolean isNewID = false;
	
	public AnnotationModel(long id){
		this(id, null);
	}
	
	public AnnotationModel(long id, AnnotationType type){
		this.objectID = id;
		this.annotationType = type;
	}
	
	public AnnotationID getAnnotationID() {	
		if(id == null && uri != null){
			id = new AnnotationID(uri, objectID);
			id.setIsNewID(this.isNewID);
			id.setObjectNumber(this.oldObjectNumber);
		}
		return id;
	}
	
	public AnnotationType getAnnotationType() {
		return annotationType;
	}
	
	public void setAnnotationType(AnnotationType annotationType) {
		this.annotationType = annotationType;
	}
	
	public Integer getPage() {
		return page;
	}
	
	public void updatePage() {
		try {
			IAnnotation annotation = new PdfAnnotationImporter().searchAnnotation(this);
			if(annotation != null && annotation.getPage() != null){
				this.page = annotation.getPage();
			}
			else{
				LogUtils.warn("Could not update Page!");
			}
		}
		catch (Exception e) {
			LogUtils.warn("Could not update Page!", e);
		}
	}
	
	public void setPage(Integer page) {
		this.page = page;
	}
	
	public long getObjectID() {
		return this.objectID;
	}
//	
//	public void setObjectID(Integer objectNumber) {
//		this.objectNumber = objectNumber;
//		if(this.uri != null){
//			this.id = new AnnotationID(this.getUri(), objectNumber);
//		}
//	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}	
		
	public URI getDestinationUri() {
		return destinationUri;
	}
	
	public void setDestinationUri(URI uri) {
		this.destinationUri = uri;
	}	
	
	public List<AnnotationModel> getChildren() {
		return children;
	}
	
	public void setNew(boolean isNew){
		this.isNew = isNew;
	}	
	
	public boolean isNew(){
		return this.isNew;
	}	
	
	public boolean isConflicted() {
		return isConflicted;
	}
	
	public void setConflicted(boolean isConflicted) {
		this.isConflicted = isConflicted;
	}
	
	public String toString(){
		return this.getTitle();
	}
	
	public URI getSource() {
		return this.uri;
	}
	
	public void setSource(URI absoluteUri){
		this.uri = absoluteUri;
		if(absoluteUri == null) {
			this.id = null;
		}
		else {
			this.id = null;
			getAnnotationID();
		}
		
	}
	
	public boolean hasNewChildren(){
		for(IAnnotation child : this.children){
			if(child.isNew() || child.hasNewChildren()){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasConflictedChildren(){
		for(AnnotationModel child : this.children){
			if(child.isConflicted() || child.hasConflictedChildren()){
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasConflicts(Collection<AnnotationModel> annotations){
		for(AnnotationModel model : annotations){
			if(model.isConflicted || model.hasConflictedChildren()){
				return true;
			}
		}
		return false;
	}

	public Object getAnnotationObject() {
		return annotationObject;
	}

	public void setAnnotationObject(Object annotationObject) {
		this.annotationObject = annotationObject;
	}

	public String getDocumentHash() {
		return AnnotationController.getDocumentHash(getSource());
	}
	
	public String getDocumentTitle() {
		return AnnotationController.getDocumentTitle(getSource());
	}

	public AnnotationModel getParent() {
		return parent;
	}

	public void setParent(AnnotationModel parent) {
		this.parent = parent;
	}

	public boolean isInserted() {
		return isInserted;
	}

	public void setInserted(boolean isInserted) {
		this.isInserted = isInserted;
	}
	
	public int getChildIndex(AnnotationModel child){
		return this.children.indexOf(child);
	}

	public void setOldObjectNumber(int number) {
		this.oldObjectNumber  = number;
		if(getAnnotationID() != null) {
			id.setObjectNumber(oldObjectNumber);
		}
		
	}
	
	public int getOldObjectNumber() {
		return oldObjectNumber;
	}

	public void setIsNewID(boolean isNewID) {
		this.isNewID = isNewID;
		if(getAnnotationID() != null) {
			id.setIsNewID(this.isNewID);
		}
	}
	
}
