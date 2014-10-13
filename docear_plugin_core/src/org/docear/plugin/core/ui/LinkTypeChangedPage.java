package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LinkTypeChangedPage extends AWizardPage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MultiLineActionLabel warning;
	
	public LinkTypeChangedPage() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(573, 122));
		warning = new MultiLineActionLabel(TextUtils.getText("page.link_type.changed.message"));
		warning.setBackground(Color.WHITE);
		warning.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if ("open_url".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(new URI("http://www.docear.org/support/forums/docear-support-forums-group3/general-feedback-questions-forum5/"));
					}
					catch (Exception ex) {
						LogUtils.warn("LinkTypeChangedPage.LinkTypeChangedPage().new ActionListener() {...}.actionPerformed(): " + ex.getMessage());
					}
				}
			}
		});
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("6px:grow"),},
			new RowSpec[] {
				RowSpec.decode("70px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		add(warning, "1, 1, left, top");
		
		JHyperlink hyperlink = new JHyperlink(TextUtils.getRawText("page.link_type.changed.forum"), "http://www.docear.org/support/forums/docear-support-forums-group3/general-feedback-questions-forum5/");
		add(hyperlink, "1, 3");
	}

	@Override
	public String getTitle() {		
		return TextUtils.getText("page.link_type.changed.title");
	}

	@Override
	public void preparePage(WizardSession context) {
		context.getNextButton().setText(TextUtils.getText("ok"));
	}

}
