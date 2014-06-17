package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.view.DocumentView;

public class DocumentSearchView extends DocumentView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private DocumentSearchPanel documentSearchPanel = new DocumentSearchPanel();
	
	public DocumentSearchView(DocumentsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	@Override
	protected Container getNewRecommandationContainerComponent(String title) {
//		JPanel containerPanel = new JPanel();
//		containerPanel.setLayout(new BorderLayout());
//		containerPanel.setBackground(Color.WHITE);
//		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
//		JPanel panel = new JPanel();
//				
//		JLabel containerTitle = new JLabel("<html><b>"+title+"</b></html>");
//		containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 18));
//		
//		containerPanel.add(containerTitle, BorderLayout.NORTH);
//		
//		panel.setBackground(Color.WHITE);
//		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
//		panel.setLayout(new ListLayoutManager());		
//		containerPanel.add(panel, BorderLayout.CENTER);
//		
//		this.add(getNewButtonBar(), BorderLayout.NORTH);
//		this.add(containerPanel, BorderLayout.CENTER);
//		this.add(getStarBar(), BorderLayout.SOUTH);
		
//		return panel
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
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}

	public Component getSearchPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getNewButtonBar(), BorderLayout.NORTH);		
		panel.add(documentSearchPanel, BorderLayout.CENTER);
//		return new DocumentSearchPanel();
		return panel;
	}
	
	public String getQueryText() {
		return this.documentSearchPanel.getQueryText();
	}

}
