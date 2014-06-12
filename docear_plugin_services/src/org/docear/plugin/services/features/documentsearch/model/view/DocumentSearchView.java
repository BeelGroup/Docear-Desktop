package org.docear.plugin.services.features.documentsearch.model.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.NoSuchElementException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.features.recommendations.model.RecommendationsModel;
import org.docear.plugin.services.features.recommendations.view.RecommendationsView;
import org.freeplane.features.mode.Controller;

public class DocumentSearchView extends RecommendationsView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static DocumentSearchView view;

	public DocumentSearchView(RecommendationsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	public static DocumentSearchView getView() throws NoSuchElementException {
		Container cont = Controller.getCurrentController().getViewController().getContentPane();
		if(tabPane == null) {
			tabPane = findTabbedPane(cont);
		}
		
		if(view == null) {
			view = new DocumentSearchView();
			cont.remove(tabPane);
			cont.add(view, BorderLayout.CENTER, 0);
		}
		
		return view;
	}
	
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

	private Component getSearchPanel() {		
		JLabel label = new JLabel("test");
		
		return label;
	}

}
