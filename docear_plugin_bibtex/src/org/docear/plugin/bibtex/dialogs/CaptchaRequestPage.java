package org.docear.plugin.bibtex.dialogs;

import org.docear.metadata.events.CaptchaEvent;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.freeplane.core.util.TextUtils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class CaptchaRequestPage extends AWizardPage {
	public CaptchaRequestPage() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		add(panel, "2, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("default:grow"),}));
		
		labelImage = new JLabel("");
		panel.add(labelImage, "1, 1");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		add(panel_1, "2, 4, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel labelTextfield = new JLabel(TextUtils.getText("docear.metadata.extraction.captcha.enter"));
		panel_1.add(labelTextfield, "1, 1, right, default");
		
		textField = new JTextField();
		panel_1.add(textField, "3, 1, fill, default");
		textField.setColumns(10);
	}
	
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JLabel labelImage;

	@Override
	public String getTitle() {		
		return TextUtils.getText("docear.metadata.extraction.captcha.title");
	}

	@Override
	public void preparePage(WizardSession session) {
		final CaptchaEvent event = session.get(CaptchaEvent.class);
		this.labelImage.setIcon(new ImageIcon(event.getCaptcha()));
		session.setWizardTitle(getTitle());
		session.getBackButton().setVisible(true);
		session.getNextButton().setText(TextUtils.getText("ok"));
		session.getBackButton().setText(TextUtils.getText("cancel"));	
		session.getNextButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				event.setSolvedCaptcha(textField.getText());				
			}
		});
		
		session.getBackButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				event.setCanceled(true);				
			}
		});
	}

}
