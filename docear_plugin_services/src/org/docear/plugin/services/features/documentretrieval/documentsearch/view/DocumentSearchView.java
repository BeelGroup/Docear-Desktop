package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
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
	
	public DocumentSearchView(DocumentsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	@Override
	protected Container getNewRecommandationContainerComponent(String title) {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		JPanel panel = new JPanel();
				
		JLabel containerTitle = new JLabel("<html><b>"+title+"</b></html>");
		containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 18));
		
		containerPanel.add(containerTitle, BorderLayout.NORTH);
		
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.CENTER);
		
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new ListLayoutManager()); 
		panel.add(getSearchPanel());
		
		JPanel documentListPanel = new JPanel();
		documentListPanel.setBackground(Color.white);
		documentListPanel.setLayout(new ListLayoutManager());
		documentListPanel.setBorder(new LineBorder(Color.GRAY, 1));
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		panel.add(documentListPanel);
		
		//this.add(panel);
		return panel;
	}

	private Component getSearchPanel() {
		System.out.println("++++++++++++++++++++++++++test++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		JLabel label = new JLabel("test");
		
		return label;
	}

}
