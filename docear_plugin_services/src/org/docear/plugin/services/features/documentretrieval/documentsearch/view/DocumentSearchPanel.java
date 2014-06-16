package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.DocumentSearchController;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocumentSearchPanel extends JPanel {
	public DocumentSearchPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		setBackground(Color.WHITE);
		setBorder(new EmptyBorder(0, 10, 0, 0));
		
		final JTextArea searchQueryArea = new JTextArea();
		add(searchQueryArea, "2, 2, fill, fill");
		
		JButton searchButton = new JButton(TextUtils.getText("search"));
		searchButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				String query = searchQueryArea.getText();
				if (query == null) {
					return;
				}
					
				query = query.trim().toLowerCase();
				if (query.length() == 0) {
					return;
				}
				
				DocumentSearchController.getController().setQuery(query);
				DocumentSearchController.getController().refreshDocuments();
				
			}
		});
		add(searchButton, "4, 2");
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
