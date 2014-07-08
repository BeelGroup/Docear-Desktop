package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.DocumentSearchController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.SearchModel;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.view.DocumentView;

public class DocumentSearchView extends DocumentView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DocumentSearchPanel documentSearchPanel;
	
	public DocumentSearchView(DocumentsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	@Override
	// not used for document search engine
	protected Container getNewRecommandationContainerComponent(String title) {
		return new JPanel();
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.CENTER);
		
		this.add(getSearchPanel(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		
//		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}

	public Component getSearchPanel() {		
		JPanel panel = new JPanel();
				
		panel.setLayout(new BorderLayout());
		panel.add(getNewButtonBar(false), BorderLayout.NORTH);
		
		if (ServiceController.getCurrentUser().isRecommendationsEnabled())  {
			SearchModel searchModel = DocumentSearchController.getController().getSearchModel();		
    		if (searchModel != null && searchModel.getId() != null) {
    			documentSearchPanel = new DocumentSearchPanel(searchModel.getModel().split(" "), searchModel.getId());			
    		}
    		else {
    			documentSearchPanel = new DocumentSearchPanel();
    		}
    		panel.add(documentSearchPanel, BorderLayout.CENTER);
		}
		
		return panel;
	}
		
	private Container getPaginator() {
		JPanel paginator = new JPanel();		
		paginator.setLayout(new BoxLayout(paginator,BoxLayout.X_AXIS));
		paginator.setBackground(Color.WHITE);
		paginator.add(Box.createHorizontalGlue());
		
		int documentsavailable = DocumentRetrievalController.getController().getDocumentsAvailable();
		int pages = (int) Math.ceil(Float.valueOf(documentsavailable) / 10F);
		
		// paginator buttons not necessary for only one page
		if (pages <= 1) {
			return null;
		}
		
		for (int i=1; i<=pages; i++) {
			JButton page = new JButton(String.valueOf(i));
			if (i == DocumentSearchController.getController().getPage()) {
				page.setEnabled(false);
			}
			
			page.setBackground(Color.WHITE);
			page.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					DocumentSearchController.getController().setPage(Integer.valueOf(e.getActionCommand()));
					DocumentSearchController.getController().search(DocumentSearchController.getController().getQuery());
				}
			});
			paginator.add(page);
		}
		paginator.add(Box.createHorizontalGlue());
		
		return paginator;
	}

	@Override
	protected void addComponendAfterDocumentList(Container documentList) {
		Container container = getPaginator();
		if (container != null) {
			documentList.add(getPaginator());
		}
	}

}
