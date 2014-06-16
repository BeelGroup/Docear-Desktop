package org.docear.plugin.services.features.documentretrieval.model;

import java.util.ArrayList;
import java.util.List;


public class DocumentModelNode {
	
	private List<DocumentModelNode> children = new ArrayList<DocumentModelNode>();
	private Object userObject = null;
	private DocumentModelNode parent;
	
	private DocumentModelNode(DocumentEntry recommendation) {
		setUserObject(recommendation);		
	}
	
	private DocumentModelNode() {
		
	}

	@Override
	public String toString() {
		return getText();
	}

	

	private String getText() {
		if(userObject == null) {
			return null;
		}
		return userObject.toString();
	}

	public List<DocumentModelNode> getChildren() {
		return this.children;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public DocumentModelNode getChildAt(int index) {
		synchronized (children) {
			return children.get(index);
		}		
	}

	public int getChildCount() {
		synchronized (children) {
			return children.size();
		}		
	}

	public int getIndex(DocumentModelNode child) {
		if(child == null) {
			return -1;
		}
		synchronized (children) {
			return children.indexOf(child);
		}
	}

	public DocumentModelNode getParent() {
		return this.parent;
	}

	public void insert(DocumentModelNode child, int index) {
		if(child == null) {
			return;
		}
		synchronized (children) {
			children.add(index, child);
		}		
	}

	public void remove(int index) {
		synchronized (children) {
			children.remove(index);
		}
	}

	public void remove(DocumentModelNode node) {
		if(node == null) {
			return;
		}
		synchronized (children) {
			children.remove(node);
		}
	}

	public void removeFromParent() {
		if(parent == null) {
			return;
		}
		parent.remove(this);
	}

	public void setParent(DocumentModelNode newParent) {
		this.parent = newParent;
	}

	public void setUserObject(Object object) {
		this.userObject = object;		
	}
	
	public Object getUserObject() {
		return this.userObject;
	}

	public boolean isLeaf() {
		return children.size() <= 0;
	}

	public void insert(DocumentModelNode node) {
		synchronized (children) {
			children.add(node);
		}		
	}

	public static DocumentModelNode createNoRecommendationsNode(String text) {
		NoRecommendationsNode node = new DocumentModelNode().new NoRecommendationsNode(text);
		return node;
	}
	
	public static DocumentModelNode createNoServiceNode() {
		NoServiceNode node = new DocumentModelNode().new NoServiceNode();
		return node;
	}
	
	public static DocumentModelNode createRecommendationContainerNode(String name) {
		RecommendationContainerNode node = new DocumentModelNode().new RecommendationContainerNode(name);
		return node;
	}
	
	public static DocumentModelNode createRecommendationContainerNode() {
		UntitledRecommendationContainerNode node = new DocumentModelNode().new UntitledRecommendationContainerNode();
		return node;
	}
	
	public static DocumentModelNode createRecommendationEntryNode(DocumentEntry entry) {
		RecommendationEntryNode node = new DocumentModelNode().new RecommendationEntryNode(entry);
		return node;
	}
	
	public class NoRecommendationsNode extends DocumentModelNode {

		public NoRecommendationsNode(String text) {
			super();
			setUserObject(text);
		}

	}
	
	public class NoServiceNode extends DocumentModelNode {

		public NoServiceNode() {
			super();
			setUserObject(null);
		}

	}
	
	public class RecommendationContainerNode extends DocumentModelNode {

		public RecommendationContainerNode(String title) {
			super();
			setUserObject(title);
		}

	}
	
	public class UntitledRecommendationContainerNode extends DocumentModelNode {

		public UntitledRecommendationContainerNode() {
			super();
			setUserObject(null);
		}

	}
	
	public class RecommendationEntryNode extends DocumentModelNode {
		public RecommendationEntryNode(DocumentEntry entry) {
			super(entry);
		}
	}
}
