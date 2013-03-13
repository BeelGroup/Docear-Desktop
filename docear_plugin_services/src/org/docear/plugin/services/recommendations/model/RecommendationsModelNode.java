package org.docear.plugin.services.recommendations.model;

import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.services.recommendations.RecommendationEntry;

public class RecommendationsModelNode {
	
	private List<RecommendationsModelNode> children = new ArrayList<RecommendationsModelNode>();
	private Object userObject = null;
	private RecommendationsModelNode parent;
	
	private RecommendationsModelNode(RecommendationEntry recommendation) {
		setUserObject(recommendation);		
	}
	
	private RecommendationsModelNode() {
		
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

	public List<RecommendationsModelNode> getChildren() {
		return this.children;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public RecommendationsModelNode getChildAt(int index) {
		synchronized (children) {
			return children.get(index);
		}		
	}

	public int getChildCount() {
		synchronized (children) {
			return children.size();
		}		
	}

	public int getIndex(RecommendationsModelNode child) {
		if(child == null) {
			return -1;
		}
		synchronized (children) {
			return children.indexOf(child);
		}
	}

	public RecommendationsModelNode getParent() {
		return this.parent;
	}

	public void insert(RecommendationsModelNode child, int index) {
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

	public void remove(RecommendationsModelNode node) {
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

	public void setParent(RecommendationsModelNode newParent) {
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

	public void insert(RecommendationsModelNode node) {
		synchronized (children) {
			children.add(node);
		}		
	}

	public static RecommendationsModelNode createNoRecommendationsNode(String text) {
		NoRecommendationsNode node = new RecommendationsModelNode().new NoRecommendationsNode(text);
		return node;
	}
	
	public static RecommendationsModelNode createNoServiceNode() {
		NoServiceNode node = new RecommendationsModelNode().new NoServiceNode();
		return node;
	}
	
	public static RecommendationsModelNode createRecommendationContainerNode(String name) {
		RecommendationContainerNode node = new RecommendationsModelNode().new RecommendationContainerNode(name);
		return node;
	}
	
	public static RecommendationsModelNode createRecommendationContainerNode() {
		UntitledRecommendationContainerNode node = new RecommendationsModelNode().new UntitledRecommendationContainerNode();
		return node;
	}
	
	public static RecommendationsModelNode createRecommendationEntryNode(RecommendationEntry entry) {
		RecommendationEntryNode node = new RecommendationsModelNode().new RecommendationEntryNode(entry);
		return node;
	}
	
	public class NoRecommendationsNode extends RecommendationsModelNode {

		public NoRecommendationsNode(String text) {
			super();
			setUserObject(text);
		}

	}
	
	public class NoServiceNode extends RecommendationsModelNode {

		public NoServiceNode() {
			super();
			setUserObject(null);
		}

	}
	
	public class RecommendationContainerNode extends RecommendationsModelNode {

		public RecommendationContainerNode(String title) {
			super();
			setUserObject(title);
		}

	}
	
	public class UntitledRecommendationContainerNode extends RecommendationsModelNode {

		public UntitledRecommendationContainerNode() {
			super();
			setUserObject(null);
		}

	}
	
	public class RecommendationEntryNode extends RecommendationsModelNode {
		public RecommendationEntryNode(RecommendationEntry entry) {
			super(entry);
		}
	}
}
