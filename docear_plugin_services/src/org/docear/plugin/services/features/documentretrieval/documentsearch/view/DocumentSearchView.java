package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.documentsearch.SearchModel;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.view.DocumentView;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

public class DocumentSearchView extends DocumentView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DocumentSearchPanel documentSearchPanel;
	
	public DocumentSearchView(DocumentsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	@Override
	protected Container getNewRecommandationContainerComponent(String title) {
//		JPanel containerPanel = new JPanel();
//		containerPanel.setLayout(new BorderLayout());
//		containerPanel.setBackground(Color.WHITE);
//		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
//		JPanel panel = new JPanel();
//				
//		JLabel containerTitle = new JLabel("<html><b>"+title+"</b></html>");
//		containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 18));
//		
//		containerPanel.add(containerTitle, BorderLayout.NORTH);
//		
//		panel.setBackground(Color.WHITE);
//		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
//		panel.setLayout(new ListLayoutManager());		
//		containerPanel.add(panel, BorderLayout.CENTER);
//		
//		this.add(getNewButtonBar(), BorderLayout.NORTH);
//		this.add(containerPanel, BorderLayout.CENTER);
//		this.add(getStarBar(), BorderLayout.SOUTH);
		
//		return panel
		return new JPanel();
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.CENTER);
		
		this.add(getSearchPanel(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);		
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}

	public Component getSearchPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getNewButtonBar(false), BorderLayout.NORTH);
		SearchModel searchModel = getSearchModel();
		if (searchModel != null && searchModel.getId() != null) {
			documentSearchPanel = new DocumentSearchPanel(searchModel.getModel().split(" "), searchModel.getId());
		}
		else {
			documentSearchPanel = new DocumentSearchPanel();
		}
		panel.add(documentSearchPanel, BorderLayout.CENTER);
		return panel;
	}
	
	private SearchModel getSearchModel() {
		final DocearUser user = ServiceController.getCurrentUser();		
		if (user == null) {
			return null;
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<SearchModel> task = executor.submit(new Callable<SearchModel>() {
			public SearchModel call() throws Exception {
				try {
    				DocearServiceResponse response = ServiceController.getConnectionController().get("/user/"+user.getUsername()+"/searchmodel/");
    				
    				DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
    				IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
    				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
    				parser.setBuilder(xmlBuilder);
    				parser.setReader(reader);
    				parser.parse();
    				DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
    				
    				DocearXmlElement element = result.find("searchmodel");
    				SearchModel searchModel = new SearchModel(Long.valueOf(element.getAttributeValue("id")), element.getContent().trim());
					return searchModel;
				}
				catch(NullPointerException ignore) {}
				return null;
			}
		});
		try {
			return task.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);		
		}
		catch(Exception e) {
			LogUtils.warn(e);
		}
		
		return null;
	}
	
	public String getQueryText() {
		if (this.documentSearchPanel == null) {
			return "";
		}
		return this.documentSearchPanel.getQueryText();
	}

	
	private Container getPaginator() {
		JPanel paginator = new JPanel();		
		paginator.setLayout(new BoxLayout(paginator,BoxLayout.X_AXIS));
		paginator.setBackground(Color.WHITE);
		paginator.add(Box.createHorizontalGlue());
		for (int i=1; i<=10; i++) {
			JButton page = new JButton(String.valueOf(i));
			page.setBackground(Color.WHITE);
			page.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			paginator.add(page);
		}
		paginator.add(Box.createHorizontalGlue());
		
		return paginator;
	}

	@Override
	protected void addComponendAfterDocumentList(Container documentList) {
		documentList.add(getPaginator());
	}

}
