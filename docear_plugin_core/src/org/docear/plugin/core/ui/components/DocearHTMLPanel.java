package org.docear.plugin.core.ui.components;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class DocearHTMLPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JEditorPane htmlField;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public DocearHTMLPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("450px:grow"),},
			new RowSpec[] {
				RowSpec.decode("300px:grow"),}));
		setPreferredSize(new Dimension(500, 400));
		
		htmlField = new JEditorPane();
		htmlField.setEditable(false);
		
		// add an html editor kit
		HTMLEditorKit kit = new HTMLEditorKit();
		htmlField.setEditorKit(kit);
		
		Document doc = kit.createDefaultDocument();
		htmlField.setDocument(doc);
		
		JScrollPane scrollPane = new JScrollPane(htmlField);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, "1, 1, fill, fill");
		
		htmlField.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
					try {
						Controller.getCurrentController().getViewController().openDocument(e.getURL().toURI());
					} catch (Exception ex) {
						LogUtils.warn(ex.getMessage());
					}
				}
			}
		});
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setText(String text) {
		if(text == null) {
			text = "";
		}
		htmlField.setText(text);
		htmlField.setSelectionStart(0);
		htmlField.setSelectionEnd(0);
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
