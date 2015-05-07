package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.export.ExportFormats;
import net.sf.jabref.export.layout.LayoutHelper;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ImportFormatReader;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PdfXmpImporter;
import net.sf.jabref.util.Pair;

import org.docear.metadata.MetaDataSearchHub;
import org.docear.metadata.data.MetaData;
import org.docear.metadata.data.MetaData.AbstractSource;
import org.docear.metadata.data.MetaDataSource;
import org.docear.metadata.data.ScholarMetaData;
import org.docear.metadata.data.ScholarMetaData.ScholarSource;
import org.docear.metadata.engines.GoogleScholarSearchEngine;
import org.docear.metadata.events.CaptchaEvent;
import org.docear.metadata.events.FetchedResultsEvent;
import org.docear.metadata.events.MetaDataEvent;
import org.docear.metadata.events.MetaDataListener;
import org.docear.metadata.extractors.ExtractorConfigKey;
import org.docear.metadata.extractors.HtmlDataExtractor.CommonConfigKeys;
import org.docear.metadata.extractors.MalformedConfigException;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.actions.MetaDataAction.MetaDataActionObject;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.IPageKeyBindingProcessor;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.services.ServiceController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MetaDataExtractorPage extends AWizardPage {
	
	private static final String DOCEAR_METADATA_CREATE_ATTACH_ONLY = "docear_metadata_createAttachOnly";
	public static final String DOCEAR_METADATA_SEARCH_BY_FILE = "docear_metadata_searchByFile";
	public static final String DOCEAR_METADATA_SEARCH_BY_TITLE = "docear_metadata_searchByTitle";
	public static final String DOCEAR_METADATA_SEARCH_OPTION = "docear_metadata_searchOption";
	public static final String DOCEAR_METADATA_CREATE_XMP_DATA_ENTRY = "docear_metadata_createXmpDataEntry";
	public static final String DOCEAR_METADATA_CREATE_EMPTY_ENTRY = "docear_metadata_createEmptyEntry";
	public static final String DOCEAR_METADATA_CREATE_FETCHED_DATA_ENTRY = "docear_metadata_createFetchedDataEntry";
	public static final String DOCEAR_METADATA_CREATE_ENTRY_OPTION = "docear_metadata_createEntryOption";
	private static final long serialVersionUID = 1L;
	private JTextField textFieldSearch;
	private JRadioButton radioButton_createBlank;
	private JRadioButton radioButton_createFetched;
	private JButton buttonLookup;
	private JButton button_Settings;
	private JRadioButton radioButton_CreateXmp;
	private JRadioButton radioButton_searchFile;
	private MultiLineActionLabel actionLabel_File;
	
	private URI pdfFile;
	private String pdfFileName;
	private String pdfTitle;
	private List<Pair<BibtexEntry,MetaDataSource>> xmpData = new ArrayList<Pair<BibtexEntry,MetaDataSource>>();
	private JLabel labelSearchBy;
	private JLabel actionLabel_Title;
	private JRadioButton radioButton_searchTitle;
	private JPanel panel;
	private JScrollPane scrollPaneXmpData;
	private JList listXmpData;
	private JScrollPane scrollPaneFetchedResults;
	private JList listFetchedResults;
	private BibtexEntryListModel listModelFetchedResults;
	private WizardSession session;
	private MetaDataSearchHub searchHub = new MetaDataSearchHub();
	private String searchValue = "";
	private JLabel labelWarning;
	private JRadioButton radioButtonAttachOnly;
	private JLabel labelSpinner;
	private JLabel labelStatustext;
	private int requestCount;
	private JLabel labelSearch;

	public MetaDataExtractorPage() {		
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("default:grow"),
				RowSpec.decode("4dlu:grow"),
				RowSpec.decode("default:grow"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		radioButton_createBlank = new JRadioButton(TextUtils.getText("docear.metadata.extraction.createBlank"));
		radioButton_createBlank.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCreateSelection(e);				
			}
		});
		radioButton_createBlank.setBackground(Color.WHITE);
		add(radioButton_createBlank, "2, 2");
		
		JPanel panel_createFetched = new JPanel();
		panel_createFetched.setBackground(Color.WHITE);
		add(panel_createFetched, "2, 3, fill, fill");
		panel_createFetched.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		radioButton_createFetched = new JRadioButton(TextUtils.getText("docear.metadata.extraction.createFetched"));
		radioButton_createFetched.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCreateSelection(e);
			}
		});
		radioButton_createFetched.setBackground(Color.WHITE);
		panel_createFetched.add(radioButton_createFetched, "1, 2");
		
		JPanel panel_Search = new JPanel();
		panel_Search.setBackground(Color.WHITE);
		add(panel_Search, "2, 4, fill, top");
		panel_Search.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("13dlu"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("min(300dlu;pref):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("15dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		labelSearchBy = new JLabel("Search by");
		panel_Search.add(labelSearchBy, "2, 2");
		
		radioButton_searchFile = new JRadioButton(TextUtils.getText("docear.metadata.extraction.lookup.filename"));
		radioButton_searchFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSearchSelection(e);
			}
		});
		radioButton_searchFile.setBackground(Color.WHITE);
		panel_Search.add(radioButton_searchFile, "2, 4, fill, default");
		
		actionLabel_File = new MultiLineActionLabel();
		actionLabel_File.setBackground(Color.WHITE);
		actionLabel_File.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		panel_Search.add(actionLabel_File, "4, 4, fill, fill");
		
		radioButton_searchTitle = new JRadioButton(TextUtils.getText("docear.metadata.extraction.lookup.title"));
		radioButton_searchTitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSearchSelection(e);
			}
		});
		radioButton_searchTitle.setBackground(Color.WHITE);
		panel_Search.add(radioButton_searchTitle, "2, 6");
		
		actionLabel_Title = new JLabel();
		actionLabel_Title.setBackground(Color.WHITE);	
		panel_Search.add(actionLabel_Title, "4, 6, fill, fill");
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "2, 5, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("13dlu"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		labelSearch = new JLabel(TextUtils.getText("docear.metadata.extraction.lookup.search"));
		panel.add(labelSearch, "2, 2");
		
		textFieldSearch = new JTextField();
		panel.add(textFieldSearch, "4, 2");
		textFieldSearch.setColumns(10);		
				
		buttonLookup = new JButton(TextUtils.getText("docear.metadata.extraction.lookup"));
		buttonLookup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				searchMetadata();
			}
			
		});
		panel.add(buttonLookup, "6, 2");
		buttonLookup.setBackground(Color.WHITE);
		
		JPanel panel_FetchedResults = new JPanel();
		panel_FetchedResults.setBackground(Color.WHITE);
		add(panel_FetchedResults, "2, 6, 1, 3, fill, fill");
		panel_FetchedResults.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("13dlu"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(20dlu;min):grow"),}));
		
		scrollPaneFetchedResults = new JScrollPane();
		panel_FetchedResults.add(scrollPaneFetchedResults, "2, 2, fill, fill");
		listModelFetchedResults = new BibtexEntryListModel();
		
		JPanel panel_SearchSettings = new JPanel();
		panel_SearchSettings.setBackground(Color.WHITE);
		add(panel_SearchSettings, "2, 9, fill, fill");
		panel_SearchSettings.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("13dlu"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		button_Settings = new JButton(TextUtils.getText("docear.metadata.extraction.search.settings"));
		button_Settings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callOptionsPage(e);
			}			
		});
		
		listFetchedResults = new JList();		
		listFetchedResults.setVisibleRowCount(10);
		listFetchedResults.setModel(listModelFetchedResults);
		listFetchedResults.setCellRenderer(new BibtexEntryListCellRenderer());
		listFetchedResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFetchedResults.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPaneFetchedResults.setViewportView(listFetchedResults);
		
		labelSpinner = new JLabel("");
		labelSpinner.setBackground(Color.WHITE);
		labelSpinner.setIcon(new ImageIcon(MetaDataExtractorPage.class.getResource("/images/metadata-loader.gif")));
		panel_SearchSettings.add(labelSpinner, "2, 2");
		
		labelStatustext = new JLabel("");
		panel_SearchSettings.add(labelStatustext, "4, 2");
		button_Settings.setBackground(Color.WHITE);
		panel_SearchSettings.add(button_Settings, "6, 2, right, default");
		
		radioButton_CreateXmp = new JRadioButton(TextUtils.getText("docear.metadata.extraction.createXmp"));
		radioButton_CreateXmp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCreateSelection(e);
			}
		});
		radioButton_CreateXmp.setBackground(Color.WHITE);
		add(radioButton_CreateXmp, "2, 11");
		
		JPanel panel_XmpData = new JPanel();
		panel_XmpData.setBackground(Color.WHITE);
		add(panel_XmpData, "2, 12, fill, fill");
		panel_XmpData.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("13dlu"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(20dlu;min):grow"),}));
		
		scrollPaneXmpData = new JScrollPane();
		panel_XmpData.add(scrollPaneXmpData, "2, 2, fill, fill");
		
		listXmpData = new JList();
		listXmpData.setVisibleRowCount(6);
		listXmpData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listXmpData.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPaneXmpData.setViewportView(listXmpData);	
		
		setPreferredSize(new Dimension(720, 550));
		
		radioButtonAttachOnly = new JRadioButton(TextUtils.getText("docear.metadata.extraction.attachOnly"));
		radioButtonAttachOnly.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setCreateSelection(e);
			}
		});
		radioButtonAttachOnly.setBackground(Color.WHITE);
		add(radioButtonAttachOnly, "2, 14");
		
		labelWarning = new JLabel();
		labelWarning.setFont(new Font("Tahoma", Font.BOLD, 13));
		labelWarning.setForeground(Color.RED);
		labelWarning.setHorizontalAlignment(SwingConstants.CENTER);
		add(labelWarning, "2, 16");
	}

	protected void callOptionsPage(ActionEvent e) {
		session.gotoPage("metadataOptions");		
	}

	protected void searchMetadata() {
		if(this.searchValue.equals(this.textFieldSearch.getText())) return;
		this.searchValue = this.textFieldSearch.getText();
		this.listModelFetchedResults.clearEntries();
		this.listModelFetchedResults.fireDataChanged();
		this.labelSpinner.setVisible(true);
		requestCount = 0;
		labelStatustext.setText("Finished " + requestCount + " of " + searchHub.getRegisteredEngines().size() + " Request(s).");
		Set<Class<?>> sources = setupSources();
		Map<ExtractorConfigKey, Object> options = setupSearchOptions();
		try {
			this.searchHub.asyncSearch(this.textFieldSearch.getText(), sources, options, new MetaDataListener() {
				
				@Override
				public void onFinishedRequest(MetaDataEvent event) {
					if(event instanceof FetchedResultsEvent) {
						ArrayList<Pair<BibtexEntry,MetaDataSource>> entries = new ArrayList<Pair<BibtexEntry,MetaDataSource>>();
						List<MetaData> results = ((FetchedResultsEvent)event).getResult();
						for(MetaData result : results){
							MetaDataSource source = result.getSource();
							if(source instanceof ScholarSource){
								if(searchValue.equals(result.getQuery())){
									String bibtex = ((ScholarMetaData)result).getBibtex();
									try {
										ParserResult parsedBibtex = BibtexParser.parse(new StringReader(bibtex));
										for(BibtexEntry entry : parsedBibtex.getDatabase().getEntries()){
											entries.add(new Pair<BibtexEntry,MetaDataSource>(entry, source));
										}
										
									} catch (IOException e) {
										LogUtils.warn(e);
									}	
								}
							}
						}
						
						listModelFetchedResults.addEntries(entries);
						requestCount++;
						if(requestCount >= searchHub.getRegisteredEngines().size()){
							labelStatustext.setText("Fetched " + listModelFetchedResults.getSize() + " entries.");														
						}
						else{
							labelStatustext.setText("Finished " + requestCount + " of " + searchHub.getRegisteredEngines().size() + " Request(s).");
						}
						if(listModelFetchedResults.getSize() > 0 && listFetchedResults.getSelectedIndex() < 0){
							listFetchedResults.setSelectedIndex(0);
						}						
					}					
					labelSpinner.setVisible(false);
				}
				
				@Override
				public void onCaptchaRequested(final MetaDataEvent event) {
					CaptchaRequestDialog.showDialog((CaptchaEvent)event);			
				}
			});
			
		} catch (MalformedConfigException e) {
			LogUtils.warn(e);
		}
		
	}

	private Map<ExtractorConfigKey, Object> setupSearchOptions() {
		Map<ExtractorConfigKey, Object> options = new HashMap<ExtractorConfigKey, Object>();
		ResourceController properties = Controller.getCurrentController().getResourceController();
		options.put(CommonConfigKeys.MAXRESULTS, properties.getIntProperty(MetaDataOptionsPage.DOCEAR_METADATA_MAX_RESULT));
		options.put(CommonConfigKeys.COOKIE_FOLDER, new File(ServiceController.getController().getUserSettingsHome()).getAbsolutePath());
		options.put(CommonConfigKeys.DEBUGLOGGING, properties.getBooleanProperty(MetaDataOptionsPage.DOCEAR_METADATA_DEBUG_LOGGING));
		return options;
	}

	private Set<Class<?>> setupSources() {
		Set<Class<?>> sources = new HashSet<Class<?>>();
		ResourceController properties = Controller.getCurrentController().getResourceController();
		if(properties.getBooleanProperty(MetaDataOptionsPage.DOCEAR_METADATA_SEARCH_SCHOLAR)){
			sources.add(GoogleScholarSearchEngine.class);
		}
		if(properties.getBooleanProperty(MetaDataOptionsPage.DOCEAR_METADATA_SEARCH_DOCEAR)){
			//sources.add(DocearSearchEngine.class);
		}
		return sources;
	}

	protected void setSearchSelection(ActionEvent event) {
		boolean searchTitle = event.getSource() == this.radioButton_searchTitle;
		boolean searchFile = event.getSource() == this.radioButton_searchFile;
		
		this.radioButton_searchFile.setSelected(searchFile);
		this.radioButton_searchTitle.setSelected(searchTitle);
		
		if(searchTitle){
			this.textFieldSearch.setText(this.pdfTitle);
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_SEARCH_OPTION, DOCEAR_METADATA_SEARCH_BY_TITLE);
		}
		if(searchFile){
			this.textFieldSearch.setText(this.pdfFileName.substring(0, CoreUtils.resolveURI(pdfFile).getName().lastIndexOf(".")));
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_SEARCH_OPTION, DOCEAR_METADATA_SEARCH_BY_FILE);
		}
	}

	protected void setCreateSelection(ActionEvent event) {
		boolean createBlank = event.getSource() == this.radioButton_createBlank;
		boolean createFetched = event.getSource() == this.radioButton_createFetched;
		boolean createXmp = event.getSource() == this.radioButton_CreateXmp;
		boolean attachOnly = event.getSource() == this.radioButtonAttachOnly;
		
		this.radioButton_createBlank.setSelected(createBlank);		
		this.radioButton_createFetched.setSelected(createFetched);		
		this.radioButton_CreateXmp.setSelected(createXmp);	
		this.radioButtonAttachOnly.setSelected(attachOnly);
		
		this.textFieldSearch.setEnabled(createFetched);
		this.buttonLookup.setEnabled(createFetched);
		this.listFetchedResults.setEnabled(createFetched);		
		this.button_Settings.setEnabled(createFetched);
		this.radioButton_searchFile.setEnabled(createFetched);
		if(this.pdfTitle != null && !this.pdfTitle.isEmpty()){
			this.radioButton_searchTitle.setEnabled(createFetched);
		}		
		this.actionLabel_File.setEnabled(createFetched);
		this.actionLabel_Title.setEnabled(createFetched);
		this.scrollPaneFetchedResults.setEnabled(createFetched);
		this.scrollPaneFetchedResults.getHorizontalScrollBar().setEnabled(createFetched);		
		this.scrollPaneFetchedResults.getVerticalScrollBar().setEnabled(createFetched);
		this.scrollPaneFetchedResults.getViewport().getView().setEnabled(createFetched);
		this.labelStatustext.setEnabled(createFetched);
		this.labelSpinner.setEnabled(createFetched);
		this.labelSearchBy.setEnabled(createFetched);
		this.labelSearch.setEnabled(createFetched);
		
		this.listXmpData.setEnabled(createXmp);
		this.scrollPaneXmpData.setEnabled(createXmp);
		this.scrollPaneXmpData.getHorizontalScrollBar().setEnabled(createXmp);		
		this.scrollPaneXmpData.getVerticalScrollBar().setEnabled(createXmp);
		this.scrollPaneXmpData.getViewport().getView().setEnabled(createXmp);
		if(createXmp){
			if(listXmpData.getSelectedIndex() < 0 || listXmpData.getSelectedIndex() > listXmpData.getModel().getSize()){
				listXmpData.setSelectedIndex(0);
			}
		}		
		
		if(createBlank){
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_CREATE_ENTRY_OPTION, DOCEAR_METADATA_CREATE_EMPTY_ENTRY);
		}
		else if(createFetched){
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_CREATE_ENTRY_OPTION, DOCEAR_METADATA_CREATE_FETCHED_DATA_ENTRY);
		}
		else if(createXmp){
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_CREATE_ENTRY_OPTION, DOCEAR_METADATA_CREATE_XMP_DATA_ENTRY);
		}
		else if(attachOnly){
			Controller.getCurrentController().getResourceController().setProperty(DOCEAR_METADATA_CREATE_ENTRY_OPTION, DOCEAR_METADATA_CREATE_ATTACH_ONLY);
		}
	}

	@Override
	public String getTitle() {
		return TextUtils.getText("docear.metadata.title.title");
	}

	@Override
	public void preparePage(final WizardSession session) {		
		this.session = session;		
		session.setWizardTitle(getTitle());
		session.getBackButton().setVisible(true);
		getRootPane().setDefaultButton((JButton)session.getNextButton());
		session.getNextButton().setText(TextUtils.getText("ok"));
		session.getBackButton().setText(TextUtils.getText("cancel"));	
		
		MetaDataActionObject data = session.get(MetaDataActionObject.class);
		this.pdfFile = data.getCurrentPDF();
		this.pdfFileName = CoreUtils.resolveURI(pdfFile).getName();
		this.pdfTitle = AnnotationController.getDocumentTitle(pdfFile);
		this.xmpData = this.readXmpData(CoreUtils.resolveURI(pdfFile));
		this.searchHub.registerSearchEngine(new GoogleScholarSearchEngine(null));
		this.labelSpinner.setVisible(false);
				
		if(data.getResult().get(pdfFile).getEntryToUpdate() != null || data.getResult().get(pdfFile).isDuplicatePdf()){
			labelWarning.setVisible(true);
			if(data.getResult().get(pdfFile).isDuplicatePdf()){
				labelWarning.setText(TextUtils.getText("docear.metadata.extraction.dublicate"));
			}
			else{
				labelWarning.setText(TextUtils.getText("docear.metadata.extraction.warning"));
			}
		}
		else{
			labelWarning.setVisible(false);
		}
		
		if(data.getResult().get(pdfFile).isShowattachOnlyOption()){
			radioButtonAttachOnly.setVisible(true);
		}
		else{
			radioButtonAttachOnly.setVisible(false);
		}
		
		
		actionLabel_Title.setText(this.pdfTitle);
		actionLabel_File.setText("<action cmd=\"open_document_link\">" + this.pdfFileName + "</action>");
		actionLabel_File.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Controller.getCurrentController().getViewController().openDocument(pdfFile);
				} catch (IOException e) {
					LogUtils.warn("could not open link: "+ e.getLocalizedMessage());
				}
			}
		});
		
		searchValue = "";
		listModelFetchedResults.clearEntries();
		BibtexEntryListModel listModel = new  BibtexEntryListModel(this.xmpData);			
		this.listXmpData.setModel(listModel);
		this.listXmpData.setCellRenderer(new BibtexEntryListCellRenderer());
		if(hasXmpData()){
			this.radioButton_CreateXmp.setEnabled(true);			
		}
		else{
			this.radioButton_CreateXmp.setEnabled(false);
			this.listXmpData.setEnabled(false);
		}
		
		String createEntryOption = Controller.getCurrentController().getResourceController().getProperty(DOCEAR_METADATA_CREATE_ENTRY_OPTION, DOCEAR_METADATA_CREATE_FETCHED_DATA_ENTRY);
		if(data.getResult().get(pdfFile).isDuplicatePdf()){
			setCreateSelection(new ActionEvent(null, 0, ""));
		}
		else{
			if(createEntryOption.equals(DOCEAR_METADATA_CREATE_EMPTY_ENTRY)){
				setCreateSelection(new ActionEvent(this.radioButton_createBlank, 0, ""));
			}
			else if (createEntryOption.equals(DOCEAR_METADATA_CREATE_FETCHED_DATA_ENTRY)) {
				setCreateSelection(new ActionEvent(this.radioButton_createFetched, 0, ""));			
			}
			else if (createEntryOption.equals(DOCEAR_METADATA_CREATE_XMP_DATA_ENTRY)) {
				if(this.hasXmpData()){
					setCreateSelection(new ActionEvent(this.radioButton_CreateXmp, 0, ""));
				}
				else{
					setCreateSelection(new ActionEvent(this.radioButton_createFetched, 0, ""));
				}
			}
			else if (createEntryOption.equals(DOCEAR_METADATA_CREATE_ATTACH_ONLY)){
				if(data.getResult().get(pdfFile).isShowattachOnlyOption()){
					setCreateSelection(new ActionEvent(this.radioButtonAttachOnly, 0, ""));
				}
				else{
					setCreateSelection(new ActionEvent(this.radioButton_createFetched, 0, ""));
				}
			}
		}
		
		String searchOption = Controller.getCurrentController().getResourceController().getProperty(DOCEAR_METADATA_SEARCH_OPTION, DOCEAR_METADATA_SEARCH_BY_TITLE);
		if(searchOption.equals(DOCEAR_METADATA_SEARCH_BY_TITLE)){			
			setSearchSelection(new ActionEvent(this.radioButton_searchTitle, 0, ""));			
		}
		else if (searchOption.equals(DOCEAR_METADATA_SEARCH_BY_FILE)){
			setSearchSelection(new ActionEvent(this.radioButton_searchFile, 0, ""));
		}
		if(this.pdfTitle == null || this.pdfTitle.isEmpty()){
			this.radioButton_searchTitle.setEnabled(false);
			setSearchSelection(new ActionEvent(this.radioButton_searchFile, 0, ""));
		}
		
		session.getNextButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MetaDataActionObject result = session.get(MetaDataActionObject.class);
				if(radioButton_createBlank.isSelected()){
					result.getResult().get(pdfFile).setSelectedBlank(true);
				}
				if(radioButton_createFetched.isSelected()){
					result.getResult().get(pdfFile).setSelectedFetched(true);
					int row = listFetchedResults.getSelectedIndex();
					if(row >= 0 && row < listModelFetchedResults.getSize()){
						result.getResult().get(pdfFile).setResultEntry(listModelFetchedResults.getEntry(row).p);						
					}				
				}
				if(radioButton_CreateXmp.isSelected()){
					result.getResult().get(pdfFile).setSelectedXmp(true);
					int row = listXmpData.getSelectedIndex();
					if(row >= 0 && row < listXmpData.getModel().getSize()){
						result.getResult().get(pdfFile).setResultEntry( ((BibtexEntryListModel)listXmpData.getModel()).getEntry(row).p);							
					}
				}
				if(radioButtonAttachOnly.isSelected()){
					result.getResult().get(pdfFile).setAttachOnly(true);
				}
			}
		});
		
		session.getModel().getCurrentPageDescriptor().setKeyBindingProcessor(new IPageKeyBindingProcessor() {
			
			@Override
			public boolean processKeyEvent(KeyEvent e) {				
				if(e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_ESCAPE){
					session.getBackButton().doClick();
					return true;
				}
				return false;
			}
		});
		
		session.getBackButton().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MetaDataActionObject result = session.get(MetaDataActionObject.class);
				result.getResult().get(pdfFile).setSelectedCancel(true);				
			}
		});
		
		if(radioButton_createFetched.isSelected()){
			searchMetadata();
		}		
	}
	
	private boolean hasXmpData(){
		return this.xmpData.size() > 0;
	}
	
	private List<Pair<BibtexEntry, MetaDataSource>> readXmpData(File file){
		List<Pair<BibtexEntry, MetaDataSource>> xmp = new ArrayList<Pair<BibtexEntry, MetaDataSource>>();
		try {			
			List<BibtexEntry> entries =  new ImportFormatReader().importFromFile(new PdfXmpImporter(), file.getAbsolutePath());
			for(BibtexEntry entry : entries){
				xmp.add(new Pair<BibtexEntry, MetaDataSource>(entry, AbstractSource.ABSTRACT));
			}
		} catch (Exception e) {
			LogUtils.warn("Could not import Xmp Data from File " + file.getAbsolutePath());
		}
		return xmp;
	}
	
	class BibtexEntryListModel extends DefaultListModel {		
		
		private static final long serialVersionUID = 1L;
		private Collection<Pair<BibtexEntry, MetaDataSource>> entries = new ArrayList<Pair<BibtexEntry, MetaDataSource>>();
		
		public BibtexEntryListModel() {}
		
		public BibtexEntryListModel(Collection<Pair<BibtexEntry, MetaDataSource>> entries) {
			if(entries != null){
				this.entries = entries;
			}
		}
	
		public void fireDataChanged() {
			super.fireContentsChanged(this, 0, entries.size()-1);			
		}
		
		public void clearEntries(){
			this.entries.clear();
			fireDataChanged();
		}

		public void addEntries(Collection<Pair<BibtexEntry, MetaDataSource>> entries){
			this.entries.addAll(entries);
			fireDataChanged();
		}
		
		public Pair<BibtexEntry, MetaDataSource> getEntry(int rowIndex) {
			if(rowIndex < 0 || rowIndex >= entries.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			int i=0;
			for (Iterator<Pair<BibtexEntry, MetaDataSource>> iterator = entries.iterator(); iterator.hasNext(); i++) {
				if(i==rowIndex) {
					return iterator.next();
				}
				iterator.next();
			}
			return null;
		}		

		public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public int getSize() {
			if(entries == null) {
				return 0;
			}
			return entries.size();
		}
		
		
		
		public String getElementAt(int index) {
			Pair<BibtexEntry, MetaDataSource> entry = getEntry(index);
			if(entry == null || entry.p == null || entry.v == null) {
				return null;
			}
			
			StringBuffer sb = new StringBuffer();
			StringReader sr = new StringReader(Globals.prefs.get("preview0").replaceAll("__NEWLINE__", "\n"));
	        ExportFormats.entryNumber = 1; // Set entry number in case that is included in the preview layout.
			try {
				sb.append(new LayoutHelper(sr).getLayoutFromText(Globals.FORMATTER_PACKAGE).doLayout(entry.p,ReferencesController.getController().getJabrefWrapper().getDatabase()));				
			} 
			catch (Exception e) {
			}
			if(entry.v instanceof ScholarSource){
				sb.insert(0, "<b>"+TextUtils.getText("docear.metadata.extraction.search.result.title") 
							+ " "
							+ TextUtils.getText("docear.metadata.extraction.sources.scholar")
							+ ":</b><br>");
			}
			
			return sb.toString();
		}
	}

	class BibtexEntryListCellRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = 1L;
		
		public Component getListCellRendererComponent(JList table, Object value, int index, final boolean isSelected, boolean hasFocus) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>");
			sb.append(value);
			sb.append("</body></html>");
			final JLabel label = (JLabel) super.getListCellRendererComponent(table, sb, index, isSelected, hasFocus);
			if(index > 0) {
				label.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, (Color) new Color(0, 0, 0)), new EmptyBorder(10, 8, 10, 8)));
			}
			else {
				label.setBorder(new EmptyBorder(10, 8, 10, 8));
			}							
			return label;
		}
	}
}
