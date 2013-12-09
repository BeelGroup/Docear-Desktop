package org.docear.plugin.pdfutilities.features;

import java.net.URI;
import java.util.Locale;

public class AnnotationID {
	
	private String id;
	private final URI uri;
	private final long objectID;
	private boolean isCreated;
	private int internalObjectNumber;
	
	public AnnotationID(URI absoluteUri, long objectID) throws IllegalArgumentException{
		if(absoluteUri == null){
			throw new IllegalArgumentException(this.getClass().getName() + ": Uri can not be null."); //$NON-NLS-1$
		}
		this.uri = absoluteUri.normalize();
		this.objectID = objectID;
	}

	public String getId() {
		if(id == null) {
			String uri = this.uri.getPath().toLowerCase(Locale.ENGLISH).trim();
			this.id = uri + " " + Long.toString(objectID);
		}
		return id;
	}
	
	public URI getUri(){		
		return this.uri;
	}
	
	public long getObjectID(){
		return this.objectID;
	}
	
	public boolean isNewCreated() {
		return isCreated;
	}
	
	public boolean equals(Object object){
		if(object instanceof AnnotationID) {
			boolean firstStepCleared = false;
			AnnotationID other = (AnnotationID) object;
			//first compare the UIDs
			if(this.getObjectID() > -1 && other.getObjectID() > -1 && this.getObjectID() == other.getObjectID()) {
					firstStepCleared = true;
			}
			else {
				//if the UIDs not match and one of them was fresh generated try to compare the object numbers
				if(isCreated || ((AnnotationID) object).isCreated) {
					if(this.internalObjectNumber == other.internalObjectNumber) {
						firstStepCleared = true;
					}
				}
			}
			//compare the document association
			if(firstStepCleared) {
				return this.getUri().getPath().toLowerCase(Locale.ENGLISH).equals(((AnnotationID) object).getUri().getPath().toLowerCase(Locale.ENGLISH));
			}
			return false;
		}
		else{
			return super.equals(object);
		}
	}
	
	public int hashCode() {		
		return this.getUri().toString().hashCode();//this.getId().hashCode();
	}
	
	public String toString() {
		return getId();
	}

	public void setIsNewID(boolean isNewID) {
		this.isCreated = isNewID;
	}

	public void setObjectNumber(int number) {
		this.internalObjectNumber = number;
	}
	
	

}
