package org.docear.plugin.services.features.documentretrieval.model;

import java.util.Iterator;

import javax.swing.event.TreeModelListener;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.DocumentSearchController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.view.DocumentSearchView;
import org.freeplane.core.util.TextUtils;

public class DocumentsModel {

	private DocumentModelNode rootNode;
	private String evaluationLabel = "";
	private int id = 0;
	
	public DocumentsModel(DocumentEntries documentEntries) {
		new DocumentsModel();
		parseRecommendations(documentEntries);
	}
	
	public DocumentsModel() {
		
	}
	
	private void parseRecommendations(DocumentEntries documentEntries) {
		if(documentEntries == null || documentEntries.getDocumentEntries().size()==0) {
			if(ServiceController.getCurrentUser().isRecommendationsEnabled()) {
				if (DocumentRetrievalController.getView() != null && DocumentRetrievalController.getView() instanceof DocumentSearchView) {
					if (DocumentSearchController.getController().getQuery().trim().length() == 0) {
						setRoot(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("documentsearch.error.no_search_terms")));
					}
					else {
						setRoot(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("documentsearch.error.no_search_documents")));
					}
				}
				else {
					setRoot(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("recommendations.error.no_recommendations")));
				}
			}
			else {
				setRoot(DocumentModelNode.createNoServiceNode());
			}
			return;
		}
		
		
		Iterator<DocumentEntry> entries = documentEntries.getDocumentEntries().iterator();
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
		
		if(documentEntries.getDocumentEntries().isEmpty()) {			
			if (DocumentRetrievalController.getView() != null && DocumentRetrievalController.getView() instanceof DocumentSearchView) {
				getRootNode().insert(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("documentsearch.error.no_search_terms")));
			}
			else {
				getRootNode().insert(DocumentModelNode.createNoRecommendationsNode(TextUtils.getText("recommendations.error.no_recommendations")));
			}
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
