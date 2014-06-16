package org.docear.plugin.services.features.documentretrieval.model;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.event.TreeModelListener;

import org.docear.plugin.services.ServiceController;
import org.freeplane.core.util.TextUtils;

public class DocumentsModel {

	private DocumentModelNode rootNode;
	private String evaluationLabel = "";
	private int id = 0;
	
	public DocumentsModel(Collection<DocumentEntry> recommendations) {
		new DocumentsModel();
		parseRecommendations(recommendations);
	}
	
	public DocumentsModel() {
		
	}
	
	private void parseRecommendations(Collection<DocumentEntry> recommendations) {
		if(recommendations == null) {
			if(ServiceController.getCurrentUser().isRecommendationsEnabled()) {
				setRoot(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("recommendations.error.no_recommendations")));
			}
			else {
				setRoot(DocumentModelNode.createNoServiceNode());
			}
			return;
		}
		
		
		Iterator<DocumentEntry> entries = recommendations.iterator();
		// small hack: first element in collection is xml-element "recommendations"
		DocumentEntry recommendationsElement = entries.next();
		String rootTitle = recommendationsElement.getTitle();
		evaluationLabel = recommendationsElement.getEvaluationLabel();
		id = recommendationsElement.getSetId();
		
		if (rootTitle != null && rootTitle.trim().length() > 0) {
			setRoot(DocumentModelNode.createRecommendationContainerNode(rootTitle));
		}
		else {
			//fallback to standard title
			setRoot(DocumentModelNode.createRecommendationContainerNode());
		}
		
		if(recommendations.isEmpty()) {
			getRootNode().insert(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("recommendations.error.no_recommendations")));
		} 
		else {
			while (entries.hasNext()) {
				getRootNode().insert(DocumentModelNode.createRecommendationEntryNode(entries.next()));
			}		
		}
	}
	
	public String getTitle() {
		String label = ServiceController.getCurrentUser().getName();
		if(label != null && label.trim().length() > 0) {
			return TextUtils.format("recommendations.map.label.forUser", label);
		}
		return TextUtils.getText("recommendations.map.label.anonymous");
	}
	
	public String getEvaluationLabel() {
		return evaluationLabel ;
	}

	public void setRoot(DocumentModelNode root) {
		this.rootNode = root;		
	}

	public void addTreeModelListener(TreeModelListener l) {
		
	}
	
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	public DocumentModelNode getChild(DocumentModelNode parent, int index) {
		if(parent == null) {
			return null;
		}
		return parent.getChildAt(index);
	}

	public int getChildCount(DocumentModelNode parent) {
		if(parent == null) {
			return 0;
		}
		return parent.getChildCount();
	}

	public DocumentModelNode getRootNode() {
		return this.rootNode;
	}

	public boolean isLeaf(DocumentModelNode node) {
		if(node == null) {
			return true;
		}
		return node.isLeaf();
	}

	public int getIndexOfChild(DocumentModelNode parent, DocumentModelNode child) {
		if(parent == null) {
			return -1;
		}
		return parent.getIndex(child);
	}

	public int getSetId() {
		return id;
	}
}
