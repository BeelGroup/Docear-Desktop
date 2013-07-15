package org.docear.plugin.pdfutilities.features;

import java.net.URI;
import java.util.Locale;

public class AnnotationID {
	
	private String id;
	private final URI uri;
	private final long objectID;
	
	public AnnotationID(URI absoluteUri, long objectID) throws IllegalArgumentException{
		if(absoluteUri == null){
			throw new IllegalArgumentException(this.getClass().getName() + ": Uri can not be null."); //$NON-NLS-1$
		}
		this.uri = absoluteUri.normalize();
		
		String uri = this.uri.getPath().toLowerCase(Locale.ENGLISH).trim();
		this.id = uri + " " + Long.toString(objectID);
		this.objectID = objectID;
	}

	public String getId() {
		return id;
	}
	
	public URI getUri(){		
		return this.uri;
	}
	
	public long getObjectID(){
		return this.objectID;
	}
	
	public boolean equals(Object object){
		if(object instanceof AnnotationID) {
			if(this.getObjectID() == ((AnnotationID) object).getObjectID()) {
				return this.getUri().getPath().toLowerCase(Locale.ENGLISH).equals(((AnnotationID) object).getUri().getPath().toLowerCase(Locale.ENGLISH));
			}			
			return false;
		}
		else{
			return super.equals(object);
		}
	}
	
	public int hashCode(){		
		return this.getId().hashCode();
		
	}
	
	public String toString() {
		return getId();
	}
	
	

}
