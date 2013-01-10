package org.docear.plugin.services.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Traverser {
	public enum TraversalMethod {
		BREADTH_FIRST, DEPTH_FIRST
	}

	private final TraversalMethod method;
	
	public Traverser(TraversalMethod method) {
		this.method = method;
	}
	
	abstract public boolean acceptElement(DocearXmlElement element, XmlPath path);
	
	public Collection<DocearXmlElement> traverse(DocearXmlElement element) {
		List<DocearXmlElement> acceptedElements = new ArrayList<DocearXmlElement>();		
		traverse(element, null, acceptedElements);
		return acceptedElements;
	}
		
	private void traverse(DocearXmlElement element, XmlPath path, Collection<DocearXmlElement> acceptedElements) {
		int count = 0;
		if(this.method.equals(TraversalMethod.BREADTH_FIRST)) {
			for(DocearXmlElement child : element.getChildren()) {
				if(acceptElement(child, new XmlPath(path, child.getName()+"["+count+"]"))) {
					acceptedElements.add(child);
				}
				count++;
			}
			count = 0;
			for(DocearXmlElement child : element.getChildren()) {
				traverse(child, new XmlPath(path, child.getName()+"["+count+"]"), acceptedElements);
				count++;
			}			
		}
		else {
			for(DocearXmlElement child : element.getChildren()) {
				XmlPath nPath = new XmlPath(path, child.getName()+"["+count+"]");
				if(acceptElement(child, nPath)) {
					acceptedElements.add(child);
				}
				traverse(child, nPath, acceptedElements);
				count++;
			}
		}
	}

}
