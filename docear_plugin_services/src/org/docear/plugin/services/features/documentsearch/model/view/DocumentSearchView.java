package org.docear.plugin.services.features.documentsearch.model.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.features.recommendations.DocumentView;
import org.docear.plugin.services.features.recommendations.model.RecommendationsModel;

public class DocumentSearchView extends DocumentView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DocumentSearchView(RecommendationsModel model) {
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
		
		containerPanel.add(getSearchPanel(), BorderLayout.NORTH);
		containerPanel.add(containerTitle, BorderLayout.CENTER);
		
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.SOUTH);
		
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		JPanel panel = new JPanel();
				
		JLabel containerTitle = new JLabel("<html><b>BLAAAA</b></html>");
		containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 18));
		
		containerPanel.add(getSearchPanel(), BorderLayout.NORTH);
		containerPanel.add(containerTitle, BorderLayout.CENTER);
		
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.SOUTH);
		
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		this.add(getStarBar(), BorderLayout.SOUTH);
		
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
