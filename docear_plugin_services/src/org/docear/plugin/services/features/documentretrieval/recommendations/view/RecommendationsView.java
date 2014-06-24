package org.docear.plugin.services.features.documentretrieval.recommendations.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.features.documentretrieval.view.DocumentView;

public class RecommendationsView extends DocumentView {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
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
		
		this.add(getNewButtonBar(true), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new ListLayoutManager());
		panel.setBorder(new LineBorder(Color.GRAY, 1));		
		this.add(getNewButtonBar(true), BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		//this.add(panel);
		return panel;
	}

	@Override
	protected void addComponendAfterDocumentList(Container documentList) {
		// TODO Auto-generated method stub
		
	}

}
