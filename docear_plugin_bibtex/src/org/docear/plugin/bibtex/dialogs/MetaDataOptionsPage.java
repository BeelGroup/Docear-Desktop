package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.docear.plugin.core.actions.OpenLogsFolderAction;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.SwingConstants;

import java.awt.FlowLayout;
import java.net.URI;
import java.awt.Component;

public class MetaDataOptionsPage extends AWizardPage {
	
	public static final String DOCEAR_METADATA_MAX_RESULT = "docear_metadata_maxResult";
	public static final String DOCEAR_METADATA_DEBUG_LOGGING = "docear_metadata_debugLogging";
	public static final String DOCEAR_METADATA_SEARCH_DOCEAR = "docear_metadata_searchDocear";
	public static final String DOCEAR_METADATA_SEARCH_SCHOLAR = "docear_metadata_searchScholar";
	private static final long serialVersionUID = 1L;
	private JCheckBox checkBoxScholar;
	private JCheckBox checkBoxDocear;
	private JSpinner spinnerMaxResult;
	private JCheckBox checkBoxLogging;
	
	public MetaDataOptionsPage() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JLabel labelSources = new JLabel(TextUtils.getText("docear.metadata.extraction.sources.title"));
		add(labelSources, "2, 2");
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "2, 4, fill, fill");
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		scrollPane.setViewportView(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		checkBoxScholar = new JCheckBox(TextUtils.getText("docear.metadata.extraction.sources.scholar"));
		checkBoxScholar.setBackground(Color.WHITE);
		panel.add(checkBoxScholar, "2, 2");
		
		checkBoxDocear = new JCheckBox(TextUtils.getText("docear.metadata.extraction.sources.docear"));
		checkBoxDocear.setBackground(Color.WHITE);
		panel.add(checkBoxDocear, "4, 2");
		
		JLabel labelSearchOptions = new JLabel(TextUtils.getText("docear.metadata.extraction.options.title"));
		add(labelSearchOptions, "2, 6");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		add(scrollPane_1, "2, 8, fill, fill");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		scrollPane_1.setViewportView(panel_1);
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		MultiLineActionLabel labelMaxResult = new MultiLineActionLabel(TextUtils.getText("docear.metadata.extraction.options.maxResult"));
		labelMaxResult.setBackground(Color.WHITE);
		panel_1.add(labelMaxResult, "2, 2, left, default");
		
		spinnerMaxResult = new JSpinner();
		spinnerMaxResult.setModel(new SpinnerNumberModel(3, 1, 50, 1));
		spinnerMaxResult.setBackground(Color.WHITE);
		panel_1.add(spinnerMaxResult, "4, 2");
		
		MultiLineActionLabel labelLogging = new MultiLineActionLabel(TextUtils.getText("docear.metadata.extraction.options.logging"));
		labelLogging.setHorizontalAlignment(0);
		labelLogging.setAlignmentX(Component.LEFT_ALIGNMENT);
		labelLogging.setBackground(Color.WHITE);		
		labelLogging.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				if("logging_source".equals(e.getActionCommand())) {
					try {
						new OpenLogsFolderAction().actionPerformed(null);						
					}					
					catch (Exception ex) {
						LogUtils.warn(ex.getMessage());
					}
				}
			}
		});
		panel_1.add(labelLogging, "2, 4, left, default");
		
		checkBoxLogging = new JCheckBox("");
		checkBoxLogging.setBackground(Color.WHITE);
		checkBoxLogging.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(checkBoxLogging, "4, 4");
	}	

	@Override
	public String getTitle() {		
		return TextUtils.getText("docear.metadata.extraction.options.dialogtitle");
	}

	@Override
	public void preparePage(WizardSession session) {
		session.setWizardTitle(getTitle());
		session.getBackButton().setVisible(true);
		getRootPane().setDefaultButton((JButton)session.getNextButton());
		session.getNextButton().setText(TextUtils.getText("ok"));
		session.getBackButton().setText(TextUtils.getText("cancel"));
		this.checkBoxDocear.setVisible(false);
		final ResourceController properties = Controller.getCurrentController().getResourceController();
		this.checkBoxScholar.setSelected(properties.getBooleanProperty(DOCEAR_METADATA_SEARCH_SCHOLAR));
		this.checkBoxDocear.setSelected(properties.getBooleanProperty(DOCEAR_METADATA_SEARCH_DOCEAR));
		this.spinnerMaxResult.setModel(new SpinnerNumberModel(properties.getIntProperty(DOCEAR_METADATA_MAX_RESULT, 3), 1, 50, 1));
		this.checkBoxLogging.setSelected(properties.getBooleanProperty(DOCEAR_METADATA_DEBUG_LOGGING));
		
		session.getNextButton().addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				properties.setProperty(DOCEAR_METADATA_SEARCH_SCHOLAR, checkBoxScholar.isSelected());
				properties.setProperty(DOCEAR_METADATA_SEARCH_DOCEAR, checkBoxDocear.isSelected());
				properties.setProperty(DOCEAR_METADATA_MAX_RESULT, spinnerMaxResult.getModel().getValue().toString());
				properties.setProperty(DOCEAR_METADATA_DEBUG_LOGGING, checkBoxLogging.isSelected());
			}
		});
	}

}
