package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.services.features.documentretrieval.documentsearch.DocumentSearchController;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocumentSearchPanel extends JPanel {
	final private JTextField searchQueryField = new JTextField();
	
	public DocumentSearchPanel() {
		this(null, null);
	}
	
	public DocumentSearchPanel(String[] searchModel, final Long searchModelId) {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("min:grow"),}));
		
		
		
		setBackground(Color.WHITE);
		setBorder(new EmptyBorder(0, 10, 0, 0));
		
		searchQueryField.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				newSearch();
			}
		});
		
		add(searchQueryField, "2, 2, fill, fill");
		searchQueryField.setText(DocumentSearchController.getController().getQuery());
		
		JButton searchButton = new JButton(TextUtils.getText("documentsearch.search"));
		searchButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				newSearch();
			}
		});
		add(searchButton, "4, 2");
		
		if (searchModel != null) {
    		JPanel panel = getButtonPanel(searchModel);
    		add(panel, "2, 4, 3, 1, fill, fill");
		}
	}
	
	private void newSearch() {
		DocumentSearchController.getController().setPage(1);
		DocumentSearchController.getController().setDocumentsSetId(null);
		DocumentSearchController.getController().search(searchQueryField.getText());
	}

	private JPanel getButtonPanel(String[] searchModel) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));	
		buttonPanel.setBackground(Color.WHITE);

		for (String s : searchModel) {
			JButton searchTermButton = new JButton(s);
			searchTermButton.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					searchQueryField.setText(searchQueryField.getText()+" "+e.getActionCommand());
				}
			});			
			buttonPanel.add(searchTermButton);
		}
		buttonPanel.setMinimumSize(new Dimension(0, 100));
		return buttonPanel;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
