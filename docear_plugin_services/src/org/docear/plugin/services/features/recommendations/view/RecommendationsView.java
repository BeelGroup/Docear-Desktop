package org.docear.plugin.services.features.recommendations.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.util.NoSuchElementException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.features.recommendations.DocumentView;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;

public class RecommendationsView extends DocumentView {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static DocumentView view = null; 

	public static DocumentView getView() throws NoSuchElementException {
		Container cont = Controller.getCurrentController().getViewController().getContentPane();
		if(tabPane == null) {
			tabPane = findTabbedPane(cont);
		}
		
		if(view == null) {
			view = new RecommendationsView();
			cont.remove(tabPane);
			cont.add(view, BorderLayout.CENTER, 0);
		}
		
		return view;
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
		panel.setBackground(Color.white);
		panel.setLayout(new ListLayoutManager());
		panel.setBorder(new LineBorder(Color.GRAY, 1));
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		//this.add(panel);
		return panel;
	}

	@Override
	public void close() throws NoSuchElementException {
		Container cont = Controller.getCurrentController().getViewController().getContentPane();
		if(view == null) {
		return;
		}

		cont.remove(view);
		cont.add(tabPane, BorderLayout.CENTER, 0);
		view = null;
		((JComponent) cont).revalidate();
		UITools.getFrame().repaint();
	}	
}
